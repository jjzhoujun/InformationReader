package com.zhimu.news;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
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
import com.zhimu.news.utils.LoadImageUtils;
import com.zhimu.news.utils.SharedUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 当图片大于2个时，采用这个界面显示新闻内容和图片
 */
public class NewsMoreImageActivity extends BaseActivity {

    private ViewPager viewPager;
    private TextView tv_more_news_title;
    private TextView tv_more_news_content;

    private List<NewsBean.ImageUrl> imgUrls;
    private String title;
    private String html;
    private String source;
    private String desc;
    private String link;

    private DisplayImageOptions options;

    // 是否收藏：默认是true
    private boolean isCollect = true;

    // 图文显示
    private List<String> mList = new ArrayList<>();
    private ScrollView more_news_scrollview;
    private Toolbar mToolbar;
    private NewsCollectDao newsCollectDao;
    private NewsCollectImgUrlDao newsCollectImgUrlDao;

    // 标记
    public static final int NEWS_MORE_IMAGE_ACTIVITY = 30;

    private int where_fragment_activity;
    private boolean isCollected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_more_image);

        // 初始化图片设置
        options = MyApplication.getInstance().getOptions(R.mipmap.ic_big_bg);

        /**
         * 初始化数据库
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

        // 初始化控件
        initView();

        initData();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.more_image_tool);
        Spinner more_image_spinner = (Spinner) findViewById(R.id.more_image_spinner);
        // 滚动条
        more_news_scrollview = (ScrollView) findViewById(R.id.more_news_scrollview);
        viewPager = (ViewPager) findViewById(R.id.more_news_viewpager);
        // 标题
        tv_more_news_title = (TextView) findViewById(R.id.tv_more_news_title);
        // 内容
        tv_more_news_content = (TextView) findViewById(R.id.tv_more_news_content);
        // 提示
        TextView tv_news_prompt = (TextView) findViewById(R.id.tv_news_prompt);

        // 设置内容字体大小：取出保存的值，默认在xml中保存的是16sp
        int size = SharedUtils.getSharedInt(NewsMoreImageActivity.this, "size", 0);
        switch (size) {
            case 14:
                tv_more_news_content.setTextSize(size);
                break;
            case 18:
                tv_more_news_content.setTextSize(size);
                break;
            case 20:
                tv_more_news_content.setTextSize(size);
                break;
            default:
                break;
        }

        // 设置toolbar
        mToolbar.setTitle("");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(R.mipmap.back);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 导航上一页
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                // 如果从fragment过来，点击返回首页：从栈中提到顶端，不是默认的重新创建
                if (where_fragment_activity == NewsFragment.NEWS_FRAGMENT) {
                    intent = new Intent(NewsMoreImageActivity.this, TabHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                }

                // 如果从搜索界面过来
                if (where_fragment_activity == SearchActivity.SEARCH_ACTIVITY) {
                    intent = new Intent(NewsMoreImageActivity.this, SearchActivity.class);
                }
                startActivity(intent);
                finish();
            }
        });

        // 点击查看原网页
        if (tv_news_prompt != null) {
            tv_news_prompt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NewsMoreImageActivity.this, NewsWebActivity.class);
                    intent.putExtra("url_link", link);
                    intent.putExtra("where_activity", NEWS_MORE_IMAGE_ACTIVITY);
                    startActivity(intent);
                }
            });
        }

        // 设置Spinner下拉列表控件
        final String spinnerText = getResources().getString(R.string.news_more_spinner_title);
        final String spinnerImage = getResources().getString(R.string.news_more_spinner_image);
        // 文字+图片
        mList.add(spinnerText);
        mList.add(spinnerImage);

        // 添加数据
        final ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, R.layout.spinner_txt, mList);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (more_image_spinner != null) {
            more_image_spinner.setAdapter(mAdapter);
            more_image_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    // 判断
                    String item = mAdapter.getItem(position);
                    if (item.equals(spinnerImage)) {

                        // 滚动条及滚动条下的所有控件都消失
                        more_news_scrollview.setVisibility(View.GONE);

                    }

                    if (item.equals(spinnerText)) {

                        more_news_scrollview.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    /**
     * 数据
     */
    private void initData() {
        Bundle bundle = getIntent().getExtras();

        // 是哪个界面穿过来的值
        where_fragment_activity = bundle.getInt("where_fragment_activity");

        link = bundle.getString("url_link");
        title = bundle.getString("title");
        source = bundle.getString("source");
        html = bundle.getString("html");
        desc = bundle.getString("desc");
        imgUrls = (List<NewsBean.ImageUrl>) bundle.getSerializable("image_url");

        // 设置toolbar标题
        if (imgUrls != null) {
            mToolbar.setTitle(1 + "/" + imgUrls.size());
        }
        // 新闻标题
        tv_more_news_title.getPaint().setFakeBoldText(true);
        tv_more_news_title.setText(title);
        // 内容
        Spanned spanned = Html.fromHtml(html);
        tv_more_news_content.setText(spanned);

        // 滑动监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            /**
             * 滑动到哪一个位置
             * @param position 当前值
             */
            @Override
            public void onPageSelected(int position) {

                mToolbar.setTitle((position + 1) + "/" + imgUrls.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // 绑定adapter
        MoreImageNewsAdapter adapter = new MoreImageNewsAdapter();
        viewPager.setAdapter(adapter);

    }

    /**
     * 创建menu视图
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_collect, menu);

        MenuItem item = menu.findItem(R.id.action_news_collect);
        // 获取数据库中的所有数据
        List<NewsCollect> newsCollects = newsCollectDao.queryBuilder().build().list();
        for (int i = 0; i < newsCollects.size(); i++) {
            String titleCollect = newsCollects.get(i).getTitle();
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
     *
     * @param item
     * @return
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
                Toast.makeText(NewsMoreImageActivity.this, R.string.news_detailed_toast_collect, Toast.LENGTH_SHORT).show();
            } else {
                // 保存到数据库
                saveCollect();

                // 设置为黄色图标
                item.setIcon(R.mipmap.my_collected_yellow);

                isCollected = true;
                Toast.makeText(NewsMoreImageActivity.this, R.string.news_detailed_toast_collect_success, Toast.LENGTH_SHORT).show();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存到数据库
     */
    private void saveCollect() {

        // 时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date = new Date(System.currentTimeMillis());
        String pubDate = dateFormat.format(date);

        // 插入数据
        NewsCollect newsCollect = new NewsCollect(null, title, source, desc, html, pubDate, link, true);

        // 打开另一张表，插入图片链接
        for (int i = 0; i < imgUrls.size(); i++) {
            String imgUrl = imgUrls.get(i).getUrl();
            NewsCollectImgUrl newsCollectImgUrl = new NewsCollectImgUrl(null, title, imgUrl);
            newsCollectImgUrlDao.insert(newsCollectImgUrl);
        }

        newsCollectDao.insert(newsCollect);

    }

    /**
     * 适配器
     */
    class MoreImageNewsAdapter extends PagerAdapter {

        /**
         * 设置int最大值，可以循环滑动:这里仅设置所需要的图片数量
         *
         * @return list集合个数
         */
        @Override
        public int getCount() {
            return imgUrls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /**
         * 类似于BaseAdapter的getView()方法
         * 将数据设置给View
         * 返回当前页
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            // viewpager每一页是图片展示
            View view = LayoutInflater.from(getBaseContext()).inflate(R.layout.news_more_image_viewpager, container, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.more_news_image);

            final String imgUrl = imgUrls.get(position).getUrl();
            // 加载网络图片
            ImageLoader.getInstance().displayImage(imgUrl,
                    imageView,
                    options,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            super.onLoadingFailed(imageUri, view, failReason);

                            Toast.makeText(NewsMoreImageActivity.this, R.string.news_more_toast_img_fail, Toast.LENGTH_SHORT).show();
                        }
                    });

            // 保存图片
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    LoadImageUtils.getInit().saveImage(NewsMoreImageActivity.this, imgUrl);
                    return true;
                }
            });

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
