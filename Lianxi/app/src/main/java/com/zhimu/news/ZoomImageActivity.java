package com.zhimu.news;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.view.ZoomImageView;

import java.util.ArrayList;

/**
 * 图片放大界面
 */
public class ZoomImageActivity extends BaseActivity {

    private ArrayList<String> imgLists;
    private ViewPager mViewPager;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        options = MyApplication.getInstance().getOptions(R.mipmap.no_pictures);
        imageLoader = ImageLoader.getInstance();

        initView();

        initData();

    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.my_image_viewpager);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);

        // toolbar首次初始化：消失
        if (mToolbar != null) {
            // 背景设为黑色透明色
            mToolbar.setBackgroundColor(getResources().getColor(R.color.zoom_tool_bg));
            mToolbar.setTitle(R.string.zoom_image_tool_title);
            mToolbar.setTitleTextColor(Color.WHITE);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // 根据不同值导航到上一页
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    intoMyImgActivity();
                    finish();

                }
            });
        }

    }

    private void intoMyImgActivity() {
        Intent intent = new Intent(ZoomImageActivity.this, MyImageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

    }

    private void initData() {

        // 判断有没有sd卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.zoom_image_tool_sd, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = getIntent();
        imgLists = intent.getStringArrayListExtra("my_images");
        // 当前点击了第几个图片
        int imagePosition = intent.getIntExtra("image", 0);

        // 获取屏幕分辨率
        mViewPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        mViewPager.setAdapter(new ZoomImageAdapter());
        // 点击了第几个图片就指定viewpager显示第几张图片
        mViewPager.setCurrentItem(imagePosition);
    }

    @Override
    public void onBackPressed() {
        intoMyImgActivity();

        super.onBackPressed();

    }

    /**
     * 图片处理适配器
     */
    class ZoomImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imgLists.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            final ZoomImageView view = new ZoomImageView(ZoomImageActivity.this);
            // 设置可以缩放
            view.enable();
            // 加载本地图片:还可以设置一个监听事件
            imageLoader.displayImage(imgLists.get(position), view, options);
            // 把view添加到活动页
            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}
