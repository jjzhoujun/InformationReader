package com.zhimu.news.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhimu.news.R;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.bean.NewsBean;
import com.zhimu.news.impl.OnClickRecyclerItemListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * 支持下拉刷新；上拉加载更多，但没有提示上拉加载
 * <p/>
 * 2016-02-23
 */
public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 声明调用的数据类型
    List<NewsBean.ContentList> lists;
    private Context context;
    private LayoutInflater mLayoutInflater;

    private OnClickRecyclerItemListener mItemClickListener;

    // 判断类型:1为无图类型，2为有图类型
    private static final int NO_IMAGE_TYPE = 1;
    private static final int IMAGE_TYPE = 2;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    /**
     * 调用接口
     *
     * @param mItemClickListener 接口
     */
    public void setItemClickListener(OnClickRecyclerItemListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public NewsAdapter() {
    }

    public NewsAdapter(Context ctx, List<NewsBean.ContentList> contentLists) {
        this.context = ctx;
        mLayoutInflater = LayoutInflater.from(context);
        lists = contentLists;

        options = MyApplication.getInstance().getOptions(R.mipmap.news_home);
        imageLoader = ImageLoader.getInstance();

    }

    @Override
    public int getItemViewType(int position) {

        List<NewsBean.ImageUrl> imageurls = lists.get(position).getImageurls();

        // 有的没有图片，不等于0个，有图
        if (imageurls != null && imageurls.size() >= 1) {
            return IMAGE_TYPE;
        } else {
            // 无图
            return NO_IMAGE_TYPE;
        }

    }

    /**
     * item显示类型
     *
     * @param parent   父容器
     * @param viewType item视图类型
     * @return view
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == NO_IMAGE_TYPE) {
            View view = mLayoutInflater.inflate(R.layout.news_no_image_recyclerview_item, parent, false);

            return new NoImageViewHolder(view);
        } else {
            View view = mLayoutInflater.inflate(R.layout.news_image_recyclerview_item, parent, false);

            return new ImageViewHolder(view);
        }

    }

    /**
     * 数据绑定
     *
     * @param holder   holder类型
     * @param position 绑定的item位置
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        // 标题
        String title = lists.get(position).getTitle();
        String replaceTitle = title.replace("&quot", "");
        // 描述
        String desc = lists.get(position).getDesc();
        // 来源
        String source = lists.get(position).getSource();
        // 日期 pubDate='2015-12-31 20:00:13.709'
        String pubDate = lists.get(position).getPubDate();
        String substring = pubDate.substring(5, 16);

        if (holder instanceof NoImageViewHolder) {

            final NoImageViewHolder noImageViewHolder = (NoImageViewHolder) holder;

            noImageViewHolder.tv_news_noimage_title.setText(replaceTitle);
            // 设为粗体
//            ((NoImageViewHolder) holder).tv_news_noimage_title.getPaint().setFakeBoldText(true);

            if (desc == null || desc.equals("")) {
                noImageViewHolder.tv_news_noimage_desc.setVisibility(View.INVISIBLE);
                noImageViewHolder.tv_news_noimage_desc.setTextSize(0);
                noImageViewHolder.tv_news_noimage_desc.setMaxHeight(0);
            } else {
                noImageViewHolder.tv_news_noimage_desc.setText(desc);
            }

            // 来源
            noImageViewHolder.tv_news_noimage_source.setText(source);
            // 日期
            noImageViewHolder.tv_news_noimage_pubDate.setText(substring);

            // 删除
            noImageViewHolder.iv_no_image_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mItemClickListener) {
                        mItemClickListener.onItemDeleteImageViewClick(noImageViewHolder.iv_no_image_delete, position);
                    }
                }
            });

        } else if (holder instanceof ImageViewHolder) {

            final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;

            // 标题
            imageViewHolder.tv_news_image_title.setText(replaceTitle);
            // 设为粗体
//            imageViewHolder.tv_news_image_title.getPaint().setFakeBoldText(true);

            if (desc == null || desc.equals("")) {
                imageViewHolder.tv_news_image_desc.setVisibility(View.INVISIBLE);
                imageViewHolder.tv_news_image_desc.setTextSize(0);
                imageViewHolder.tv_news_image_desc.setMaxHeight(0);
            } else {
                imageViewHolder.tv_news_image_desc.setText(desc);
            }

            // 图片：设置第一张图片；imageUrl代表图片的URL地址，imageView代表承载图片的IMAGEVIEW控件,
            // options代表DisplayImageOptions配置参数,这里没有配置，快速滑动时，加载有延时，图片错位替换
            imageLoader.displayImage(
                    lists.get(position).getImageurls().get(0).getUrl(),
                    imageViewHolder.iv_news_pic, options);

            // 来源
            imageViewHolder.tv_news_image_source.setText(source);
            // 日期
            imageViewHolder.tv_news_image_pubDate.setText(substring);

            // 删除图标
            imageViewHolder.iv_image_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mItemClickListener) {
                        mItemClickListener.onItemDeleteImageViewClick(imageViewHolder.iv_image_delete, position);
                    }

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    /**
     * 有图ViewHolder
     */
    public class ImageViewHolder extends RecyclerView.ViewHolder {

        // 标题
        private final TextView tv_news_image_title;
        // 新闻描述
        private final TextView tv_news_image_desc;
        // 来源
        private final TextView tv_news_image_source;
        // 日期
        private final TextView tv_news_image_pubDate;
        // 图片
        private ImageView iv_news_pic;
        // 删除图标
        private ImageView iv_image_delete;

        public ImageViewHolder(final View view) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mItemClickListener) {
                        mItemClickListener.onItemClick(view, getAdapterPosition());
                    }
                }
            });

            tv_news_image_title = (TextView) view.findViewById(R.id.tv_news_image_title);
            tv_news_image_desc = (TextView) view.findViewById(R.id.tv_news_image_desc);
            iv_news_pic = (ImageView) view.findViewById(R.id.iv_news_pic);
            iv_image_delete = (ImageView) view.findViewById(R.id.iv_image_delete);
            tv_news_image_source = (TextView) view.findViewById(R.id.tv_news_image_source);
            tv_news_image_pubDate = (TextView) view.findViewById(R.id.tv_news_image_pubDate);
        }

    }

    /**
     * 无图ViewHolder
     */
    public class NoImageViewHolder extends RecyclerView.ViewHolder {

        // 标题
        private final TextView tv_news_noimage_title;
        // 新闻描述
        private final TextView tv_news_noimage_desc;
        // 来源
        private final TextView tv_news_noimage_source;
        // 日期
        private final TextView tv_news_noimage_pubDate;
        // 删除图标
        private ImageView iv_no_image_delete;

        public NoImageViewHolder(final View view) {
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mItemClickListener) {
                        mItemClickListener.onItemClick(view, getAdapterPosition());
                    }
                }
            });

            tv_news_noimage_title = (TextView) view.findViewById(R.id.tv_news_noimage_title);
            tv_news_noimage_desc = (TextView) view.findViewById(R.id.tv_news_noimage_desc);
            tv_news_noimage_source = (TextView) view.findViewById(R.id.tv_news_noimage_source);
            tv_news_noimage_pubDate = (TextView) view.findViewById(R.id.tv_news_noimage_pubDate);
            iv_no_image_delete = (ImageView) view.findViewById(R.id.iv_no_image_delete);
        }

    }

    // 添加更多数据到最后面
    public void addMoreItem(List<NewsBean.ContentList> newDatas) {
        lists.addAll(newDatas);
        notifyDataSetChanged();
    }

    // 在第一个位置添加数据
    public void addData(int position, NewsBean.ContentList newDatas) {
        lists.add(position, newDatas);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    /**
     * 删除数据，指定其位置
     */
    public void deleteData(int position) {
        lists.remove(position);
        notifyItemRemoved(position);
    }
}
