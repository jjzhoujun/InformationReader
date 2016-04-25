package com.zhimu.news.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhimu.news.R;
import com.zhimu.news.channel.ChannelItem;

import java.util.List;

/**
 * 可以拖拽的gridview适配器
 */
public class DragGridViewAdapter extends BaseAdapter {

    /**
     * 是否显示底部的ITEM
     */
    private boolean isItemShow = false;

    private Context context;
    /**
     * 控制的postion
     */
    private int holdPosition;
    /**
     * 是否改变
     */
    private boolean isChanged = false;
    /**
     * 列表数据是否改变
     */
    private boolean isListChanged = false;
    /**
     * 是否可见
     */
    boolean isVisible = true;
    /**
     * 可以拖动的列表（即用户选择的频道列表）
     */
    public List<ChannelItem> channelList;
    /**
     * TextView 频道内容
     */
    private TextView item_text;
    /**
     * 要删除的position
     */
    public int remove_position = -1;

    private boolean bool = false;

    public DragGridViewAdapter(Context context, List<ChannelItem> channelList) {
        this.context = context;
        this.channelList = channelList;


    }

    // 设置textview第一个字体颜色
    public void setItemColor(boolean bool) {
        this.bool = bool;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return channelList == null ? 0 : channelList.size();
    }

    @Override
    public ChannelItem getItem(int position) {
        // TODO Auto-generated method stub
        if (channelList != null && channelList.size() != 0) {
            return channelList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.channel_gridview_item, null, false);
        item_text = (TextView) view.findViewById(R.id.text_item);
        ImageView iv_channel_delete = (ImageView) view.findViewById(R.id.iv_channel_delete);
        ChannelItem channel = getItem(position);
        item_text.setText(channel.getName());

        // 过时
//        item_text.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.channel_item_bg));
        item_text.setBackgroundResource(R.drawable.channel_item_bg);

        // 如果是true，说明需要有删除按钮
        if (bool && position != 0) {
            iv_channel_delete.setVisibility(View.VISIBLE);
        }

        // 把第一个设置为红色字体
        if (position == 0) {
            item_text.setTextColor(context.getResources().getColor(R.color.channel_item_txt));
            item_text.setEnabled(false);
        }

        if (isChanged && (position == holdPosition) && !isItemShow) {
            item_text.setText("");
            item_text.setSelected(true);
            item_text.setEnabled(true);
            isChanged = false;
        }
        if (!isVisible && (position == -1 + channelList.size())) {
            item_text.setText("");
            item_text.setSelected(true);
            item_text.setEnabled(true);
        }
        if (remove_position == position) {
            item_text.setText("");
        }
        return view;
    }

    /**
     * 添加频道列表
     */
    public void addItem(ChannelItem channel) {
        channelList.add(channel);
        isListChanged = true;
        notifyDataSetChanged();
    }

    /**
     * 拖动变更频道排序
     */
    public void exchange(int dragPostion, int dropPostion) {
        holdPosition = dropPostion;
        ChannelItem dragItem = getItem(dragPostion);

        if (dragPostion < dropPostion) {
            channelList.add(dropPostion + 1, dragItem);
            channelList.remove(dragPostion);
        } else {
            channelList.add(dropPostion, dragItem);
            channelList.remove(dragPostion + 1);
        }
        isChanged = true;
        isListChanged = true;
        notifyDataSetChanged();
    }

    /**
     * 获取频道列表
     */
    public List<ChannelItem> getChannnelLst() {
        return channelList;
    }

    /**
     * 设置删除的position
     */
    public void setRemove(int position) {
        remove_position = position;
        notifyDataSetChanged();
    }

    /**
     * 删除频道列表
     */
    public void remove() {
        channelList.remove(remove_position);
        remove_position = -1;
        isListChanged = true;
        notifyDataSetChanged();
    }

    /**
     * 设置频道列表
     */
    public void setListDate(List<ChannelItem> list) {
        channelList = list;
    }

    /**
     * 获取是否可见
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * 排序是否发生改变
     */
    public boolean isListChanged() {
        return isListChanged;
    }

    /**
     * 设置是否可见
     */
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    /**
     * 显示放下的ITEM
     */
    public void setShowDropItem(boolean show) {
        isItemShow = show;
    }
}