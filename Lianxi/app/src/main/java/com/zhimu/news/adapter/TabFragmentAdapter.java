package com.zhimu.news.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.zhimu.news.channel.ChannelItem;

import java.util.List;

/**
 * 新闻tab标签适配器
 * Created by Administrator on 2016.3.2.
 */
public class TabFragmentAdapter extends FragmentStatePagerAdapter {

    // 存放fagment列表
    private List<Fragment> fragmentLists;
    // 新闻tab列表
    private List<ChannelItem> lists;

    public TabFragmentAdapter(List<Fragment> fragmentLists, FragmentManager fm, List<ChannelItem> lists) {
        super(fm);
        this.lists = lists;
        this.fragmentLists = fragmentLists;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentLists.get(position);
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    /**
     * 每个页面的导航标题
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return lists.get(position).getName();
    }
}
