package com.zhimu.news.impl;

import java.util.List;
import java.util.Map;

/**
 * 频道数据库缓存管理接口
 */
import android.content.ContentValues;

import com.zhimu.news.channel.ChannelItem;

public interface ChannelDaoInface {
    /**
     * 添加一个item
     *
     * @param item
     * @return
     */
    boolean addCache(ChannelItem item);

    /**
     * 删除一个item
     *
     * @param whereClause
     * @param whereArgs
     * @return
     */
    boolean deleteCache(String whereClause, String[] whereArgs);

    /**
     * 更新item列表
     *
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    boolean updateCache(ContentValues values, String whereClause, String[] whereArgs);

    Map<String, String> viewCache(String selection, String[] selectionArgs);

    List<Map<String, String>> listCache(String selection, String[] selectionArgs);

    /**
     * 清除所有的频道
     */
    void clearFeedTable();
}
