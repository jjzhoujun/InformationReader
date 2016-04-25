package com.zhimu.news.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 获取不同定义的时间类型
 * Created by Administrator on 2016.4.17.
 */
public class DateUtils {

    /**
     * 日期
     * @param template 日期类型
     * @return date
     */
    public String getDate(String template) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(template, Locale.CHINA);
        Date date = new Date(System.currentTimeMillis());
        return dateFormat.format(date);
    }

}
