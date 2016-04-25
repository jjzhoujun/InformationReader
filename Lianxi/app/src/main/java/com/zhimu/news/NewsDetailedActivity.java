package com.zhimu.news;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.bean.NewsBean;
import com.zhimu.news.dao.DaoMaster;
import com.zhimu.news.dao.DaoSession;
import com.zhimu.news.dao.NewsCollect;
import com.zhimu.news.dao.NewsCollectDao;
import com.zhimu.news.dao.NewsCollectImgUrl;
import com.zhimu.news.dao.NewsCollectImgUrlDao;
import com.zhimu.news.fragment.NewsFragment;
import com.zhimu.news.utils.DateUtils;
import com.zhimu.news.utils.LoadImageUtils;
import com.zhimu.news.utils.SharedUtils;
import com.zhimu.news.view.ZoomImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 新闻详情展示界面
 */
public class NewsDetailedActivity extends BaseActivity {

    // 新闻内容
    private TextView tv_news_content;
    private TextView tv_news_title;
    private TextView tv_news_source;
    private ImageView iv_news_detailed_image;
    private TextView tv_news_prompt;

    private String link;
    private String html;
    private String title;
    private String source;
    private String desc;
    private List<NewsBean.ImageUrl> imgLists;

    // 跳转标记
    public static final int NEWS_DETAILED_ACTIVITY = 20;

    // 数据库操作
    private NewsCollectDao newsCollectDao;
    private NewsCollectImgUrlDao newsCollectImgUrlDao;
    private int where_fragment_activity;
    private Toolbar mToolbar;
    private boolean isCollected;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detailed);

        /**
         * 初始化数据库:第二个参数：数据库名称
         */
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

        options = MyApplication.getInstance().getOptions(R.mipmap.ic_big_bg);

        // 控件
        initView();
        // 数据
        initData();

    }

    /**
     * 初始化控件
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        tv_news_title = (TextView) findViewById(R.id.tv_news_title);
        tv_news_source = (TextView) findViewById(R.id.tv_news_source);
        // 开头图片
        iv_news_detailed_image = (ImageView) findViewById(R.id.iv_news_detailed_image);
        // 内容
        tv_news_content = (TextView) findViewById(R.id.tv_news_content);
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

        mToolbar.setTitle(R.string.news_detailed_tool_title);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    /**
     * 初始化数据
     */
    private void initData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        // 是哪个界面传过来的值
        where_fragment_activity = bundle.getInt("where_fragment_activity");

        link = bundle.getString("url_link");
        title = bundle.getString("title");
        source = bundle.getString("source");
        html = bundle.getString("html");
        String pub_date = bundle.getString("pub_date");
        desc = bundle.getString("desc");
        // 有可能是null
        imgLists = (List<NewsBean.ImageUrl>) bundle.getSerializable("image_url");

        // 标题：设置粗体显示
        tv_news_title.getPaint().setFakeBoldText(true);
        tv_news_title.setText(title);

        // 来源+时间：点击查看原网页
        if (pub_date != null) {
            String strSource = source + "   " +
                    pub_date.substring(5, 16) +
                    getResources().getString(R.string.news_detailed_source);
            tv_news_source.setText(strSource);
        }

        // Html内容：转成可显示的字符串
        Spanned fromHtml = Html.fromHtml(html);
        tv_news_content.setText(fromHtml);

        // 图片网络加载
        if (imgLists != null) {
            ImageLoader.getInstance().displayImage(
                    imgLists.get(0).getUrl(),
                    iv_news_detailed_image,
                    options,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            super.onLoadingFailed(imageUri, view, failReason);

                            Toast.makeText(NewsDetailedActivity.this, R.string.news_toast_load_img_fail, Toast.LENGTH_SHORT).show();
                        }
                    });
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

        // 下载图片
        iv_news_detailed_image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                LoadImageUtils.getInit().saveImage(NewsDetailedActivity.this, imgLists.get(0).getUrl());
                return true;
            }
        });

        // 导航上一页
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (where_fragment_activity == NewsFragment.NEWS_FRAGMENT) {
                    Intent intent = new Intent(NewsDetailedActivity.this, TabHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                }

                if (where_fragment_activity == SearchActivity.SEARCH_ACTIVITY) {
                    Intent intent = new Intent(NewsDetailedActivity.this, SearchActivity.class);
                    startActivity(intent);
                    finish();
                }

            }

        });
    }

    /**
     * 跳转到网页显示
     */
    private void intoWebNewsActivity() {
        Intent intent = new Intent(NewsDetailedActivity.this, NewsWebActivity.class);
        intent.putExtra("url_link", link);
        intent.putExtra("where_activity", NEWS_DETAILED_ACTIVITY);
        startActivity(intent);
    }

    /**
     * 创建menu视图
     *
     * @param menu 菜单item视图
     * @return true自己处理
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_collect, menu);

        MenuItem item = menu.findItem(R.id.action_news_collect);

        // 获取数据库中的所有数据
        List<NewsCollect> allNewsCollects = newsCollectDao.queryBuilder().build().list();
        for (int i = 0; i < allNewsCollects.size(); i++) {
            String titleCollect = allNewsCollects.get(i).getTitle();
            // 如果当前标题等于数据库中的某个标题，说明有保存过
            if (title.equals(titleCollect)) {
                // 设置为黄色图标
                item.setIcon(R.mipmap.my_collected_yellow);
            }
        }

        return true;
    }

    /**
     * 选中menu时回调
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_news_collect) {

            // 获取数据库中的所有数据
            List<NewsCollect> allNewsCollects = newsCollectDao.queryBuilder().build().list();
            for (int i = 0; i < allNewsCollects.size(); i++) {
                String titleCollect = allNewsCollects.get(i).getTitle();
                // 如果当前标题等于数据库中的某个标题，说明有保存过
                if (title.equals(titleCollect)) {
                    // 设置为黄色图标
                    item.setIcon(R.mipmap.my_collected_yellow);

                    isCollected = true;

                }
            }

            // 如果是false，说明没有收藏
            if (isCollected) {
                Toast.makeText(NewsDetailedActivity.this, R.string.news_detailed_toast_collect, Toast.LENGTH_SHORT).show();
            } else {
                // 保存到数据库
                saveCollect();

                isCollected = true;

                // 设置为黄色图标
                item.setIcon(R.mipmap.my_collected_yellow);
                Toast.makeText(NewsDetailedActivity.this, R.string.news_detailed_toast_collect_success, Toast.LENGTH_SHORT).show();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存到数据库
     */
    private void saveCollect() {
        // 时间
        String pubDate = new DateUtils().getDate("yyyy-MM-dd HH:mm:ss");

        // 判断集合中是否有元素
        NewsCollect newsCollect;
        if (imgLists != null && imgLists.size() > 0) {
            // 插入数据
            newsCollect = new NewsCollect(null, title, source, desc, html, pubDate, link, true);

            // 打开另一张表，插入图片链接
            for (int i = 0; i < imgLists.size(); i++) {
                String imgUrls = imgLists.get(i).getUrl();
                NewsCollectImgUrl newsCollectImgUrl = new NewsCollectImgUrl(null, title, imgUrls);
                newsCollectImgUrlDao.insert(newsCollectImgUrl);
            }

        } else {
            newsCollect = new NewsCollect(null, title, source, desc, html, pubDate, link, false);
        }
        newsCollectDao.insert(newsCollect);

    }

}
