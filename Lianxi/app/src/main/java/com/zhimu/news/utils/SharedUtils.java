package com.zhimu.news.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences保存数据
 * 
 */
public class SharedUtils {

	// xml保存文件名字
	public static final String SHARED_NAME = "news";

	// 是否自动检测更新key
	public static final String ISUPDATEAPP = "isUpdateApp";

	// 字体大小key
	public static final String TEXT_SIZE = "size";

	// 添加boolean
	public static boolean getSharedBoolean(Context context, String key, boolean defaultValue) {
		SharedPreferences mPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
		return mPreferences.getBoolean(key, defaultValue);
		
	}
	public static void setSharedBoolean(Context context, String key, boolean value) {
		SharedPreferences mPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
		mPreferences.edit().putBoolean(key, value).apply();
		
	}

	// 添加字符串
	public static void  setSharedString(Context context, String key, String value) {
		SharedPreferences mPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
		mPreferences.edit().putString(key, value).apply();
	}

	public static String getSharedString(Context context, String key, String defaultValue) {
		SharedPreferences mPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
		return mPreferences.getString(key, defaultValue);

	}

	// 添加int
	public static void  setSharedInt(Context context, String key, int value) {
		SharedPreferences mPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
		mPreferences.edit().putInt(key, value).apply();
	}

	public static int getSharedInt(Context context, String key, int defaultValue) {
		SharedPreferences mPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
		return mPreferences.getInt(key, defaultValue);

	}
}
