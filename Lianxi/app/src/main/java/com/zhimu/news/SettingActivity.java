package com.zhimu.news;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.pgyersdk.activity.FeedbackActivity;
import com.pgyersdk.feedback.PgyFeedback;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.dao.DaoMaster;
import com.zhimu.news.dao.DaoSession;
import com.zhimu.news.dao.NewsCollectDao;
import com.zhimu.news.dao.NewsCollectImgUrlDao;
import com.zhimu.news.utils.FileSizeUtils;
import com.zhimu.news.utils.SetTextSize;
import com.zhimu.news.utils.SharedUtils;
import com.zhimu.news.view.GroupItemView;

import java.io.File;

import cn.waps.AppConnect;
import cn.waps.UpdatePointsNotifier;

/**
 * 设置界面:实现了万普广告查看积分接口，可以去除，不需要
 * 2016-04-19
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener, UpdatePointsNotifier {

    private GroupItemView checkVersionView;
    private GroupItemView textSizeView;
    private GroupItemView myImgView;
    private GroupItemView myCollectView;
    private GroupItemView integrateView;
    private GroupItemView feedView;
    private GroupItemView helpView;
    private SwitchCompat switchCompatView;
    private Toolbar mToolbar;

    private GroupItemView cacheDataView;

    private NewsCollectDao newsCollectDao;
    private NewsCollectImgUrlDao newsCollectImgUrlDao;


    // 记录积分数量
    private int my_integrate = 0;

    /**
     * 这样做，是否合适？
     */

    /**
     * 我的图片
     */
    private Handler mImgFileHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            boolean isDeleteSuccess = (boolean) msg.obj;

            if (isDeleteSuccess) {
                // 通知图库更新，删除遗留后的缩略图
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ZhiMu/MyImage";
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(new File(filePath)));
                sendBroadcast(intent);

                Toast.makeText(SettingActivity.this, R.string.set_delete_toast_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingActivity.this, R.string.set_delete_toast_fail, Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 图片缓存
     */
    private Handler mCacheImgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            double cacheDataSize = (double) msg.obj;

            if (cacheDataSize < 0.001) {
                Toast.makeText(SettingActivity.this, R.string.set_delete_toast_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingActivity.this, R.string.set_delete_toast_fail, Toast.LENGTH_SHORT).show();
            }
        }

    };

    /**
     * 我的收藏
     */
    private Handler mCollectHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle bundle = msg.getData();
            long new_collect = bundle.getLong("new_collect");
            long news_img_url = bundle.getLong("news_img_url");

            // 等于0个，删除成功
            if (new_collect == 0 && news_img_url == 0) {
                Toast.makeText(SettingActivity.this, R.string.set_delete_toast_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingActivity.this, R.string.set_delete_toast_fail, Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 万普获取积分：注册监听接口
        AppConnect.getInstance(this).getPoints(this);

        initSQl();

        initView();

        initData();
    }

    /**
     * 初始化数据库
     */
    private void initSQl() {
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(this, "collects", null);
        // 打开数据库
        SQLiteDatabase database = devOpenHelper.getWritableDatabase();
        // 检查数据库的更新和创建
        DaoMaster daoMaster = new DaoMaster(database);
        // 注册实体数据
        DaoSession daoSession = daoMaster.newSession();
        // 新闻详情：数据操作
        newsCollectDao = daoSession.getNewsCollectDao();
        // 新闻图片链接：数据操作
        newsCollectImgUrlDao = daoSession.getNewsCollectImgUrlDao();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) findViewById(R.id.toolbar_title);
        switchCompatView = (SwitchCompat) findViewById(R.id.set_update_notify_switch);
        checkVersionView = (GroupItemView) findViewById(R.id.set_check_version);
        textSizeView = (GroupItemView) findViewById(R.id.set_text_size);
        // 我的图片
        myImgView = (GroupItemView) findViewById(R.id.set_my_img);
        // 我的收藏
        myCollectView = (GroupItemView) findViewById(R.id.set_my_collect);
        // 缓存数据
        cacheDataView = (GroupItemView) findViewById(R.id.set_cache_data);
        // 我的积分
        integrateView = (GroupItemView) findViewById(R.id.set_my_integrate);
        feedView = (GroupItemView) findViewById(R.id.set_feed);
        helpView = (GroupItemView) findViewById(R.id.set_help);

        if (mTitle != null) {
            mTitle.setText(R.string.set_title);
        }

        mToolbar.setTitle("");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void initData() {

        // 检查版本更新
        checkVersionView.setOnClickListener(this);
        textSizeView.setOnClickListener(this);
        myImgView.setOnClickListener(this);
        myCollectView.setOnClickListener(this);
        cacheDataView.setOnClickListener(this);
        integrateView.setOnClickListener(this);
        feedView.setOnClickListener(this);
        helpView.setOnClickListener(this);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击返回首页：从栈中提到顶端，不是默认的重新创建
                Intent intent = new Intent(SettingActivity.this, TabHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }

        });

        // 是否自动更新
        switchCompatView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // 默认打开状态：灰色变化
                    switchCompatView.setChecked(true);
                    SharedUtils.setSharedBoolean(SettingActivity.this, SharedUtils.ISUPDATEAPP, true);
                    Toast.makeText(SettingActivity.this, R.string.set_switch_texton_toast, Toast.LENGTH_SHORT).show();
                } else {
                    switchCompatView.setChecked(false);
                    SharedUtils.setSharedBoolean(SettingActivity.this, SharedUtils.ISUPDATEAPP, false);
                    Toast.makeText(SettingActivity.this, R.string.set_switch_textoff_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_check_version:
                // 点击检查版本，弹出对话框
                showCheckVersion();
                break;
            case R.id.set_text_size:
                // 字体大小
                SetTextSize.setTextSizeDialog(this);
                break;
            case R.id.set_my_img:
                // 清空我的图片
                deleteImgFile();
                break;
            case R.id.set_my_collect:
                // 清空我的收藏：数据库大小
                deleteCollect();
                break;
            case R.id.set_cache_data:
                // 清空缓存数据
                deleteCacheData();
                break;
            case R.id.set_my_integrate:
                // 我的积分
                showMyIntegrate();
                break;
            case R.id.set_feed:
                // 意见反馈
                showFeed();
                break;
            case R.id.set_help:
                // 关于掌中小报
                Intent intent = new Intent(SettingActivity.this, ExplainCopyActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 检查版本
     */
    private void showCheckVersion() {
        PgyUpdateManager.register(this, new UpdateManagerListener() {
            @Override
            public void onNoUpdateAvailable() {
                Toast.makeText(SettingActivity.this, R.string.set_no_new_version, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpdateAvailable(String result) {

                // 将新版本信息封装到AppBean中:json格式写法
                final AppBean mAppBean = getAppBeanFromString(result);
                // 弹出对话框
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle(R.string.set_updated)
                        .setMessage(R.string.set_version_message)
                        .setPositiveButton(R.string.set_positive_btn,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 下载
                                        startDownloadTask(SettingActivity.this, mAppBean.getDownloadURL());
                                    }
                                }).show();

            }
        });
    }

    /**
     * 清空我的图片
     */
    private void deleteImgFile() {
        // 判断有没有sd卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.set_toast_no_find_sd, Toast.LENGTH_SHORT).show();
            return;
        }

        final FileSizeUtils imgFileSize = new FileSizeUtils();
        final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ZhiMu/MyImage";
        final String imgFile = imgFileSize.getAutoFileOrFilesSize(filePath);

        new AlertDialog.Builder(SettingActivity.this)
                .setTitle(R.string.set_delete_myimg_dialog_title)
                .setMessage(getResources().getString(R.string.set_myimg_dialog_size) +
                        imgFile + getResources().getString(R.string.delete_positive))
                .setPositiveButton(R.string.set_positive_btn,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        // 删除该文件夹下的所有图片
                                        boolean isDeleteSuccess = imgFileSize.deleteAllImgFile(filePath);
                                        // 传布尔值，判断是否删除成功
                                        Message message = Message.obtain();
                                        message.obj = isDeleteSuccess;
                                        mImgFileHandler.sendMessage(message);

                                    }
                                }).start();

                            }
                        })
                .show();
    }

    /**
     * 清空我的收藏：数据库中的数据
     */
    private void deleteCollect() {

        @SuppressLint("SdCardPath") String filePath = "/data/data/com.zhimu.news/databases/collects";
        // 数据库中的路径地址
//        String filePath = getFilesDir().getPath() + "/com.zhimu.news/databases/collects";
        // 清空缓存:数据库文件可能很大，本身占空间，不为0,约值为20k
        FileSizeUtils fileSizeUtils = new FileSizeUtils();
        // 怎么优化20k？可以不判断文件本身大小，只判断数据库中存有数据的大小，并没有删除数据库文件
        String autoFileOrFilesSize = fileSizeUtils.getAutoFileOrFilesSize(filePath);

        new AlertDialog.Builder(SettingActivity.this)
                .setTitle(R.string.set_delete_mycollect_dialog_title)
                .setMessage(getResources().getString(R.string.my_collect_dialog_title) + autoFileOrFilesSize +
                        getResources().getString(R.string.delete_positive))
                .setPositiveButton(R.string.set_positive_btn,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        // 删除所有数据
                                        newsCollectDao.deleteAll();
                                        newsCollectImgUrlDao.deleteAll();

                                        // 再次查询
                                        long count = newsCollectDao.count();
                                        long count1 = newsCollectImgUrlDao.count();

                                        // 传递两个长整型值
                                        Message message = Message.obtain();
                                        Bundle bundle = new Bundle();
                                        bundle.putLong("news_collect", count);
                                        bundle.putLong("news_img_url", count1);
                                        message.setData(bundle);
                                        mCollectHandler.sendMessage(message);
                                    }
                                }).start();
                            }
                        })
                .show();
    }

    /**
     * 清空缓存数据
     */
    private void deleteCacheData() {
        // 判断有没有sd卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.set_toast_no_find_sd, Toast.LENGTH_SHORT).show();
            return;
        }

        // 清空缓存
        final FileSizeUtils cacheFileSize = new FileSizeUtils();
        // 绝对路径
        final String cacheFile = cacheFileSize.getAutoFileOrFilesSize(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/ZhiMu/CacheImage");

        new AlertDialog.Builder(SettingActivity.this)
                .setTitle(R.string.set_cache_img_title)
                .setMessage(getResources().getString(R.string.set_cache_data_size) +
                        cacheFile + getResources().getString(R.string.delete_positive))
                .setPositiveButton(R.string.set_positive_btn,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // 开启子线程删除:耗时操作
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        // 清空磁盘、内存中的缓存图片
                                        ImageLoader.getInstance().getDiskCache().clear();
                                        ImageLoader.getInstance().getMemoryCache().clear();

                                        // 绝对路径，再次查找，检查是否删除干净
                                        double cacheDataSize = cacheFileSize.getFileAndFileSize(
                                                cacheFile,
                                                FileSizeUtils.TYPE_B);

                                        Message message = Message.obtain();
                                        message.obj = cacheDataSize;
                                        mCacheImgHandler.sendMessage(message);

                                    }
                                }).start();

                            }
                        })
                .show();

    }

    /**
     * 我的积分：万普广告积分，另开一个activity可以花费积分，兑换东西
     */
    private void showMyIntegrate() {

        new AlertDialog.Builder(SettingActivity.this)
                .setTitle(R.string.set_my_integrate)
                .setMessage(getResources().getString(R.string.set_see_my_integrate) + my_integrate +
                        getResources().getString(R.string.set_spend_my_integrate))
                .setPositiveButton(R.string.set_positive_btn,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Something
                                // 点击确定，花费积分，花费积分兑换什么东西，自己决定,不在这里调用方法，否则会返回首页
//                                AppConnect.getInstance(SettingActivity.this).spendPoints(my_integrate, SettingActivity.this);
                            }
                        })
                .setNegativeButton(R.string.set_text_size_negative,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                .show();
    }

    /**
     * 打开意见反馈界面
     */
    private void showFeed() {
        // 打开沉浸式,默认为false
        FeedbackActivity.setBarImmersive(true);
        // 设置顶部导航栏和底部bar的颜色
        FeedbackActivity.setBarBackgroundColor("#EE3333");
        // 设置顶部按钮和底部按钮按下时的反馈色
        FeedbackActivity.setBarButtonPressedColor("#FF0000");
        // 设置颜色选择器的背景色:灰白色
        FeedbackActivity.setColorPickerBackgroundColor("#FEFEFE");
        PgyFeedback.getInstance().showActiivty(this);
    }

    /**
     * 万普积分获取成功回调方法
     *
     * @param s 积分名称
     * @param i 数量
     */
    @Override
    public void getUpdatePoints(String s, final int i) {

        my_integrate = i;

    }

    /**
     * 万普积分获取失败回调方法
     *
     * @param s 积分名称
     */
    @Override
    public void getUpdatePointsFailed(String s) {

    }

    @Override
    protected void onDestroy() {

        // 传null，清除所有消息，避免内存泄漏
        mImgFileHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

}
