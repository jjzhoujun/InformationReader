package com.zhimu.news.utils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 自定义请求数据
 * Created by Administrator on 2016.4.17.
 */
public class CommonRequest extends Request<String> {

    private Map<String, String> map;
    private Response.Listener<String> listener;

    public CommonRequest(int method, String url, Response.ErrorListener listener) {
        super(method, url, listener);
    }

    public CommonRequest(String url, Map<String, String> map,
                         Response.Listener<String> listener, Response.ErrorListener errorListener) {
        this(Method.POST, url, errorListener);

        this.map = map;
        this.listener = listener;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {

        try {
            String str = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {

            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {

        // 获取json格式的数据
        map.put("Accept", "application/json");
        map.put("Content-Type", "application/json;charset=UTF-8");

        // 系统时间
        map.put("showapi_timestamp", new DateUtils().getDate("yyyyMMddHHmmss"));
        // 添加易源接口请求参数
        map.put("showapi_appid", AllAppKeyUtils.SHOWAPI_APPID);
        map.put("showapi_sign", AllAppKeyUtils.SHOWAPI_SIGN);
        // 需要html格式内容
        map.put("needHtml", "1");

        return map;
    }
}
