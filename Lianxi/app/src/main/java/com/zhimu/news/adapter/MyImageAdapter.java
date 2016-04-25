package com.zhimu.news.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhimu.news.R;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.impl.OnClickRecyclerImageListener;

import java.util.List;

/**
 * 显示我的图片
 * Created by Administrator on 2016.4.8.
 */
public class MyImageAdapter extends RecyclerView.Adapter {

    private List<String> mList;
    private Context context;

    private OnClickRecyclerImageListener onClickMyImageListener;
    private final LayoutInflater mLayoutInflater;
    private final DisplayImageOptions options;
    private final ImageLoader imageLoader;

    public MyImageAdapter(Context cxt, List<String> list) {
        this.context = cxt;
        mList = list;
        mLayoutInflater = LayoutInflater.from(context);

        options = MyApplication.getInstance().getOptions(R.mipmap.no_pictures);
        imageLoader = ImageLoader.getInstance();

    }

    public void setOnClickMyImageListener(OnClickRecyclerImageListener onClickMyImageListener) {
        this.onClickMyImageListener = onClickMyImageListener;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.my_image_gridview_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        String path = mList.get(position);

        final MyViewHolder viewHolder = (MyViewHolder) holder;
        final ImageView imageView = (ImageView) viewHolder.imageView;
        imageLoader.displayImage(path, imageView, options);
    }

    /**
     * 缓存数据item
     */
    private class MyViewHolder extends RecyclerView.ViewHolder {

        private final View imageView;

        public MyViewHolder(final View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != onClickMyImageListener) {
                        onClickMyImageListener.onItemImageClick(itemView, getAdapterPosition());
                    }
                }
            });

            imageView = itemView.findViewById(R.id.my_image_item);

        }

    }

}
