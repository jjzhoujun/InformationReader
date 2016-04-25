package com.zhimu.news;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.zhimu.news.adapter.TabFragmentAdapter;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.channel.ChannelIdManager;
import com.zhimu.news.channel.ChannelItem;
import com.zhimu.news.fragment.ChannelFragmentFactory;
import com.zhimu.news.utils.NetUtils;
import com.zhimu.news.utils.SharedUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.waps.AppConnect;

/**
 * 新闻主页
 * Created by Administrator on 2016.2.28.
 */
public class TabHomeActivity extends BaseActivity implements View.OnClickListener {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private static final int HOME_REQUESTCODE = 1;

    /**
     * 第一次按back健时的时间
     */
    private long mExitTime;
    private List<Fragment> fragmentLists = new ArrayList<>();

    // 从数据库中获取tab栏目数据
    private List<ChannelItem> userChannelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_home);

        initView();

        initData();

        // 检查网络：activity第一次创建时，检查网络，后续并没有实时检查网络，该怎么实现？
        checkedNetwork();

        // 检查版本
        initCheckVersion();

    }

    /**
     * 初始化控件
     */
    private void initView() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) findViewById(R.id.toolbar_title);

        // 添加更多item图片按钮
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        ImageView iv_add_item = (ImageView) findViewById(R.id.iv_add_item);
        viewPager = (ViewPager) findViewById(R.id.view_viewpager);

        FloatingActionButton fab_tab_home = (FloatingActionButton) findViewById(R.id.fab_tab_home);
        if (fab_tab_home != null) {
            fab_tab_home.setVisibility(View.GONE);
            fab_tab_home.setOnClickListener(this);
        }

        // 这里并没有做处理，改为用dialog
        TextView tv_net_setting = (TextView) findViewById(R.id.tv_net_setting);
        if (tv_net_setting != null) {
            tv_net_setting.setVisibility(View.GONE);
            tv_net_setting.setOnClickListener(this);
        }

        if (iv_add_item != null) {
            iv_add_item.setOnClickListener(this);
        }

        // 主页不设置标题
        if (mTitle != null) {
            mTitle.setVisibility(View.GONE);
        }

        if (mToolbar != null) {
            mToolbar.setTitle(getResources().getString(R.string.app_name));
            mToolbar.setTitleTextColor(Color.WHITE);
            setSupportActionBar(mToolbar);
        }

    }

    /**
     * 初始化各频道fragment
     */
    private void initData() {

        // 清空频道fragment
        fragmentLists.clear();

        // 获取tab
        userChannelList = ChannelIdManager.getInstance(MyApplication.getInstance().getSQLHelper()).getUserChannel();
        for (int i = 0; i < userChannelList.size(); i++) {
            // 获取当前position的title
            ChannelItem item = userChannelList.get(i);
            // 新闻标题
            String title = item.getName();
            // 添加fragment
            Fragment fragment = ChannelFragmentFactory.createFragment(title);
            fragmentLists.add(fragment);

        }

        // 添加适配器
        TabFragmentAdapter adapter = new TabFragmentAdapter(fragmentLists, getSupportFragmentManager(), userChannelList);
        // ViewPage监听:实际加载2页，
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);

        // 将ViewPager和TabLayout绑定
        tabLayout.setupWithViewPager(viewPager);

    }

    /**
     * 检查版本
     */
    private void initCheckVersion() {
        // 蒲公英SDK：版本更新：如果设置了更新通知，是true，不自动更新,更新信息内容，需要自己写
        if (!SharedUtils.getSharedBoolean(this, SharedUtils.ISUPDATEAPP, false)) {
            PgyUpdateManager.register(this, new UpdateManagerListener() {
                @Override
                public void onNoUpdateAvailable() {

                }

                @Override
                public void onUpdateAvailable(String result) {
                    // 将新版本信息封装到AppBean中
                    final AppBean appBean = getAppBeanFromString(result);
                    new AlertDialog.Builder(TabHomeActivity.this)
                            .setTitle(R.string.set_updated)
                            .setMessage(R.string.set_version_message)
                            .setNegativeButton(R.string.set_positive_btn,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startDownloadTask(
                                                    TabHomeActivity.this,
                                                    appBean.getDownloadURL());
                                        }
                                    })
                            .show();
                }
            });
        }
    }

    /**
     * 点击控件监听
     *
     * @param v 点击控件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_item:
                // 点击跳转到频道管理界面，并返回数据
                Intent intentChannel = new Intent(TabHomeActivity.this, ChannelIdActivity.class);
                startActivityForResult(intentChannel, HOME_REQUESTCODE);
                finish();
                break;
//            case R.id.tv_net_setting:
//                // 设置网络
//                intoSetNet();
//                break;
//            case R.id.fab_tab_home:
//                // 重新初始化数据
//                initData();
//                break;
            default:
                break;
        }
    }

    /**
     * 设置网络
     */
    private void intoSetNet() {
        Intent intentNet;
        /**
         * 跳转设置wifi界面：判断手机版本，3.0版本设置的方法不一样
         */
        if (Build.VERSION.SDK_INT > 10) {
            intentNet = new Intent(Settings.ACTION_WIFI_SETTINGS);
        } else {
            intentNet = new Intent();
            ComponentName componentName = new ComponentName("com.android.settings", "com.android.setting.WirelessSettings");
            intentNet.setComponent(componentName);
            intentNet.setAction("android.intent.action.View");
        }
        startActivity(intentNet);

    }

    /**
     * 判断网络
     */
    public void checkedNetwork() {

        if (!NetUtils.isNetConnected(this)) {

            // 弹出对话框
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_set_net)
                    .setMessage(R.string.dialog_set_net_message)
                    .setNegativeButton(R.string.dialog_negative,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                    .setPositiveButton(R.string.dialog_positive,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 跳转设置网络界面
                                    intoSetNet();
                                }
                            })
                    .show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 判断结果码
        if (requestCode == HOME_REQUESTCODE) {
            if (resultCode == ChannelIdActivity.CHANNEL_RESULTCODE) {

                initData();
            }
        }

    }

    /**
     * 退出提示
     *
     * @param keyCode 按健
     * @param event   事件
     * @return true
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, R.string.tab_home_exit, Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }

            return true;
        }
        // 拦截MENU按钮点击事件，无任何操作
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 关闭万普广告统计数据
        AppConnect.getInstance(this).close();
    }

    /**
     * 创建menu视图
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tab_home, menu);
        return true;
    }

    /**
     * menu点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.action_tab_search:
                // 搜索
                intent = new Intent(this, SearchActivity.class);
                break;
            case R.id.action_tab_collect:
                // 收藏
                intent = new Intent(this, MyCollectActivity.class);
                break;
            case R.id.action_tab_picture:
                // 图片
                intent = new Intent(this, MyImageActivity.class);
                break;
            case R.id.action_tab_setting:
                // 设置
                intent = new Intent(this, SettingActivity.class);
                break;
            default:
                break;
        }

        startActivity(intent);

        return true;
    }

    /**
     * 让menu的图标显示出来
     */
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.onPrepareOptionsPanel(view, menu);
    }

}
