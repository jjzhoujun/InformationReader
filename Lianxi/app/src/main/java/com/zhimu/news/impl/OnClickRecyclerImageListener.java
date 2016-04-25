package com.zhimu.news.impl;

import android.view.View;

/**
 *
 * 图文笑话的图片点击
 * 点击图片：保存当前的图片回调方法
 * Created by Administrator on 2016.2.27.
 */
public interface OnClickRecyclerImageListener {

    /**
     * item view 回调方法
     * @param view  被点击的view
     * @param position 索引值
     */
    void onItemImageClick(View view, int position);

}
