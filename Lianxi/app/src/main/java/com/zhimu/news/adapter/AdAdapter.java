package com.zhimu.news.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhimu.news.R;

import java.util.List;

import cn.waps.AdInfo;

/**
 * 原生广告适配器
 * Created by Administrator on 2016.4.20.
 */
public class AdAdapter extends RecyclerView.Adapter<AdAdapter.AdViewHolder> {

    private List<AdInfo> lists;
    private Context context;
    private LayoutInflater mLayoutInflater;
    private AdAdListener listener;

    public AdAdapter(List<AdInfo> lists, Context ctx) {
        this.lists = lists;
        context = ctx;

        mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * 设置接口
     * @param listener 监听接口
     */
    public void setAdAdListener(AdAdListener listener) {
        this.listener = listener;
    }

    @Override
    public AdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.explain_ad_item, parent, false);
        return new AdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdViewHolder holder, int position) {

        // 标题
        String adName = lists.get(position).getAdName();
        Bitmap adIcon = lists.get(position).getAdIcon();
        // 设置
        holder.ad_iv.setImageBitmap(adIcon);
        holder.ad_title.setText(adName);

    }

    @Override
    public int getItemCount() {
        // 广告已经在最初初始化了，在闪频页加载，缓存了20条，可能会报空指针异常
        return lists.size();
    }

    /**
     * 数据缓存工具
     */
    public class AdViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ad_iv;
        private final TextView ad_title;

        public AdViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (null != listener) {
                        listener.onClickItem(itemView, getAdapterPosition());
                    }
                }
            });

            ad_iv = (ImageView) itemView.findViewById(R.id.ad_iv);
            ad_title = (TextView) itemView.findViewById(R.id.ad_title);
        }
    }

    /**
     * 点击接口
     */
    public interface AdAdListener {
        void onClickItem(View view, int position);
    }
}
