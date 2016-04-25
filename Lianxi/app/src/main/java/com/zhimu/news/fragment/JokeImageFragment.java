package com.zhimu.news.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.zhimu.news.R;
import com.zhimu.news.adapter.JokeAdapter;
import com.zhimu.news.bean.JokeBean;
import com.zhimu.news.divider.HorizontalDividerItemDecoration;
import com.zhimu.news.impl.OnClickRecyclerImageListener;
import com.zhimu.news.utils.AllAppKeyUtils;
import com.zhimu.news.utils.CommonRequest;
import com.zhimu.news.utils.DateUtils;
import com.zhimu.news.utils.LoadImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 图片笑话fragment：囧图
 */
public class JokeImageFragment extends Fragment {

    private SwipeRefreshLayout joke_swipeRefreshLayout;
    private RecyclerView joke_recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private JokeAdapter jokeAdapter;
    // Volley请求队列
    private RequestQueue mRequestQueue;

    // 下拉刷新的次数
    private int refreshCount = 0;

    // 上拉加载的页数:从当前获取的第一页+1
    private int pageUp = 1;

    // 记录所有数据个数
    private List<Integer> listNums = new ArrayList<>();

    // 请求页数key
    private static final String PAGE = "page";
    // 当日时间
    private static final String TIME = "time";
    // 请求最大数量
    private static final String MAX_RESULT = "maxResult";
    private int firstAllNum;

    public JokeImageFragment() {

    }

    /**
     * 绑定一个视图view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_joke, container, false);
            // 初始化控件
            initView(view);

            return view;
    }

    /**
     * 加载视图完后调用，此时加载数据
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRequestQueue = Volley.newRequestQueue(getContext());

        // 加载第一次请求数据
        initFirstData();

    }

    /**
     * 初始化控件，设置监听事件
     *
     * @param view
     */
    private void initView(View view) {
        joke_swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.joke_swipeRefreshLayout);
        joke_recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        // 设置RecyclerView属性
        joke_recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        // 添加分隔线
        joke_recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .marginResId(R.dimen.dimen_10, R.dimen.dimen_10)
                        .build());
        joke_recyclerView.setLayoutManager(linearLayoutManager);

        // 加载进度条开始到结束，4种变换颜色
        joke_swipeRefreshLayout.setProgressViewOffset(false, 0, 100);
        joke_swipeRefreshLayout.setColorSchemeResources(
                R.color.colorSwipeRefresh1,
                R.color.colorSwipeRefresh2,
                R.color.colorSwipeRefresh3,
                R.color.colorSwipeRefresh4);

        /**
         * 下拉刷新监听
         */
        joke_swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshCount++;

                initRefreshData();
            }
        });

        /**
         * RecyclerView滑动监听
         */
        joke_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && (lastVisibleItemPosition + 1) == linearLayoutManager.getItemCount()) {

                    initUpRequest();
                }
            }

        });

    }

    /**
     * 第一次加载的数据
     */
    private void initFirstData() {

        // 第一次加载显示进度
        joke_swipeRefreshLayout.setRefreshing(true);

        HashMap<String, String> hashMap = new HashMap<>();
        // 今天的数据第一次刷新从第一页开始：上拉加载从第二页开始
        hashMap.put(PAGE, "1");
        // 每次返回10条数据
        hashMap.put(MAX_RESULT, "10");
        // 请求这段时间以来的所有数据：以便上拉加载数据使用：下拉刷新则请求今天的数据：
        // 因为这个时间段包含了今天，默认会显示今天以来最新的数据
        hashMap.put(TIME, "2015-07-10");

        CommonRequest firstRequest = new CommonRequest(AllAppKeyUtils.JOKE_IMAGE_URL, hashMap,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        joke_swipeRefreshLayout.setRefreshing(false);

                        Gson gson = new Gson();
                        JokeBean firstJokeData = gson.fromJson(response, JokeBean.class);
                        final List<JokeBean.ShowapiResBody.Contentlist> contentlist =
                                firstJokeData.getShowapi_res_body().getContentlist();
                        // 获取所有数量
                        firstAllNum = firstJokeData.getShowapi_res_body().getAllNum();
                        listNums.add(firstAllNum);

                        // 内容为空，提示没有数据
                        if (contentlist == null || contentlist.isEmpty()) {
                            Toast.makeText(getActivity(), R.string.joke_img_toast, Toast.LENGTH_SHORT).show();
                        } else {
                            jokeAdapter = new JokeAdapter(getActivity(), contentlist);
                            joke_recyclerView.setAdapter(jokeAdapter);

                            // 接口回调，保存图片
                            jokeAdapter.setOnClickMyImageListener(new OnClickRecyclerImageListener() {
                                @Override
                                public void onItemImageClick(View view, int position) {

                                    LoadImageUtils.getInit().saveImage(getActivity(), contentlist.get(position).getImg());
                                }
                            });

                        }

                    }
                }, new Response.ErrorListener() {
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
     * 上拉加载的数据
     */
    private void initUpRequest() {

        // 第一次加载为第二页
        pageUp++;

        HashMap<String, String> hashMap = new HashMap<>();
        // 这个时间段的数据，都可以加载:文档里没有做要求
        hashMap.put(TIME, "2015-07-10");
        hashMap.put(PAGE, String.valueOf(pageUp));
        // 每次返回10条数据
        hashMap.put(MAX_RESULT, "10");

        CommonRequest upRequest = new CommonRequest(AllAppKeyUtils.JOKE_IMAGE_URL, hashMap,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Gson gson = new Gson();
                        JokeBean jokeData = gson.fromJson(response, JokeBean.class);
                        List<JokeBean.ShowapiResBody.Contentlist> contentlist =
                                jokeData.getShowapi_res_body().getContentlist();
                        // 不能提取为全局变量，会与第一次加载的数据覆盖，但contentlist数据并没有改变
                        jokeAdapter.addMoreItem(contentlist);
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
     * 下拉刷新时请求的数据
     */
    private void initRefreshData() {

        HashMap<String, String> hashMap = new HashMap<>();
        // 今天的从第1页开始最新的数据：这里不需要设置，默认显示第一页的数据：最多返回20条数据
        hashMap.put(PAGE, "1");
        // 请求今天的最新数据
        String mToday = new DateUtils().getDate("yyyy-MM-dd");
        hashMap.put(TIME, mToday);

        CommonRequest refreshRequest = new CommonRequest(AllAppKeyUtils.JOKE_IMAGE_URL, hashMap,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        joke_swipeRefreshLayout.setRefreshing(false);

                        Gson gson = new Gson();
                        JokeBean jokeData = gson.fromJson(response, JokeBean.class);
                        List<JokeBean.ShowapiResBody.Contentlist> contentlist =
                                jokeData.getShowapi_res_body().getContentlist();
                        // 下拉刷新时的所有数据
                        int refreshAllNum = jokeData.getShowapi_res_body().getAllNum();
                        listNums.add(refreshAllNum);
                        // 比较前后两次的总数差：后一次数量大于前一次数量，说明服务器更新了数据
                        int num = listNums.get(refreshCount) - listNums.get(refreshCount - 1);

                        if (num > 0) {
                            for (int i = 0; i < (num < 21 ? num : contentlist.size()); i++) {
                                jokeAdapter.addData(i, contentlist.get(i));
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.joke_img_toast_refresh, Toast.LENGTH_SHORT).show();
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
     *
     */
    private void showError() {
        // 请求失败隐藏刷新控件
        joke_swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getActivity(), R.string.joke_img_toast_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mRequestQueue.cancelAll("firstRequest");
        mRequestQueue.cancelAll("upRequest");
        mRequestQueue.cancelAll("refreshRequest");
        mRequestQueue.stop();
    }
}
