package com.locksdk.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zhangdeming
 * @date 创建时间 2016/9/23
 * @description 描述类的功能
 */

public class DateUtil {
    public static final String yyMMdd = "yy-MM-dd";
    public static final String yyyyMMdd = "yyyy-MM-dd";
    public static final String HHmmss = "HH:mm:ss";
    public static final String HHmmss_not = "HHmmss";
    public static final String HHmm = "HH:mm";
    public static final String yyMMddHHmmss = "yy-MM-dd HH:mm:ss";
    public static final String yyyyMMddHHmm = "yyyy-MM-dd HH:mm";
    public static final String yyMMddHHmm = "yy-MM-dd HH:mm";
    public static final String yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
    public static final String MMddHHmm = "MM-dd hh:mm";
    public static final String MMddHHmmss = "MM-dd hh:mm:ss";

    public static final String yyyyMMddHHmmss_not = "yyyyMMddHHmmss";

    public static final String MMdd = "MM-dd";
    public static final String yyyyMM = "yyyy-MM";

    public static final String yyyymm = "yyyy-MM";
    public static final String yyyymmdd = "yyyy-MM-dd";
    public static final String yyyymmdd_not = "yyyyMMdd";

    public static String format(String format, long stamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String time = "";
        try {
            Date date = new Date();
            date.setTime(stamp);
            time = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    public static long getTimeStamp(String format, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date date = sdf.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取当前的时间戳
     *
     * @return
     */
    public static long getNowTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
        if (day_of_week == Calendar.SUNDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -2);
        } else if (day_of_week == Calendar.SATURDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        return calendar.getTimeInMillis();

    }
}
