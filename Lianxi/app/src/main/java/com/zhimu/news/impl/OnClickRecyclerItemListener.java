package com.zhimu.news.impl;

import android.view.View;

/**
 * 新闻类item点击回调接口
 * Created by Administrator on 2016.2.27.
 */
public interface OnClickRecyclerItemListener {

    /**
     * item view 回调方法
     * @param view  被点击的view
     * @param position 索引值
     */
    void onItemClick(View view, int position);

    /**
     *
     * @param view  删除小图标
     * @param position      当前索引值
     */
    void onItemDeleteImageViewClick(View view, int position);
}
