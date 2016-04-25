package com.zhimu.news;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhimu.news.adapter.MyImageAdapter;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.fragment.EmptyFragment;
import com.zhimu.news.impl.OnClickRecyclerImageListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyImageActivity extends BaseActivity implements EmptyFragment.OnMyImageFragmentListener {

    private RecyclerView recyclerView;

    // 图片路径
    private List<String> imgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_image);

        initView();

        initImages();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) findViewById(R.id.toolbar_title);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // 设置RecyclerView属性
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            StaggeredGridLayoutManager staggeredGridLayoutManager
                    = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
        }

        if (mTitle != null) {
            mTitle.setText(R.string.my_img_title);
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

                    intoHomeActivity();
                }

            });
        }


    }

    /**
     * 扫描手机中的指定文件图片
     */
    private void initImages() {

        // 判断有没有sd卡
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, R.string.my_img_no_find_sd, Toast.LENGTH_SHORT).show();

            // 替换成fragment
            initFragment();

            return;
        }

        /**
         * 根据路径获取该文件夹下的所有图片文件的路径
         * 需不需要开一个异步线程？
         *
         */
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ZhiMu/MyImage";
        // 获取的是sd卡的绝对路径
        File file = new File(filePath);
        // 判断文件夹是否存在
        if (file.exists()) {
            // 判断文件是否是一个目录
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                // 判断文件是否为空
                if (files.length > 0) {
                    for (File fileImg : files) {
                        String fileImgPath = fileImg.getPath();
                        // ImageLoader加载前面需要加file://
                        imgList.add("file://" + fileImgPath);
                    }
                }else {
                    // 替换成fragment
                    initFragment();
                }
            }
        } else {
            // 替换成fragment
            initFragment();
        }

        // 更新界面
        initUI();

    }

    /**
     * 更新界面
     */
    private void initUI() {

        MyImageAdapter myImageAdapter = new MyImageAdapter(MyImageActivity.this, imgList);
        recyclerView.setAdapter(myImageAdapter);

        // 点击放大图片
        myImageAdapter.setOnClickMyImageListener(new OnClickRecyclerImageListener() {
            @Override
            public void onItemImageClick(View view, int position) {

                Intent intent = new Intent(MyImageActivity.this, ZoomImageActivity.class);
                // 把list集合传过去
                intent.putStringArrayListExtra("my_images", (ArrayList<String>) imgList);
                // 把当前的position也传过去
                intent.putExtra("image", position);
                startActivity(intent);

            }
        });
    }

    /**
     * 替换fragment
     */
    private void initFragment() {
        EmptyFragment myImageFragment = new EmptyFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.my_img_frame, myImageFragment).commit();
    }

    /**
     * 实现接口，跳转首页
     */
    @Override
    public void onCheckFragmentButton() {
        intoHomeActivity();
    }

    /**
     * 跳转到首页
     */
    private void intoHomeActivity() {
        // 点击返回首页：从栈中提到顶端，不是默认的重新创建
        Intent intent = new Intent(MyImageActivity.this, TabHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }
}
