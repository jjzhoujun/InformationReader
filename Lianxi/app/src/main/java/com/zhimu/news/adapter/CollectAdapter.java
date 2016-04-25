package com.zhimu.news.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhimu.news.R;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.dao.DaoMaster;
import com.zhimu.news.dao.DaoSession;
import com.zhimu.news.dao.NewsCollect;
import com.zhimu.news.dao.NewsCollectImgUrl;
import com.zhimu.news.dao.NewsCollectImgUrlDao;
import com.zhimu.news.impl.OnClickRecyclerItemListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.List;

/**
 * 我的收藏适配器
 * 2016-03-21
 *
 */
public class CollectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NewsCollect> lists;
    private LayoutInflater mLayoutInflater;

    private OnClickRecyclerItemListener onClickRecyclerItemListener;

    // 判断类型:1为无图类型，2为有图类型
    private static final int NO_IMAGE_TYPE = 1;
    private static final int IMAGE_TYPE = 2;
    private DisplayImageOptions options;

    private Context context;
    private final NewsCollectImgUrlDao newsCollectImgUrlDao;

    /**
     * 调用接口
     *
     * @param onClickRecyclerItemListener
     *          接口
     */
    public void setItemClickListener(OnClickRecyclerItemListener onClickRecyclerItemListener) {
        this.onClickRecyclerItemListener = onClickRecyclerItemListener;
    }

    public CollectAdapter(Context ctx, List<NewsCollect> lists) {
        this.lists = lists;
        this.context = ctx;
        mLayoutInflater = LayoutInflater.from(context);

        // 初始化图片设置
        options = MyApplication.getInstance().getOptions(R.mipmap.news_home);

        // 打开数据库
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(context, "collects", null);
        SQLiteDatabase database = devOpenHelper.getWritableDatabase();
        // 检查数据库的更新和创建
        DaoMaster daoMaster = new DaoMaster(database);
        // 注册实体数据
        DaoSession daoSession = daoMaster.newSession();
        // 数据操作
        newsCollectImgUrlDao = daoSession.getNewsCollectImgUrlDao();
    }

    @Override
    public int getItemViewType(int position) {

        Boolean isImgUrl = lists.get(position).getIsImgUrl();

        // 有的没有图片，不等于0个，有图
        if (isImgUrl) {
            return IMAGE_TYPE;
        }else {
            return NO_IMAGE_TYPE;
        }

    }

    /**
     * item显示类型
     *
     * @param parent
     *              父容器
     * @param viewType
     *              item视图类型
     * @return
     *          view
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
     * @param holder
     *              holder类型
     * @param position
     *              绑定的item位置
     */
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        // 标题
        String title = lists.get(position).getTitle();
        String replaceTitle = title.replace("&quot", "");
        // 描述
        String desc = lists.get(position).getDesc();
        // 来源
        String source = lists.get(position).getSource();
        // 日期2016-04-11 20:23:45
        String date = lists.get(position).getPubDate();
        String collect_date = context.getResources().getString(R.string.my_collect_detailed_source) +
                date.substring(5, 16);
        // 图片链接
        Boolean isImgUrl = lists.get(position).getIsImgUrl();
        String imgUrl = null;
        if (isImgUrl) {

            // 查询另一张表中的数据，以便得到图片
            List<NewsCollectImgUrl> newsCollectImgUrls = newsCollectImgUrlDao.queryBuilder().
                    where(NewsCollectImgUrlDao.Properties.Title.eq(title)).build().list();

            imgUrl = newsCollectImgUrls.get(0).getImgUrl();
        }

        if (holder instanceof NoImageViewHolder) {

            final NoImageViewHolder noImageViewHolder = (NoImageViewHolder) holder;

            noImageViewHolder.tv_news_noimage_title.setText(replaceTitle);

            if (desc == null || desc.equals("")) {
                noImageViewHolder.tv_news_noimage_desc.setVisibility(View.INVISIBLE);
                noImageViewHolder.tv_news_noimage_desc.setTextSize(0);
                noImageViewHolder.tv_news_noimage_desc.setMaxHeight(0);
            } else {
                noImageViewHolder.tv_news_noimage_desc.setText(desc);
            }

            // 来源
            noImageViewHolder.tv_news_noimage_source.setText(source);

            noImageViewHolder.tv_news_noimage_pubDate.setText(collect_date);

            // 删除
            noImageViewHolder.iv_no_image_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != onClickRecyclerItemListener) {
                        onClickRecyclerItemListener.onItemDeleteImageViewClick(noImageViewHolder.iv_no_image_delete,
                                noImageViewHolder.getAdapterPosition());
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
            ImageLoader.getInstance().displayImage(
                    imgUrl,
                    imageViewHolder.iv_news_pic, options);

            // 来源
            imageViewHolder.tv_news_image_source.setText(source);
            // 日期
            imageViewHolder.tv_news_image_pubDate.setText(collect_date);

            // 删除
            imageViewHolder.iv_image_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != onClickRecyclerItemListener) {
                        onClickRecyclerItemListener.onItemDeleteImageViewClick(imageViewHolder.iv_image_delete,
                                imageViewHolder.getAdapterPosition());
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
                    if (null != onClickRecyclerItemListener) {
                        onClickRecyclerItemListener.onItemClick(view, getAdapterPosition());
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
                    if (null != onClickRecyclerItemListener) {
                        onClickRecyclerItemListener.onItemClick(view, getAdapterPosition());
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
    public void addMoreItem(List<NewsCollect> newDatas) {
        lists.addAll(newDatas);
        notifyDataSetChanged();
    }

    // 在第一个位置添加数据
    public void addData(int position, NewsCollect newDatas) {
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
