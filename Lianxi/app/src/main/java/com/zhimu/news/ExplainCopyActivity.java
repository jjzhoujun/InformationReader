package com.zhimu.news;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.pgyersdk.crash.PgyCrashManager;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.utils.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * 关于本软件的解释，去除万普广告
 * 2016-04-20
 */
public class ExplainCopyActivity extends BaseActivity {

    private TextView explain_txt;
    private TextView explain_version_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explain_copy);

        /**
         * 加载谷歌广告控件，请求广告
         */
        initGoogleAd();

        initView();

        initData();

    }

    /**
     * 可能请求不到广告数据，须设置监听，成功才显示
     * 指导链接：https://developers.google.com/admob/android/quick-start#faq
     *
     */
    private void initGoogleAd() {
        final AdView mAdView = (AdView) findViewById(R.id.explain_copy_adView);
        AdRequest.Builder builder = new AdRequest.Builder();
        // 添加一个测试id，发布时不需要
//        builder.addTestDevice(getResources().getString(R.string.banner_ad_unit_id));
        // 创建请求
        AdRequest adRequest = builder.build();
        if (mAdView != null) {
            // 请求广告
            mAdView.loadAd(adRequest);
            // 设置广告加载监听
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);

                    // 加载失败
                    Log.e("activity", "展示谷歌广告失败");
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();

                    // 加载成功，显示广告
                    mAdView.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    private void initView() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // 标题
        TextView mTitle = (TextView) findViewById(R.id.toolbar_title);
        // 版本名称
        explain_version_txt = (TextView) findViewById(R.id.explain_copy_version_txt);
        // 软件解释
        explain_txt = (TextView) findViewById(R.id.explain_copy_txt);

        if (mTitle != null) {
            mTitle.setText(R.string.explain_title);
        }

        if (mToolbar != null) {
            mToolbar.setTitle("");
            mToolbar.setTitleTextColor(Color.WHITE);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击返回首页：从栈中提到顶端，不是默认的重新创建
                    Intent intent = new Intent(ExplainCopyActivity.this, SettingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                }

            });
        }
    }

    private void initData() {
        try {
            // 读出文件
            InputStream inputStream = getResources().getAssets().open("news.txt");
            String result = StreamUtils.readFromStream(inputStream);
            // 声明
            explain_txt.setText(result);

        } catch (IOException e) {
            PgyCrashManager.reportCaughtException(this, e);
        }

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            // 版本名
            String versionName = packageInfo.versionName;
            String name = getResources().getString(R.string.explain_version_txt) + versionName;
            explain_version_txt.setText(name);

        } catch (PackageManager.NameNotFoundException e) {
            PgyCrashManager.reportCaughtException(this, e);
        }

    }
}
