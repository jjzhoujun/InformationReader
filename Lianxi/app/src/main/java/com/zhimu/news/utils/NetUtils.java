package com.zhimu.news.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.zhimu.news.R;

/**
 * 判断网络工具类
 * Created by Administrator on 2016.3.12.
 */
public class NetUtils {

    /**
     * 检查当前网络是否连接
     */
    public static boolean isNetConnected(Context context) {

        boolean success = false;

        // 判断网络
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            // 获取不到时，很有可能为null
            if (networkInfo != null) {
                // 返回false，说明没有网络
                success = networkInfo.isAvailable();
            }
        }

        return success;
    }

    /**
     * 网络已经连接，判断是wifi还是gprs
     */
    public static void isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            boolean available = networkInfo.isAvailable();
            if (available) {
                // 获取手机连接状态
                NetworkInfo.State gprsState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
                NetworkInfo.State wifiState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

                if (gprsState == NetworkInfo.State.CONNECTED || gprsState == NetworkInfo.State.CONNECTING) {
                    Toast.makeText(context, R.string.toast_gprs, Toast.LENGTH_SHORT).show();
                }

                if (wifiState == NetworkInfo.State.CONNECTED || wifiState == NetworkInfo.State.CONNECTING) {
                    Toast.makeText(context, R.string.toast_wifi, Toast.LENGTH_SHORT).show();

                }
            }
        }

    }

}
