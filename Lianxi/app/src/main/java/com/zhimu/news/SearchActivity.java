package com.zhimu.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.zhimu.news.adapter.NewsAdapter;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.bean.NewsBean;
import com.zhimu.news.divider.HorizontalDividerItemDecoration;
import com.zhimu.news.impl.OnClickRecyclerItemListener;
import com.zhimu.news.utils.AllAppKeyUtils;
import com.zhimu.news.utils.CommonRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * 搜索结果界面
 * <p>
 * 2016-03-25
 */
public class SearchActivity extends BaseActivity implements OnClickRecyclerItemListener {

    private RecyclerView recyclerView;
    private RequestQueue mRequestQueue;
    private List<NewsBean.ContentList> contentLists;
    private NewsAdapter adapter;

    // 搜索新闻编辑框
    private EditText search_news;
    // 删除图片
    private ImageView toolbar_delete_image;
    private LinearLayoutManager linearLayoutManager;

    // 上拉加载请求页数
    private int pageUp = 1;

    private Gson gson;
    // 搜索标题
    private String title;

    // 标记
    public static final int SEARCH_ACTIVITY = 100;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mRequestQueue = Volley.newRequestQueue(this);

        initView();
    }

    /**
     * 初始化View
     */
    private void initView() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.search_swipeRefreshLayout);
        search_news = (EditText) findViewById(R.id.search_news);
        toolbar_delete_image = (ImageView) findViewById(R.id.toolbar_delete_image);
        ImageView search_toolbar_image = (ImageView) findViewById(R.id.search_toolbar_image);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // 不设置toolbar的标题，null没有效果
        if (mToolbar != null) {
            mToolbar.setTitle("");
            mToolbar.setNavigationIcon(R.mipmap.back);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击返回首页：前面跳转时，已经把TabHomeActivity销毁掉了
                    Intent intent = new Intent(SearchActivity.this, TabHomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                }

            });
        }

        // 加载进度条开始到结束，4种变换颜色
        refreshLayout.setProgressViewOffset(false, 0, 250);
        refreshLayout.setColorSchemeResources(
                R.color.colorSwipeRefresh1,
                R.color.colorSwipeRefresh2,
                R.color.colorSwipeRefresh3,
                R.color.colorSwipeRefresh4);

        // 设置RecyclerView属性
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        // 添加分隔线
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .marginResId(R.dimen.dimen_10, R.dimen.dimen_10)
                        .build());

        recyclerView.setLayoutManager(linearLayoutManager);

        /**
         * 监听Text内容变化
         */
        search_news.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    toolbar_delete_image.setVisibility(View.VISIBLE);
                } else {
                    toolbar_delete_image.setVisibility(View.INVISIBLE);
                }
            }
        });

        /**
         * 搜索图片监听
         */
        if (search_toolbar_image != null) {
            search_toolbar_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    title = search_news.getText().toString();

                    // 关闭软键盘
                    InputMethodManager methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (methodManager.isActive()) {
                        methodManager.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }

                    // 判断输入的title是否合法
                    if (title.length() < 0 || title.equals("")) {
                        Toast.makeText(SearchActivity.this, R.string.search_toast_search_title, Toast.LENGTH_SHORT).show();
                    } else {
                        // 开始搜索
                        searchNews();
                    }

                }
            });
        }

        /**
         * 删除图片监听
         */
        toolbar_delete_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_news.setText("");

                // 如果软键盘隐藏了，打开软键盘
                InputMethodManager methodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (methodManager.isActive()) {
                    methodManager.toggleSoftInput(1, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        /**
         * RecyclerView滑动监听
         */
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && (lastVisibleItemPosition + 1) == linearLayoutManager.getItemCount()) {

                    // 判断输入的title是否合法
                    if (title.length() < 0 || title.equals("")) {
                        Toast.makeText(SearchActivity.this, R.string.search_toast_search_title, Toast.LENGTH_SHORT).show();
                    } else {
                        // 开始搜索
                        initUpRequestData();
                    }
                }
            }
        });

    }

    /**
     * 搜索请求数据
     */
    private void searchNews() {

        // 启动搜索时，启动进度
        refreshLayout.setRefreshing(true);

        HashMap<String, String> hashMap = new HashMap<>();
        // 首次请求第1页的数据：每页20条
        hashMap.put("page", "1");
        // 查询新闻的标题
        hashMap.put("title", title);

        CommonRequest firstRequest = new CommonRequest(AllAppKeyUtils.NEWS_URL, hashMap,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        refreshLayout.setRefreshing(false);

                        // 解析json数据
                        gson = new Gson();
                        NewsBean newsTestBean = gson.fromJson(response, NewsBean.class);
                        NewsBean.PageBean pagebean = newsTestBean.getShowapi_res_body().getPagebean();

                        contentLists = pagebean.getContentlist();
                        adapter = new NewsAdapter(SearchActivity.this, contentLists);
                        recyclerView.setAdapter(adapter);

                        adapter.setItemClickListener(SearchActivity.this);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        refreshLayout.setRefreshing(false);

                        Toast.makeText(SearchActivity.this, R.string.search_toast_error, Toast.LENGTH_SHORT).show();

                    }
                });

        firstRequest.setTag("firstRequest");
        mRequestQueue.add(firstRequest);
        mRequestQueue.start();

    }

    /**
     * 上拉加载数据
     */
    private void initUpRequestData() {

        pageUp++;
        HashMap<String, String> hashMap = new HashMap<>();
        // 每次请求从第2页开始：每页20条
        hashMap.put("page", String.valueOf(pageUp));
        // 查询新闻的标题
        hashMap.put("title", title);

        CommonRequest upRequest = new CommonRequest(AllAppKeyUtils.NEWS_URL, hashMap,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        NewsBean newsTestBean = gson.fromJson(response, NewsBean.class);
                        List<NewsBean.ContentList> contentlist = newsTestBean.getShowapi_res_body().getPagebean().getContentlist();
                        adapter.addMoreItem(contentlist);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(SearchActivity.this, R.string.search_toast_error, Toast.LENGTH_SHORT).show();

                    }
                });

        upRequest.setTag("upRequest");
        mRequestQueue.add(upRequest);
        mRequestQueue.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRequestQueue.cancelAll("firstRequest");
        mRequestQueue.cancelAll("upRequest");
        mRequestQueue.stop();
    }

    @Override
    public void onItemClick(View view, int position) {
        // html格式内容
        String html = contentLists.get(position).getHtml();

        // 网页链接
        String link = contentLists.get(position).getLink();
        // 标题
        String title = contentLists.get(position).getTitle();
        // 来源
        String source = contentLists.get(position).getSource();
        // 日期
        String pubDate = contentLists.get(position).getPubDate();
        // 描述
        String desc = contentLists.get(position).getDesc();
        // 图片
        List<NewsBean.ImageUrl> imageurls = contentLists.get(position).getImageurls();

        // 如果html内容长度小于50个字符，或者没有内容，直接跳转到原网页链接
        Spanned spanned = Html.fromHtml(html);

        // 当图片个数大于2个时，跳转到MoreImageNewsActivity,并传递标题+图片链接地址
        if (!imageurls.isEmpty() && imageurls.size() >= 2) {

            if (html == null || html.equals("") || spanned.length() < 50) {

                intoActivity(this, NewsWebActivity.class,
                        link, null, null, null, null, null, null);

            } else {

                intoActivity(this, NewsMoreImageActivity.class,
                        link, title, source, null, desc, html, imageurls);
            }

        }

        // 有一个图片
        if (!imageurls.isEmpty() && imageurls.size() == 1) {

            if (html == null || html.equals("") || spanned.length() < 50) {

                intoActivity(this, NewsWebActivity.class,
                        link, null, null, null, null, null, null);

            } else {

                intoActivity(this, NewsDetailedActivity.class,
                        link, title, source, pubDate, desc, html, imageurls);
            }

        }

        // 没有图片
        if (imageurls.isEmpty() || imageurls.size() == 0) {
            if (html == null || html.equals("") || spanned.length() < 50) {

                intoActivity(this, NewsWebActivity.class,
                        link, null, null, null, null, null, null);
            } else {

                intoActivity(this, NewsDetailedActivity.class,
                        link, title, source, pubDate, desc, html, null);
            }
        }

    }

    /**
     * 跳转不同界面
     *
     * @param link    原网页链接
     * @param title   标题
     * @param source  来源
     * @param pubDate 日期
     * @param desc    描述
     * @param html    内容
     */
    private void intoActivity(Context context, Class<?> cls, String link, String title, String source, String pubDate,
                              String desc, String html, List<NewsBean.ImageUrl> imageurls) {

        Intent intentShow = new Intent(context, cls);

        Bundle bundle = new Bundle();
        bundle.putString("url_link", link);
        bundle.putString("title", title);
        bundle.putString("source", source);
        bundle.putString("pub_date", pubDate);
        bundle.putString("desc", desc);
        bundle.putString("html", html);
        bundle.putSerializable("image_url", (Serializable) imageurls);
        // 搜索界面传值
        bundle.putInt("where_fragment_activity", SEARCH_ACTIVITY);
        intentShow.putExtras(bundle);
        startActivity(intentShow);

    }

    @Override
    public void onItemDeleteImageViewClick(View view, int position) {
        adapter.deleteData(position);
    }

}
