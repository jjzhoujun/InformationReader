package com.zhimu.news;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pgyersdk.crash.PgyCrashManager;
import com.umeng.analytics.MobclickAgent;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.utils.AllAppKeyUtils;
import com.zhimu.news.utils.NetUtils;

import cn.waps.AppConnect;
import ofs.ahd.dii.AdManager;
import ofs.ahd.dii.st.SplashView;
import ofs.ahd.dii.st.SpotDialogListener;
import ofs.ahd.dii.st.SpotManager;

/**
 * 闪屏页:第一个页面
 * 2016-01-04
 */
public class SplashActivity extends BaseActivity {

    // 广告
    private RelativeLayout relative_ad;
    // 倒计时
    private TextView tv_time;
    private MyCount myCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /**
         * 万普广告
         * 第一个参数：分发渠道，查万普广告渠道列表
         * 第二个参数：万普后台生成
         *
         */
        AppConnect.getInstance("360", "dc14d31443525c3c327dff3fb7012094", this);
        // 万普广告异步预加载数据：20条
        AppConnect.getInstance(this).initAdInfo();

        // 有米广告：true为测试模式
        AdManager.getInstance(this).init(AllAppKeyUtils.YOUMI_ID, AllAppKeyUtils.YOUMI_SECRET, false);
        // 缓存3到5条数据
        SpotManager.getInstance(this).loadSplashSpotAds();

        // 初始化控件
        initView();

        // 如果进来没有网络，主页会加载3次，在这里判断网络
        if (NetUtils.isNetConnected(this)) {
            // 展示开屏广告
            showSplashView();
        }else {
            // 进入主页
            intoActivity();
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        relative_ad = (RelativeLayout) findViewById(R.id.relative_ad);
        tv_time = (TextView) findViewById(R.id.tv_time);
        TextView splash_version_txt = (TextView) findViewById(R.id.splash_version_txt);

        // 设置版本号
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;

            String name1 = getResources().getString(R.string.splash_version_txt_desc) + "\nv " + versionName;
            if (splash_version_txt != null) {
                splash_version_txt.setText(name1);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

            PgyCrashManager.reportCaughtException(this, e);
        }

        // 广告倒计时
        tv_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 取消倒计时
                myCount.cancel();

                // 进入主页
                intoActivity();
            }
        });

    }

    /**
     * 跳转到首页
     */
    private void intoActivity() {
        Intent intent = new Intent(SplashActivity.this, TabHomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 展示开屏广告：通过自定义形式展示
     */
    private void showSplashView() {

        // 插屏动画效果，0:ANIM_NONE为无动画，1:ANIM_SIMPLE为简单动画效果，2:ANIM_ADVANCE为高级动画效果
        SpotManager.getInstance(this).setAnimationType(SpotManager.ANIM_ADVANCE);

        SplashView splashView = new SplashView(this, null);
        // 设置是否显示倒计时，默认显示,这里设置为不显示:不好看
        splashView.setShowReciprocal(false);
        // 设置是否显示关闭按钮，默认不显示
        splashView.hideCloseBtn(true);
        // 传入跳转的intent，若传入intent，初始化时目标activity应传入null
        Intent intent = new Intent(this, TabHomeActivity.class);
        splashView.setIntent(intent);
        // 展示失败后是否直接跳转，默认直接跳转,不跳转
        splashView.setIsJumpTargetWhenFail(true);

        //获取开屏视图
        View splash = splashView.getSplashView();

        // 自适应屏幕的宽和高
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        // 添加到控件上
        relative_ad.addView(splash, params);

        // 展示自定义的插屏
        SpotManager.getInstance(this).showSplashSpotAds(this, splashView, new SpotDialogListener() {
            @Override
            public void onShowSuccess() {
                tv_time.setVisibility(View.VISIBLE);
                //开始倒计时
                myCount = new MyCount(6000, 1000);
                myCount.start();
            }

            @Override
            public void onShowFailed() {

            }

            @Override
            public void onSpotClosed() {

            }

            @Override
            public void onSpotClick(boolean b) {

            }
        });

    }

    /**
     * 倒计时
     */
    class MyCount extends CountDownTimer {
        /**
         * @param millisInFuture    总共时间
         * @param countDownInterval 间隔时间
         */
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        /**
         * @param millisUntilFinished 当前剩余时间
         */
        @Override
        public void onTick(long millisUntilFinished) {
            String time = (millisUntilFinished / 1000 - 1) +
                    getResources().getString(R.string.splash_ad_txt_name);
            tv_time.setText(time);
        }

        @Override
        public void onFinish() {

        }
    }

    /**
     * 当按回退键时，可以退出插屏
     */
    @Override
    public void onBackPressed() {
        // 可以点击后退关闭插播广告。
        if (!SpotManager.getInstance(this).disMiss()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        SpotManager.getInstance(this).onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 调用插屏，开屏，退屏时退出
        SpotManager.getInstance(this).onDestroy();
        super.onDestroy();

        if (myCount != null) {
            myCount.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 调用友盟统计数据分析:启动次数和时长：新增用户、活跃用户、启动次数、使用时长
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        MobclickAgent.onPause(this);
    }
}
