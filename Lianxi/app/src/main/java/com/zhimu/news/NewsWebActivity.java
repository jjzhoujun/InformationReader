package com.zhimu.news;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zhimu.news.base.BaseActivity;

/**
 * 打开网页新闻界面
 * Created by Administrator on 2016.2.24.
 */
public class NewsWebActivity extends BaseActivity {

    //  声明网页引擎
    private WebView webView;
    // 链接地址
    private String url_link;
    // activity标记
    private int where_activity;
    private AnimationDrawable drawable;
    private LinearLayout linearLayout;
    private WebRunnable webRunnable;

    // 异步处理网页：因为界面有一个刷新进度显示
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_web);

        // 获取传过来的链接
        Bundle bundle = getIntent().getExtras();
        url_link = bundle.getString("url_link");
        where_activity = bundle.getInt("where_activity");

        initView();

        // 设置webview参数
        initWebView();

        // 异步请求加载网络
        webRunnable = new WebRunnable();
        mHandler.post(webRunnable);

    }

    /**
     * 初始化控件
     */
    private void initView() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        linearLayout = (LinearLayout) findViewById(R.id.linear_progress);
        ImageView imageView = (ImageView) findViewById(R.id.iv_progress);
        webView = (WebView) findViewById(R.id.webView);

        // 开始加载动画
        linearLayout.setVisibility(View.VISIBLE);
        if (imageView != null) {
            drawable = (AnimationDrawable) imageView.getDrawable();
        }
        drawable.start();

        // toolbar设置参数
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.news_web_tool_title);
            mToolbar.setTitleTextColor(Color.WHITE);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // 根据不同值导航到上一页
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    intoWhereActivity();

                }
            });
        }

    }

    /**
     * 根据不同activity导航到上一页
     */
    private void intoWhereActivity() {

        Intent intent;
        switch (where_activity) {
            // 新闻搜藏详情界面
            case NewsCollectDetailedActivity.NEWS_COLLECT_DETAILED_ACTIVITY:
                intent = new Intent(NewsWebActivity.this, NewsCollectDetailedActivity.class);
                break;
            // 新闻搜藏更多图片界面
            case NewsCollectMoreImageActivity.NEWS_COLLECT_MORE_IMAGE_ACTIVITY:
                intent = new Intent(NewsWebActivity.this, NewsCollectMoreImageActivity.class);
                break;
            // 新闻详情界面
            case NewsDetailedActivity.NEWS_DETAILED_ACTIVITY:
                intent = new Intent(NewsWebActivity.this, NewsDetailedActivity.class);
                break;
            // 新闻详情更多图片界面
            case NewsMoreImageActivity.NEWS_MORE_IMAGE_ACTIVITY:
                intent = new Intent(NewsWebActivity.this, NewsMoreImageActivity.class);
                break;
            default:
                intent = new Intent(NewsWebActivity.this, TabHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                break;
        }
        startActivity(intent);
        finish();

    }

    /**
     * webview设置参数
     */
    private void initWebView() {
        WebSettings webSettings = webView.getSettings();
        // 可任意比例缩放
        webSettings.setUseWideViewPort(true);
        // 加载页面模式
        webSettings.setLoadWithOverviewMode(true);
        // 设置支持javascript脚本
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 适应屏幕，内容自动缩放
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 隐藏webview缩放按钮
        webSettings.setDisplayZoomControls(false);
        // 允许访问文件
        webSettings.setAllowFileAccess(true);
        // 支持缩放
        webSettings.setSupportZoom(true);
        webSettings.setAppCacheEnabled(true);

        /**
         * 这里选择默认：辅助webview处理javascript的对话框、网站标题、网站title、加载进度条等;也可以定制
         */
        webView.setWebChromeClient(new WebChromeClient());
        // 在webview中打开链接
        webView.setWebViewClient(new ViewClient());
    }

    /**
     * 处理各种通知，请求事件，加载自定义的进度提示
     */
    private class ViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null) view.loadUrl(url);
            return true;
        }

        /**
         * 加载完成
         */
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            drawable.stop();

            linearLayout.setVisibility(View.GONE);

            webView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

            Toast.makeText(NewsWebActivity.this, R.string.news_web_toast_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        // 销毁线程
        mHandler.removeCallbacks(webRunnable);
        super.onDestroy();
    }

    /**
     * 实现线程接口
     */
    class WebRunnable implements Runnable {

        @Override
        public void run() {
            webView.loadUrl(url_link);
        }
    }

}
