package com.zhimu.news.app;

import android.app.Application;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.pgyersdk.crash.PgyCrashManager;
import com.zhimu.news.channel.SQLHelper;

import java.io.File;

/**
 * API接口初始化
 * Created by Administrator on 2016.2.9.
 */
public class MyApplication extends Application {

    private static MyApplication application;
    private SQLHelper sqlHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        initImageLoad();

        // 注册蒲公英SDK:可捕获异常
        PgyCrashManager.register(this);

    }

    /**
     * 饿汉单例模式：有且仅有一个实例
     * @return application
     */
    public static MyApplication getInstance() {
        return application;
    }

    /**
     * 获取数据库Helper
     */
    public SQLHelper getSQLHelper() {
        if (sqlHelper == null)
            sqlHelper = new SQLHelper(application);
        return sqlHelper;
    }

    @Override
    public void onTerminate() {
        //整体摧毁的时候调用这个方法
        if (sqlHelper != null)
            sqlHelper.close();
        super.onTerminate();
    }

    /**
     * 初始化图片ImageLoad,配置图片加载器参数
     */
    private void initImageLoad() {
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getApplicationContext());
        // 创建缓存文件
        File cacheDir = StorageUtils.getOwnCacheDirectory(getApplicationContext(), "ZhiMu/CacheImage");
        // 保存到自定义的缓存路径
        builder.diskCache(new UnlimitedDiskCache(cacheDir));
        // 线程池内加载的数量
        builder.threadPoolSize(3);
        // 缓存的文件数量
        builder.diskCacheFileCount(200);
        // 开始创建
        ImageLoaderConfiguration configuration = builder.build();
        // 初始化图片加载配置参数
        ImageLoader.getInstance().init(configuration);
    }

    /**
     * 获取缓存图片配置；新闻首页,穿入需要默认显示的图片；各个位置的图片不一样
     */
    public DisplayImageOptions getOptions(int imageRes) {

            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
            // 设置图片在下载期间显示的图片
            builder.showImageOnLoading(imageRes);
            // 设置图片加载/解码过程中错误时候显示的图片
            builder.showImageOnFail(imageRes);
            // 设置下载的图片是否缓存在内存中
            builder.cacheInMemory(true);
            // 缓存在磁盘中:但读取速度慢
            builder.cacheOnDisk(true);
            // 设置图片以如何的编码方式显示:EXACTLY_STRETCHED:图片会缩放到目标大小完全;EXACTLY :图像将完全按比例缩小的目标大小
            builder.imageScaleType(ImageScaleType.EXACTLY_STRETCHED);
            // 设置图片的解码类型
            builder.bitmapConfig(Bitmap.Config.RGB_565);
            // 设置图片在下载前是否重置，复位
            builder.resetViewBeforeLoading(true);
            //是否图片加载好后渐入的动画时间
//        builder.displayer(new FadeInBitmapDisplayer(100));
            return builder.build();

    }

}
