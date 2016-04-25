package com.zhimu.news;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.dao.DaoMaster;
import com.zhimu.news.dao.DaoSession;
import com.zhimu.news.dao.NewsCollectImgUrl;
import com.zhimu.news.dao.NewsCollectImgUrlDao;
import com.zhimu.news.utils.LoadImageUtils;
import com.zhimu.news.utils.SharedUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻收藏：当图片大于2个时，采用这个界面显示新闻内容和图片
 */
public class NewsCollectMoreImageActivity extends BaseActivity {

    private ViewPager viewPager;
    private TextView tv_more_news_title;
    private TextView tv_more_news_content;

    private DisplayImageOptions options;

    // 图文spinner标题显示
    private List<String> mList = new ArrayList<>();
    private ScrollView more_news_scrollview;
    private Toolbar mToolbar;

    private String link;
    private NewsCollectImgUrlDao newsCollectImgUrlDao;

    // 传递数值
    public static final int NEWS_COLLECT_MORE_IMAGE_ACTIVITY = 40;

    private List<NewsCollectImgUrl> newsCollectImgUrls;

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
        // 图片链接数据操作
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

        // 设置Spinner下拉列表控件
        mList.add(getResources().getString(R.string.news_more_spinner_title));
        mList.add(getResources().getString(R.string.news_more_spinner_image));

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
                    if (item.equals("图片")) {

                        // 滚动条及滚动条下的所有控件都消失
                        more_news_scrollview.setVisibility(View.GONE);

                    }

                    if (item.equals("文字")) {

                        more_news_scrollview.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        // 设置内容字体大小：取出保存的值，默认在xml中保存的是16sp
        int size = SharedUtils.getSharedInt(this, "size", 0);
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
                Intent intent = new Intent(NewsCollectMoreImageActivity.this, MyCollectActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 点击查看原网页
        if (tv_news_prompt != null) {
            tv_news_prompt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(NewsCollectMoreImageActivity.this, NewsWebActivity.class);
                    intent.putExtra("url_link", link);
                    intent.putExtra("where_activity", NEWS_COLLECT_MORE_IMAGE_ACTIVITY);
                    startActivity(intent);
                }
            });
        }

    }

    /**
     * 数据
     */
    private void initData() {
        Bundle bundle = getIntent().getExtras();
        link = bundle.getString("url_link");
        String title = bundle.getString("title");
        String html = bundle.getString("html");

        // 查询另一张表中的数据，以便得到图片,得到所有标题相同的图片链接
        newsCollectImgUrls = newsCollectImgUrlDao.queryBuilder()
                .where(NewsCollectImgUrlDao.Properties.Title.eq(title)).build().list();

        // 设置toolbar标题
        mToolbar.setTitle(1 + "/" + newsCollectImgUrls.size());
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

                mToolbar.setTitle((position + 1) + "/" + newsCollectImgUrls.size());
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
            return newsCollectImgUrls.size();
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

            final String imgUrl = newsCollectImgUrls.get(position).getImgUrl();
            // 加载网络图片
            ImageLoader.getInstance().displayImage(imgUrl, imageView, options);

            // 保存图片
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    LoadImageUtils.getInit().saveImage(NewsCollectMoreImageActivity.this, imgUrl);
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
