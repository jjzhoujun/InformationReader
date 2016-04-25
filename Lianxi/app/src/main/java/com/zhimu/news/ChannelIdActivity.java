package com.zhimu.news;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pgyersdk.crash.PgyCrashManager;
import com.zhimu.news.adapter.DragGridViewAdapter;
import com.zhimu.news.adapter.OtherGridViewAdapter;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.base.BaseActivity;
import com.zhimu.news.channel.ChannelIdManager;
import com.zhimu.news.channel.ChannelItem;
import com.zhimu.news.channel.SQLHelper;
import com.zhimu.news.view.DragItemGridView;
import com.zhimu.news.view.OtherGridView;

import java.util.ArrayList;

/**
 * 频道管理界面：可拖拽排序、删除
 * 参考一位网友的例子：http://blog.csdn.net/vipzjyno1/article/details/26514543
 * 感谢作者：vipra
 */
public class ChannelIdActivity extends BaseActivity implements OnItemClickListener {

    /**
     * 用户栏目的GRIDVIEW
     */
    private DragItemGridView userGridView;
    /**
     * 其它栏目的GRIDVIEW
     */
    private OtherGridView otherGridView;
    /**
     * 用户栏目对应的适配器，可以拖动
     */
    DragGridViewAdapter userAdapter;
    /**
     * 其它栏目对应的适配器
     */
    OtherGridViewAdapter otherGridViewAdapter;
    /**
     * 其它栏目列表
     */
    ArrayList<ChannelItem> otherChannelList = new ArrayList<>();
    /**
     * 用户栏目列表
     */
    ArrayList<ChannelItem> userChannelList = new ArrayList<>();
    /**
     * 是否在移动，由于这边是动画结束后才进行的数据更替，设置这个限制为了避免操作太频繁造成的数据错乱。
     */
    boolean isMove = false;
    private TextView tv_compile;

    // 是否点击了“编辑”
    boolean isClick = false;
    private TextView tv_darg;
    private Toolbar mToolbar;
    private SQLHelper sqlHelper;

    // 标记
    public static final int CHANNEL_RESULTCODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acttivity_channel_id);

        sqlHelper = MyApplication.getInstance().getSQLHelper();

        initView();
        initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) findViewById(R.id.toolbar_title);
        userGridView = (DragItemGridView) findViewById(R.id.userGridView);
        otherGridView = (OtherGridView) findViewById(R.id.otherGridView);
        tv_darg = (TextView) findViewById(R.id.tv_darg);
        tv_compile = (TextView) findViewById(R.id.tv_compile);

        if (mTitle != null) {
            mTitle.setText(R.string.channel_tool_title);
        }

        mToolbar.setTitle("");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    /**
     * 初始化数据
     */
    private void initData() {
        userChannelList = ((ArrayList<ChannelItem>) ChannelIdManager.getInstance(MyApplication.getInstance().getSQLHelper()).getUserChannel());
        otherChannelList = ((ArrayList<ChannelItem>) ChannelIdManager.getInstance(MyApplication.getInstance().getSQLHelper()).getOtherChannel());
        userAdapter = new DragGridViewAdapter(this, userChannelList);
        userGridView.setAdapter(userAdapter);
        otherGridViewAdapter = new OtherGridViewAdapter(this, otherChannelList);
        otherGridView.setAdapter(otherGridViewAdapter);

        // 设置GRIDVIEW的ITEM的点击监听
        otherGridView.setOnItemClickListener(this);
        userGridView.setOnItemClickListener(this);

        // 设置监听事件
        tv_compile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isClick) {
                    tv_compile.setText(R.string.channel_compile);
                    tv_darg.setVisibility(View.VISIBLE);
                    isClick = true;

                    // 当点击编辑时，item背景色变为圆角边框红色
                    userAdapter.setItemColor(true);

                } else {
                    tv_compile.setText(R.string.channel_compiled);
                    tv_darg.setVisibility(View.INVISIBLE);
                    isClick = false;

                    userAdapter.setItemColor(false);

                    // 如果移动完成，再保存
                    if (!isMove) {
                        // 文字由完成变为编辑时，再保存数据:保存数据时，变慢，异步执行
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                saveChannel();
                            }
                        });
                    }
                }


            }
        });

        /**
         * 导航到上一页
         */
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChannelIdActivity.this, TabHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                setResult(CHANNEL_RESULTCODE);
                startActivity(intent);
                finish();
            }
        });
    }



    /**
     * GRIDVIEW对应的ITEM点击监听接口
     */
    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        // 如果点击的时候，之前动画还没结束，那么就让点击事件无效
        if (isMove) {
            return;
        }

        // 如果点击的时候，是编辑状态，那么就让点击事件无效
        if (!isClick) {
            return;
        }

        switch (parent.getId()) {
            case R.id.userGridView:
                // position为0的item不可以进行任何操作
                if (position != 0) {
                    final ImageView moveImageView = getView(view);
                    if (moveImageView != null) {
                        TextView newTextView = (TextView) view.findViewById(R.id.text_item);
                        final int[] startLocation = new int[2];
                        newTextView.getLocationInWindow(startLocation);
                        final ChannelItem channel = ((DragGridViewAdapter) parent.getAdapter()).getItem(position);//获取点击的频道内容
                        otherGridViewAdapter.setVisible(false);
                        //添加到最后一个
                        otherGridViewAdapter.addItem(channel);
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                try {
                                    int[] endLocation = new int[2];
                                    //获取终点的坐标
                                    otherGridView.getChildAt(otherGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
                                    MoveAnim(moveImageView, startLocation, endLocation, channel, userGridView);
                                    userAdapter.setRemove(position);
                                } catch (Exception localException) {

                                    // 处理异常：上传到蒲公英SDK
                                    PgyCrashManager.reportCaughtException(ChannelIdActivity.this, localException);
                                }
                            }
                        }, 50L);
                    }
                }
                break;
            case R.id.otherGridView:
                final ImageView moveImageView = getView(view);
                if (moveImageView != null) {
                    TextView newTextView = (TextView) view.findViewById(R.id.text_item);
                    final int[] startLocation = new int[2];
                    newTextView.getLocationInWindow(startLocation);
                    final ChannelItem channel = ((OtherGridViewAdapter) parent.getAdapter()).getItem(position);
                    userAdapter.setVisible(false);
                    //添加到最后一个
                    userAdapter.addItem(channel);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            try {
                                int[] endLocation = new int[2];
                                //获取终点的坐标
                                userGridView.getChildAt(userGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
                                MoveAnim(moveImageView, startLocation, endLocation, channel, otherGridView);
                                otherGridViewAdapter.setRemove(position);
                            } catch (Exception localException) {
                                // 处理异常：上传到蒲公英SDK
                                PgyCrashManager.reportCaughtException(ChannelIdActivity.this, localException);
                            }
                        }
                    }, 50L);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 点击ITEM移动动画
     *
     * @param moveView
     * @param startLocation
     * @param endLocation
     * @param moveChannel
     * @param clickGridView
     */
    private void MoveAnim(View moveView, int[] startLocation, int[] endLocation, final ChannelItem moveChannel,
                          final GridView clickGridView) {
        int[] initLocation = new int[2];
        // 获取传递过来的VIEW的坐标
        moveView.getLocationInWindow(initLocation);
        // 得到要移动的VIEW,并放入对应的容器中
        final ViewGroup moveViewGroup = getMoveViewGroup();
        final View mMoveView = getMoveView(moveViewGroup, moveView, initLocation);
        // 创建移动动画
        TranslateAnimation moveAnimation = new TranslateAnimation(
                startLocation[0], endLocation[0], startLocation[1],
                endLocation[1]);
        // 动画时间
        moveAnimation.setDuration(300L);
        // 动画配置
        AnimationSet moveAnimationSet = new AnimationSet(true);
        moveAnimationSet.setFillAfter(false);
        // 动画效果执行完毕后，View对象不保留在终止的位置
        moveAnimationSet.addAnimation(moveAnimation);
        mMoveView.startAnimation(moveAnimationSet);
        moveAnimationSet.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                isMove = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                moveViewGroup.removeView(mMoveView);
                // instanceof 方法判断2边实例是不是一样，判断点击的是DragGrid还是OtherGridView
                if (clickGridView instanceof DragItemGridView) {
                    otherGridViewAdapter.setVisible(true);
                    otherGridViewAdapter.notifyDataSetChanged();
                    userAdapter.remove();
                } else {
                    userAdapter.setVisible(true);
                    userAdapter.notifyDataSetChanged();
                    otherGridViewAdapter.remove();
                }
                isMove = false;
            }
        });
    }

    /**
     * 获取移动的VIEW，放入对应ViewGroup布局容器
     *
     * @param viewGroup
     * @param view
     * @param initLocation
     * @return
     */
    private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation) {
        int x = initLocation[0];
        int y = initLocation[1];
        viewGroup.addView(view);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLayoutParams.leftMargin = x;
        mLayoutParams.topMargin = y;
        view.setLayoutParams(mLayoutParams);
        return view;
    }

    /**
     * 创建移动的ITEM对应的ViewGroup布局容器
     */
    private ViewGroup getMoveViewGroup() {
        ViewGroup moveViewGroup = (ViewGroup) getWindow().getDecorView();
        LinearLayout moveLinearLayout = new LinearLayout(this);
        moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        moveViewGroup.addView(moveLinearLayout);
        return moveLinearLayout;
    }

    /**
     * 获取点击的Item的对应View，
     *
     * @param view
     * @return
     */
    private ImageView getView(View view) {
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(cache);
        return iv;
    }

    /**
     * 退出时候保存选择后数据库的设置
     */
    private void saveChannel() {

        ChannelIdManager.getInstance(sqlHelper).deleteAllChannel();
        ChannelIdManager.getInstance(sqlHelper).saveUserChannel(userAdapter.getChannnelLst());
        ChannelIdManager.getInstance(sqlHelper).saveOtherChannel(otherGridViewAdapter.getChannnelLst());
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(ChannelIdActivity.this, TabHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        setResult(CHANNEL_RESULTCODE);
        startActivity(intent);

        super.onBackPressed();
    }
}
