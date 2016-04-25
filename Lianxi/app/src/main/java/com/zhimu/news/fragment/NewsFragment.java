package com.zhimu.news.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.zhimu.news.NewsDetailedActivity;
import com.zhimu.news.NewsMoreImageActivity;
import com.zhimu.news.NewsWebActivity;
import com.zhimu.news.R;
import com.zhimu.news.adapter.NewsAdapter;
import com.zhimu.news.divider.HorizontalDividerItemDecoration;
import com.zhimu.news.bean.NewsBean;
import com.zhimu.news.impl.OnClickRecyclerItemListener;
import com.zhimu.news.utils.AllAppKeyUtils;
import com.zhimu.news.utils.CommonRequest;
import com.zhimu.news.utils.NetUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 新闻fragment主页
 */
public class NewsFragment extends Fragment implements OnClickRecyclerItemListener {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private NewsAdapter adapter;

    // Tab标题名称
    private String mText;

    // 请求队列
    private RequestQueue mRequestQueue;
    // 数据列表
    private List<NewsBean.ContentList> contentLists;

    // 上拉加载请求数据：从第二页开始加载pageUp++
    private int pageUp = 2;
    // 标记下拉刷新的次数
    private int countRefresh = 0;

    // 标记每次下拉刷新的数量
    private List<Integer> listNums = new ArrayList<>();

    // Tab标题key值
    private static final String TITLE = "title";
    // 请求页数key
    private static final String PAGE = "page";
    // 频道名称
    private static final String CHANNEL_NAME = "channelName";

    // 标记
    public static final int NEWS_FRAGMENT = 110;
    private TextView tv_net_setting;

    public NewsFragment() {
    }

    /**
     * @param title tab标签
     * @return A new instance of fragment NewsFragment.
     */
    public static NewsFragment newInstance(String title) {
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mText = getArguments().getString(TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_news, container, false);
        // 初始化控件
        initView(view);

        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化异步请求队列
        mRequestQueue = Volley.newRequestQueue(getContext());

        // 首次进入加载数据
        initData();

    }

    /**
     * 初始化控件
     *
     * @param view 控件
     */
    private void initView(View view) {
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.new_swipeRefreshLayout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        // 设置RecyclerView属性
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        // 添加分隔线
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .marginResId(R.dimen.dimen_10, R.dimen.dimen_10)
                        .build());
        recyclerView.setLayoutManager(linearLayoutManager);

        // 加载进度条开始到结束，4种变换颜色
        refreshLayout.setProgressViewOffset(false, 0, 100);
        refreshLayout.setColorSchemeResources(
                R.color.colorSwipeRefresh1,
                R.color.colorSwipeRefresh2,
                R.color.colorSwipeRefresh3,
                R.color.colorSwipeRefresh4);

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

                    initUpRequestData();
                }
            }
        });

        /**
         * 下拉刷新
         */
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                initRefreshData();
            }
        });
    }

    /**
     * 第一次请求数据
     */
    private void initData() {

        // 第一次加载显示进度
        refreshLayout.setRefreshing(true);

        HashMap<String, String> hashMap = new HashMap<>();
        // 首次请求第1页的数据：每页20条
        hashMap.put(PAGE, "1");
        // 频道标题不为“推荐”时，加入标题查询
        if (!mText.equals(getResources().getString(R.string.home_news_fragment_title))) {
            hashMap.put(CHANNEL_NAME, mText);
        }

        CommonRequest firstRequest = new CommonRequest(AllAppKeyUtils.NEWS_URL, hashMap,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        refreshLayout.setRefreshing(false);

                        Gson gson = new Gson();
                        NewsBean newsBean = gson.fromJson(response, NewsBean.class);

                        // 获取新闻详情列表
                        contentLists = newsBean.getShowapi_res_body().getPagebean().getContentlist();

                        // 标记第一次加载数据的所有数量
                        int firstAllNum = newsBean.getShowapi_res_body().getPagebean().getAllNum();
                        listNums.add(firstAllNum);

                        adapter = new NewsAdapter(getActivity(), contentLists);
                        recyclerView.setAdapter(adapter);

                        adapter.setItemClickListener(NewsFragment.this);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showError();
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
        hashMap.put(PAGE, String.valueOf(pageUp));
        // 频道标题不为“推荐”时，加入标题查询
        if (!mText.equals(getResources().getString(R.string.home_news_fragment_title))) {
            hashMap.put(CHANNEL_NAME, mText);
        }

        CommonRequest upRequest = new CommonRequest(AllAppKeyUtils.NEWS_URL, hashMap,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        NewsBean newsBean = gson.fromJson(response, NewsBean.class);
                        // 获取新闻详情列表
                        List<NewsBean.ContentList> contentlist = newsBean.getShowapi_res_body().getPagebean().getContentlist();
                        adapter.addMoreItem(contentlist);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showError();
                    }
                });

        upRequest.setTag("upRequest");
        mRequestQueue.add(upRequest);
        mRequestQueue.start();
    }

    /**
     * 下拉刷新
     */
    private void initRefreshData() {

        // 标记下拉刷新次数
        countRefresh++;

        HashMap<String, String> hashMap = new HashMap<>();
        // 每次请求第1页的数据：每页20条
        hashMap.put(PAGE, "1");
        // 频道标题不为“推荐”时，加入标题查询
        if (!mText.equals(getResources().getString(R.string.home_news_fragment_title))) {
            hashMap.put(CHANNEL_NAME, mText);
        }

        CommonRequest refreshRequest = new CommonRequest(AllAppKeyUtils.NEWS_URL, hashMap,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        refreshLayout.setRefreshing(false);

                        Gson gson = new Gson();
                        NewsBean newsBean = gson.fromJson(response, NewsBean.class);
                        // 因为系统时间给的当前时间，如果刷新太快，有可能获取到的是同一数据：改变时间参数
                        List<NewsBean.ContentList> contentLists = newsBean.getShowapi_res_body().getPagebean().getContentlist();

                        // 比较两次的数量差
                        int refreshAllNum = newsBean.getShowapi_res_body().getPagebean().getAllNum();
                        listNums.add(refreshAllNum);
                        // 用中间差值，显示在手机列表上;如服务器更新了5条数据：因为当前获取的最新新闻永远小于前一次的个数;
                        // 这个值可能很大：第一次的减去第二次的，大于0，数据有更新
                        int num = listNums.get(countRefresh - 1) - listNums.get(countRefresh);
                        if (num > 0) {
                            for (int i = 0; i < (num < 21 ? num : contentLists.size()); i++) {
                                adapter.addData(i, contentLists.get(i));
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.news_fragment_toast_refresh, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showError();
                    }
                });

        refreshRequest.setTag("refreshRequest");
        mRequestQueue.add(refreshRequest);
        mRequestQueue.start();
    }

    /**
     * 处理错误信息
     */
    private void showError() {
        // 请求失败隐藏刷新控件
        refreshLayout.setRefreshing(false);
        Toast.makeText(getActivity(), R.string.news_fragment_toast_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRequestQueue.cancelAll("firstRequest");
        mRequestQueue.cancelAll("refreshRequest");
        mRequestQueue.cancelAll("upRequest");
        mRequestQueue.stop();
    }

    /**
     * 点击跳转不同界面
     *
     * @param view     被点击的view
     * @param position 点击索引
     */
    @Override
    public void onItemClick(View view, int position) {

        /**
         * 修改：使用eventbus
         */

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

                intoActivity(getActivity(), NewsWebActivity.class,
                        link, null, null, null, null, null, null);

            } else {

                intoActivity(getActivity(), NewsMoreImageActivity.class,
                        link, title, source, null, desc, html, imageurls);
            }

        }

        // 有一个图片
        if (!imageurls.isEmpty() && imageurls.size() == 1) {

            if (html == null || html.equals("") || spanned.length() < 50) {

                intoActivity(getActivity(), NewsWebActivity.class,
                        link, null, null, null, null, null, null);

            } else {

                intoActivity(getActivity(), NewsDetailedActivity.class,
                        link, title, source, pubDate, desc, html, imageurls);
            }

        }

        // 没有图片
        if (imageurls.isEmpty() || imageurls.size() == 0) {
            if (html == null || html.equals("") || spanned.length() < 50) {

                intoActivity(getActivity(), NewsWebActivity.class,
                        link, null, null, null, null, null, null);
            } else {

                intoActivity(getActivity(), NewsDetailedActivity.class,
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
        bundle.putInt("where_fragment_activity", NEWS_FRAGMENT);

        intentShow.putExtras(bundle);
        startActivity(intentShow);

    }


    /**
     * 删除
     *
     * @param view     删除图标
     * @param position 当前值
     */
    @Override
    public void onItemDeleteImageViewClick(View view, int position) {
        adapter.deleteData(position);
    }

}
