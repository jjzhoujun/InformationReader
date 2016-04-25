package com.zhimu.news.channel;

import android.database.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 频道管理类：对当前的界面进行管理：获取当前列表+删除+添加
 * Created by Administrator on 2016.3.8.
 */
public class ChannelIdManager {
    public static ChannelIdManager manager;
    /**
     * 默认的用户选择频道列表
     */
    public static List<ChannelItem> defaultUserChannels;
    /**
     * 默认的其他频道列表
     */
    public static List<ChannelItem> defaultOtherChannels;

    // 数据库数据
    private ChannelDao channelDao;
    /**
     * 判断数据库中是否存在用户数据
     */
    private boolean userExist = false;

    static {
        defaultUserChannels = new ArrayList<>();
        defaultOtherChannels = new ArrayList<>();

        // 已选择：如果会继续添加栏目，这样写全部要改，怎么优化？
        defaultUserChannels.add(new ChannelItem(1, "推荐", 1, 1));
        defaultUserChannels.add(new ChannelItem(2, "段子", 2, 1));
        defaultUserChannels.add(new ChannelItem(3, "趣图", 3, 1));
        defaultUserChannels.add(new ChannelItem(4, "互联网", 4, 1));
        defaultUserChannels.add(new ChannelItem(5, "房产", 5, 1));
        defaultUserChannels.add(new ChannelItem(6, "汽车", 6, 1));
        defaultUserChannels.add(new ChannelItem(7, "体育", 7, 1));
        defaultUserChannels.add(new ChannelItem(8, "娱乐", 8, 1));
        defaultUserChannels.add(new ChannelItem(9, "理财", 9, 1));
        defaultUserChannels.add(new ChannelItem(10, "财经", 10, 1));
        defaultUserChannels.add(new ChannelItem(11, "经济", 11, 1));

        // 未选择
        defaultOtherChannels.add(new ChannelItem(12, "国内", 1, 0));
        defaultOtherChannels.add(new ChannelItem(13, "国际", 2, 0));
        defaultOtherChannels.add(new ChannelItem(14, "港澳台", 3, 0));
        defaultOtherChannels.add(new ChannelItem(15, "社会", 4, 0));
        defaultOtherChannels.add(new ChannelItem(16, "军事", 5, 0));
        defaultOtherChannels.add(new ChannelItem(17, "游戏", 6, 0));
        defaultOtherChannels.add(new ChannelItem(18, "教育", 7, 0));
        defaultOtherChannels.add(new ChannelItem(19, "女人", 8, 0));
        defaultOtherChannels.add(new ChannelItem(20, "科技", 9, 0));
        defaultOtherChannels.add(new ChannelItem(21, "数码", 10, 0));
        defaultOtherChannels.add(new ChannelItem(22, "电脑", 11, 0));
        defaultOtherChannels.add(new ChannelItem(23, "科普", 12, 0));
        defaultOtherChannels.add(new ChannelItem(24, "足球", 13, 0));
        defaultOtherChannels.add(new ChannelItem(25, "CBA", 14, 0));
        defaultOtherChannels.add(new ChannelItem(26, "电影", 15, 0));
        defaultOtherChannels.add(new ChannelItem(27, "电视", 16, 0));
        defaultOtherChannels.add(new ChannelItem(28, "养生", 17, 0));
        defaultOtherChannels.add(new ChannelItem(29, "情感", 18, 0));
        defaultOtherChannels.add(new ChannelItem(30, "美容", 19, 0));

    }

    private ChannelIdManager(SQLHelper paramDBHelper) throws SQLException {
        if (channelDao == null)
            channelDao = new ChannelDao(paramDBHelper.getContext());
//            channelDao = new ChannelDao(paramDBHelper.getContext());
        // NavigateItemDao(paramDBHelper.getDao(NavigateItem.class));
        return;
    }

    /**
     * 初始化频道管理类
     *
     * @param dbHelper
     * @throws SQLException
     */
    public static ChannelIdManager getInstance(SQLHelper dbHelper) throws SQLException {
        if (manager == null)
            manager = new ChannelIdManager(dbHelper);
        return manager;
    }

    /**
     * 清除所有的频道
     */
    public void deleteAllChannel() {
        channelDao.clearFeedTable();
    }

    /**
     * 获取其他的频道
     *
     * @return 数据库存在用户配置 ? 数据库内的用户选择频道 : 默认用户选择频道;
     */
    public List<ChannelItem> getUserChannel() {
        Object cacheList = channelDao.listCache(SQLHelper.SELECTED + "= ?", new String[]{"1"});
        if (cacheList != null && !((List) cacheList).isEmpty()) {
            userExist = true;
            List<Map<String, String>> maplist = (List<Map<String, String>>) cacheList;
            int count = maplist.size();
            List<ChannelItem> list = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                ChannelItem navigate = new ChannelItem();
                navigate.setId(Integer.valueOf(maplist.get(i).get(SQLHelper.ID)));
                navigate.setName(maplist.get(i).get(SQLHelper.NAME));
                navigate.setOrderId(Integer.valueOf(maplist.get(i).get(SQLHelper.ORDERID)));
                navigate.setSelected(Integer.valueOf(maplist.get(i).get(SQLHelper.SELECTED)));
                list.add(navigate);
            }
            return list;
        }
        // 初始化默认频道
        initDefaultChannel();
        return defaultUserChannels;
    }

    /**
     * 获取其他的频道
     *
     * @return 数据库存在用户配置 ? 数据库内的其它频道 : 默认其它频道 ;
     */
    public Object getOtherChannel() {
        Object cacheList = channelDao.listCache(SQLHelper.SELECTED + "= ?", new String[]{"0"});
        List<ChannelItem> list = new ArrayList<>();
        if (cacheList != null && !((List) cacheList).isEmpty()) {
            List<Map<String, String>> maplist = (List<Map<String, String>>) cacheList;
            int count = maplist.size();
            for (int i = 0; i < count; i++) {
                ChannelItem navigate = new ChannelItem();
                navigate.setId(Integer.valueOf(maplist.get(i).get(SQLHelper.ID)));
                navigate.setName(maplist.get(i).get(SQLHelper.NAME));
                navigate.setOrderId(Integer.valueOf(maplist.get(i).get(SQLHelper.ORDERID)));
                navigate.setSelected(Integer.valueOf(maplist.get(i).get(SQLHelper.SELECTED)));
                list.add(navigate);
            }
            return list;
        }
        if (userExist) {
            return list;
        }
        cacheList = defaultOtherChannels;
        return cacheList;
    }

    /**
     * 保存用户频道到数据库
     *
     * @param userList
     */
    public void saveUserChannel(List<ChannelItem> userList) {
        for (int i = 0; i < userList.size(); i++) {
            ChannelItem channelItem = userList.get(i);
            channelItem.setOrderId(i);
            channelItem.setSelected(Integer.valueOf(1));
            channelDao.addCache(channelItem);
        }
    }

    /**
     * 保存其他频道到数据库
     *
     * @param otherList
     */
    public void saveOtherChannel(List<ChannelItem> otherList) {
        for (int i = 0; i < otherList.size(); i++) {
            ChannelItem channelItem = otherList.get(i);
            channelItem.setOrderId(i);
            channelItem.setSelected(Integer.valueOf(0));
            channelDao.addCache(channelItem);
        }
    }

    /**
     * 初始化数据库内的频道数据
     */
    private void initDefaultChannel() {
        deleteAllChannel();
        saveUserChannel(defaultUserChannels);
        saveOtherChannel(defaultOtherChannels);
    }
}
