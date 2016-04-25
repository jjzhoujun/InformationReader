package com.zhimu.news;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhimu.news.adapter.CollectAdapter;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.dao.DaoMaster;
import com.zhimu.news.dao.DaoSession;
import com.zhimu.news.dao.NewsCollect;
import com.zhimu.news.dao.NewsCollectDao;
import com.zhimu.news.dao.NewsCollectImgUrl;
import com.zhimu.news.dao.NewsCollectImgUrlDao;
import com.zhimu.news.divider.HorizontalDividerItemDecoration;
import com.zhimu.news.fragment.EmptyFragment;
import com.zhimu.news.impl.OnClickRecyclerItemListener;

import java.util.List;

/**
 * 我的收藏界面:点击删除时，有时数据库显示“删除失败”
 * <p>
 * 2016-03-21
 */
public class MyCollectActivity extends BaseActivity implements OnClickRecyclerItemListener, EmptyFragment.OnMyImageFragmentListener {

    private RecyclerView recyclerView;
    private CollectAdapter adapter;

    private NewsCollectDao collectDao;
    private NewsCollectImgUrlDao newsCollectImgUrlDao;
    private List<NewsCollect> lists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycollect);

        // 打开数据库
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(this, "collects", null);
        // 打开数据库
        SQLiteDatabase database = devOpenHelper.getWritableDatabase();
        // 检查数据库的更新和创建
        DaoMaster daoMaster = new DaoMaster(database);
        // 注册实体数据
        DaoSession daoSession = daoMaster.newSession();
        // 新闻详情：数据操作
        collectDao = daoSession.getNewsCollectDao();
        // 图片链接
        newsCollectImgUrlDao = daoSession.getNewsCollectImgUrlDao();

        initView();

        initData();

    }

    /**
     * 初始化View
     */
    private void initView() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) findViewById(R.id.toolbar_title);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        if (mTitle != null) {
            mTitle.setText(R.string.my_collect_title);
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

        // 设置RecyclerView属性
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
            // 添加分隔线
            recyclerView.addItemDecoration(
                    new HorizontalDividerItemDecoration.Builder(this)
                            .marginResId(R.dimen.dimen_10, R.dimen.dimen_10)
                            .build());
            recyclerView.setLayoutManager(linearLayoutManager);
        }

    }

    /**
     * 点击返回首页
     */
    private void intoHomeActivity() {
        // 从栈中提到顶端，不是默认的重新创建
        Intent intent = new Intent(MyCollectActivity.this, TabHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    /**
     * 初始化数据
     */
    private void initData() {

        // 查询数据库中的所有数据
        lists = collectDao.queryBuilder().build().list();

        // 个数是0个，替换
        if (lists.size() == 0) {
            // 替换成fragment
            EmptyFragment myImageFragment = new EmptyFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.my_collect_frame, myImageFragment).commit();

            return;
        }

        // 绑定数据
        adapter = new CollectAdapter(this, lists);
        recyclerView.setAdapter(adapter);

        // adapter注册监听
        adapter.setItemClickListener(this);

    }

    /**
     * 删除时回调，同时删除数据库中的数据
     *
     * @param view     删除图标
     * @param position 当前值
     */
    @Override
    public void onItemDeleteImageViewClick(View view, int position) {

        // 删除list中的数据
        adapter.deleteData(position);

        List<NewsCollect> newsCollects = collectDao.loadAll();
        // 删除之前所有个数
        long beforeDelete = collectDao.count();
        // 删除数据库中的数据
        collectDao.delete(newsCollects.get(position));
        // 重新查询数据库中的所有数据
        long afterDelete = collectDao.count();

        if (afterDelete < beforeDelete) {
            Toast.makeText(MyCollectActivity.this, R.string.my_collect_toast_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MyCollectActivity.this, R.string.my_collect_toast_fail, Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 点击跳转
     *
     * @param view     视图控件
     * @param position 当前值
     */
    @Override
    public void onItemClick(View view, int position) {

        // 获取当前值对象
        NewsCollect newsCollect = lists.get(position);
        String title = newsCollect.getTitle();
        String source = newsCollect.getSource();
        String link = newsCollect.getLink();
        String content = newsCollect.getHtml();
        String date = newsCollect.getPubDate();
        Boolean isImgUrl = newsCollect.getIsImgUrl();

        // 从1开始
        int size = 1;
        String imgUrl = null;

        if (isImgUrl) {
            // 查询另一张表中的数据，以便得到图片
            List<NewsCollectImgUrl> newsCollectImgUrls = newsCollectImgUrlDao.queryBuilder()
                    .where(NewsCollectImgUrlDao.Properties.Title.eq(title)).build().list();
            size = newsCollectImgUrls.size();

            imgUrl = newsCollectImgUrls.get(0).getImgUrl();
        }

        // 传递数据
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("source", source);
        bundle.putString("url_link", link);
        bundle.putString("html", content);
        bundle.putString("pub_date", date);
        bundle.putString("imgUrl", imgUrl);

        Intent intent;
        if (size > 1) {
            intent = new Intent(MyCollectActivity.this, NewsCollectMoreImageActivity.class);
        } else {
            intent = new Intent(MyCollectActivity.this, NewsCollectDetailedActivity.class);
        }
        intent.putExtras(bundle);
        startActivity(intent);

    }

    @Override
    public void onCheckFragmentButton() {
        intoHomeActivity();
    }
}
