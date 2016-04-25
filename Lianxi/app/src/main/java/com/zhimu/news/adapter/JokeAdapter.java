package com.zhimu.news.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhimu.news.R;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.bean.JokeBean;
import com.zhimu.news.impl.OnClickRecyclerImageListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhimu.news.utils.SharedUtils;

import java.util.List;

/**
 * 笑话适配器：支持下拉刷新；上拉加载更多，但没有提示上拉加载
 * <p>
 * Created by Administrator on 2016.2.19.
 */
public class JokeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private DisplayImageOptions options;
    // 声明调用的数据类型
    private List<JokeBean.ShowapiResBody.Contentlist> lists;
    private OnClickRecyclerImageListener onClickRecyclerImageListener;
    private Context context;

    // 判断类型
    private static final int TEXT_TYPE = 1;
    private static final int IMAGE_TYPE = 2;
    private final ImageLoader imageLoader;

    public JokeAdapter(Context ctx, List<JokeBean.ShowapiResBody.Contentlist> contentLists) {
        lists = contentLists;
        context = ctx;

        options = MyApplication.getInstance().getOptions(R.mipmap.ic_big_bg);
        imageLoader = ImageLoader.getInstance();

    }

    /**
     * 接口回调
     *
     * @param onClickRecyclerImageListener 保存图片
     */
    public void setOnClickMyImageListener(OnClickRecyclerImageListener onClickRecyclerImageListener) {
        this.onClickRecyclerImageListener = onClickRecyclerImageListener;


    }

    /**
     * 返回不同的视图类型
     *
     * @param position 当前索引值
     * @return ViewType类型
     */
    @Override
    public int getItemViewType(int position) {

        // 1为文本笑话：有标题，有内容，无图片；2为图片笑话：有标题、图片、无内容
        int type = lists.get(position).getType();

        if (type == 1) {
            return TEXT_TYPE;
        } else {
            return IMAGE_TYPE;
        }

    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    /**
     * 根据类型创建不同的视图
     *
     * @param parent   父容器
     * @param viewType 视图类别
     * @return ViewHolder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TEXT_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.joke_text_recyclerview_item, parent, false);
            return new TextViewHolder(view);

        } else if (viewType == IMAGE_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.joke_image_recyclerview_item, parent, false);
            return new ImageViewHolder(view);
        }

        return null;
    }

    /**
     * 绑定数据
     *
     * @param holder   缓存
     * @param position 索引值
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        // 标题
        String title = lists.get(position).getTitle();

        // 时间：截取字符串ct='2015-12-31 20:00:13.709'
        String sub = lists.get(position).getCt().substring(5, 16);
        String subString = context.getString(R.string.joke_img_source_desc) + " " + sub;

        if (holder instanceof TextViewHolder) {

            TextViewHolder textViewHolder = (TextViewHolder) holder;

            // 标题
            textViewHolder.tv_joke_text_title.setText(title);
            // 设为粗体
//            textViewHolder.tv_joke_text_title.getPaint().setFakeBoldText(true);

            // 内容：里面有标签的需要去掉,有的有，有的没有
            String text = lists.get(position).getText();
            String replaceText = text.replace("</p>", "").replace("<p>", "");

            // 取出保存的值：默认在xml中保存的是16sp
            int size = SharedUtils.getSharedInt(context, SharedUtils.TEXT_SIZE, 0);
            switch (size) {
                case 14:
                    textViewHolder.tv_joke_text_content.setTextSize(size);
                    break;
                case 18:
                    textViewHolder.tv_joke_text_content.setTextSize(size);
                    break;
                case 20:
                    textViewHolder.tv_joke_text_content.setTextSize(size);
                    break;
                default:
                    break;
            }

            textViewHolder.tv_joke_text_content.setText(replaceText);

            // 时间
            textViewHolder.tv_joke_text_ct.setText(subString);

        } else if (holder instanceof ImageViewHolder) {

            final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;

            // 标题
            imageViewHolder.tv_joke_image_title.setText(title);
            // 设为粗体
//            imageViewHolder.tv_joke_image_title.getPaint().setFakeBoldText(true);

            // imageUrl代表图片的URL地址，imageView代表承载图片的IMAGEVIEW控件,
            // options代表DisplayImageOptions配置参数,这里没有配置，快速滑动时，加载有延时，图片错位替换
            imageLoader.displayImage(
                    lists.get(position).getImg(),
                    imageViewHolder.iv_joke_image, options);

            // 设置图片监听回调
            imageViewHolder.iv_joke_image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (null != onClickRecyclerImageListener) {

                        onClickRecyclerImageListener.onItemImageClick(imageViewHolder.iv_joke_image, position);
                    }
                    return true;
                }

            });

            // 时间
            imageViewHolder.tv_joke_image_ct.setText(subString);
        }
    }

    /**
     * 自定义的ViewHolder，持有每个Item的的所有界面元素
     */
    public static class TextViewHolder extends RecyclerView.ViewHolder {

        // 标题
        private final TextView tv_joke_text_title;
        // 内容
        private final TextView tv_joke_text_content;
        // 时间
        private final TextView tv_joke_text_ct;

        public TextViewHolder(View view) {
            super(view);
            tv_joke_text_title = (TextView) view.findViewById(R.id.tv_joke_text_title);
            tv_joke_text_content = (TextView) view.findViewById(R.id.tv_joke_text_content);
            tv_joke_text_ct = (TextView) view.findViewById(R.id.tv_joke_text_ct);
        }
    }

    /**
     * 图片ViewHolder
     */
    private static class ImageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iv_joke_image;
        private final TextView tv_joke_image_ct;
        private final TextView tv_joke_image_title;

        public ImageViewHolder(View itemView) {
            super(itemView);

            tv_joke_image_title = (TextView) itemView.findViewById(R.id.tv_joke_image_title);
            iv_joke_image = (ImageView) itemView.findViewById(R.id.iv_image_joke);
            tv_joke_image_ct = (TextView) itemView.findViewById(R.id.tv_image_ct);
        }
    }


    /**
     * 添加更多数据到最后面
     */
    public void addMoreItem(List<JokeBean.ShowapiResBody.Contentlist> newDatas) {
        lists.addAll(newDatas);
        notifyDataSetChanged();
    }

    // 在第一个位置添加数据
    public void addData(int position, JokeBean.ShowapiResBody.Contentlist newDatas) {
        lists.add(position, newDatas);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    /**
     * 删除数据，指定其位置
     */
    public void daleteData(int position) {
        lists.remove(position);
        notifyItemRemoved(position);
    }
}
