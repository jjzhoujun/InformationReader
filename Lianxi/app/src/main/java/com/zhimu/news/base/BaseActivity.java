package com.zhimu.news.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.pgyersdk.activity.FeedbackActivity;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.PgyFeedbackShakeManager;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

/**
 * 所有Activity的基类
 * 2016.4.16.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 友盟统计：true会对日志进行加密。加密模式可以有效防止网络攻击，提高数据安全性
        AnalyticsConfig.enableEncrypt(true);

        // 蒲公英SDK：摇一摇反馈
        PgyCrashManager.register(this);


    }

    @Override
    protected void onResume() {
        super.onResume();

        // 调用友盟统计数据分析:启动次数和时长：新增用户、活跃用户、启动次数、使用时长
        MobclickAgent.onResume(this);

        /**
         * 蒲公英SDK：摇一摇用户反馈
         */
        // 自定义摇一摇的灵敏度，默认为950，数值越小灵敏度越高。
        PgyFeedbackShakeManager.setShakingThreshold(950);
        // 以对话框的形式弹出
//        PgyFeedbackShakeManager.register(this);
        // 以Activity的形式打开，这种情况下必须在AndroidManifest.xml配置FeedbackActivity
        // 打开沉浸式,默认为false
        FeedbackActivity.setBarImmersive(true);
        PgyFeedbackShakeManager.register(this, false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        MobclickAgent.onPause(this);

        PgyFeedbackShakeManager.unregister();

    }
}
