package com.zhimu.news;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.utils.SharedUtils;

/**
 * 我的收藏：新闻详情展示界面
 */
public class NewsCollectDetailedActivity extends BaseActivity {

    // 新闻内容
    private TextView tv_news_content;
    private TextView tv_news_title;
    private TextView tv_news_source;
    private ImageView iv_news_detailed_image;
    private TextView tv_news_prompt;
    private String link;

    // 标记
    public static final int NEWS_COLLECT_DETAILED_ACTIVITY = 10;
    private DisplayImageOptions imageOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detailed);

        imageOptions = MyApplication.getInstance().getOptions(R.mipmap.news_home);

        // 控件
        initView();
        // 数据
        initData();

    }

    /**
     * 初始化控件
     */
    private void initView() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // 内容
        tv_news_content = (TextView) findViewById(R.id.tv_news_content);
        tv_news_title = (TextView) findViewById(R.id.tv_news_title);
        tv_news_source = (TextView) findViewById(R.id.tv_news_source);
        // 开头图片
        iv_news_detailed_image = (ImageView) findViewById(R.id.iv_news_detailed_image);
        // 提示
        tv_news_prompt = (TextView) findViewById(R.id.tv_news_prompt);

        // 取出保存的值：默认在xml中保存的是16sp
        int size = SharedUtils.getSharedInt(this, "size", 0);
        switch (size) {
            case 14:
                tv_news_content.setTextSize(size);
                break;
            case 18:
                tv_news_content.setTextSize(size);
                break;
            case 20:
                tv_news_content.setTextSize(size);
                break;
            default:
                break;
        }

        if (mToolbar != null) {
            mToolbar.setTitle(R.string.my_collect_detailed_tool_title);
            mToolbar.setTitleTextColor(Color.WHITE);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NewsCollectDetailedActivity.this, MyCollectActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                }
            });
        }

    }

    /**
     * 初始化数据
     */
    private void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String html = bundle.getString("html");
        link = bundle.getString("url_link");
        String title = bundle.getString("title");
        String source = bundle.getString("source");
        String pub_date = bundle.getString("pub_date");
        String image_url = bundle.getString("imgUrl");

        // 标题：设置粗体显示
        tv_news_title.getPaint().setFakeBoldText(true);
        tv_news_title.setText(title);

        // 来源+时间：点击查看原网页
        if (pub_date != null) {
            String strSource = source + getResources().getString(R.string.my_collect_detailed_source) +
                    pub_date.substring(5, 16) + getResources().getString(R.string.my_collect_detailed_see);
            tv_news_source.setText(strSource);
        }

        // Html内容：转成可显示的字符串
        Spanned fromHtml = Html.fromHtml(html);
        tv_news_content.setText(fromHtml);

        // 图片网络加载
        if (image_url != null && !image_url.equals("")) {
            ImageLoader.getInstance().displayImage(
                    image_url,
                    iv_news_detailed_image,
                    imageOptions);
        }

        // 点击查看原网页
        tv_news_source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intoWebNewsActivity();
            }
        });

        // 最下面提示查看原网页
        tv_news_prompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intoWebNewsActivity();
            }
        });
    }

    /**
     * 跳转到网页显示
     */
    private void intoWebNewsActivity() {
        Intent intent = new Intent(NewsCollectDetailedActivity.this, NewsWebActivity.class);
        intent.putExtra("url_link", link);
        intent.putExtra("where_activity", NEWS_COLLECT_DETAILED_ACTIVITY);
        startActivity(intent);
    }

}
