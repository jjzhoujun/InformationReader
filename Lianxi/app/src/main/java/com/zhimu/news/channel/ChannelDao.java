package com.zhimu.news.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pgyersdk.crash.PgyCrashManager;
import com.zhimu.news.app.MyApplication;
import com.zhimu.news.impl.ChannelDaoInface;

/**
 * 数据库数据操作类：添加+删除+恢复
 */
public class ChannelDao implements ChannelDaoInface {
    private SQLHelper helper = null;

    public ChannelDao(Context context) {
        helper = new SQLHelper(context);
    }

    /**
     * 是否添加了一个item
     * @param item
     * @return
     */
    @Override
    public boolean addCache(ChannelItem item) {
        boolean flag = false;
        SQLiteDatabase database = null;
        long id = -1;
        try {
            database = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", item.getName());
            values.put("id", item.getId());
            values.put("orderId", item.getOrderId());
            values.put("selected", item.getSelected());
            // 把添加的该item加到数据库
            id = database.insert(SQLHelper.TABLE_CHANNEL, null, values);
            // id不等于-1，说明添加成功
            flag = (id != -1 ? true : false);
        } catch (Exception e) {

            PgyCrashManager.reportCaughtException(MyApplication.getInstance(), e);

        } finally {
            if (database != null) {
                database.close();
            }
        }
        return flag;
    }

    /**
     * 删除一个数据
     * @param whereClause
     *                  选择条件
     * @param whereArgs
     *                  具体哪个条件
     * @return
     */
    @Override
    public boolean deleteCache(String whereClause, String[] whereArgs) {
        boolean flag = false;
        SQLiteDatabase database = null;
        int count = 0;
        try {
            database = helper.getWritableDatabase();
            count = database.delete(SQLHelper.TABLE_CHANNEL, whereClause, whereArgs);
            flag = (count > 0 ? true : false);
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (database != null) {
                database.close();
            }
        }
        return flag;
    }

    /**
     * 更新数据
     * @param values
     * @param whereClause
     * @param whereArgs
     * @return
     */
    @Override
    public boolean updateCache(ContentValues values, String whereClause,
                               String[] whereArgs) {
        // TODO Auto-generated method stub
        boolean flag = false;
        SQLiteDatabase database = null;
        int count = 0;
        try {
            database = helper.getWritableDatabase();
            count = database.update(SQLHelper.TABLE_CHANNEL, values, whereClause, whereArgs);
            flag = (count > 0 ? true : false);
        } catch (Exception e) {
            PgyCrashManager.reportCaughtException(MyApplication.getInstance(), e);
        } finally {
            if (database != null) {
                database.close();
            }
        }
        return flag;
    }

    /**
     * 获取当前列
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public Map<String, String> viewCache(String selection,
                                         String[] selectionArgs) {
        // TODO Auto-generated method stub
        SQLiteDatabase database = null;
        Cursor cursor = null;
        Map<String, String> map = new HashMap<>();
        try {
            database = helper.getReadableDatabase();
            cursor = database.query(true, SQLHelper.TABLE_CHANNEL, null, selection,
                    selectionArgs, null, null, null, null);
            int cols_len = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                for (int i = 0; i < cols_len; i++) {
                    String cols_name = cursor.getColumnName(i);
                    String cols_values = cursor.getString(cursor
                            .getColumnIndex(cols_name));
                    if (cols_values == null) {
                        cols_values = "";
                    }
                    map.put(cols_name, cols_values);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            PgyCrashManager.reportCaughtException(MyApplication.getInstance(), e);
        } finally {
            if (database != null) {
                database.close();
            }
        }
        return map;
    }

    /**
     * 获取整个列
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public List<Map<String, String>> listCache(String selection, String[] selectionArgs) {
        List<Map<String, String>> list = new ArrayList<>();
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = helper.getReadableDatabase();
            cursor = database.query(false, SQLHelper.TABLE_CHANNEL, null, selection, selectionArgs, null, null, null, null);
            int cols_len = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < cols_len; i++) {

                    String cols_name = cursor.getColumnName(i);
                    String cols_values = cursor.getString(cursor
                            .getColumnIndex(cols_name));
                    if (cols_values == null) {
                        cols_values = "";
                    }
                    map.put(cols_name, cols_values);
                }
                list.add(map);
            }

        } catch (Exception e) {
            // TODO: handle exception
            PgyCrashManager.reportCaughtException(MyApplication.getInstance(), e);
        } finally {
            if (database != null) {
                database.close();
            }
        }
        return list;
    }

    /**
     * 清除所有的频道
     */
    public void clearFeedTable() {
        String sql = "DELETE FROM " + SQLHelper.TABLE_CHANNEL + ";";
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql);

        revertSeq();
    }

    /**
     * 恢复数据库
     */
    private void revertSeq() {
        String sql = "update sqlite_sequence set seq=0 where name='"
                + SQLHelper.TABLE_CHANNEL + "'";
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql);
    }

}
