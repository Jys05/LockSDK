package com.locksdk.util;

import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * Created by Mr.Su on 2017/10/5.
 * 正则表达式工具类，包含常用正则表达式：
 * 1.手机号码 2.固定电话号码 3.邮箱 4.身份证号码18位
 * 5.正整数 6.负整数 7.整数 8.非负整数 9.非正整数 10.正浮点 11.负浮点
 */

public class RegexUtil {

    //手机号码 "\\d":任意一个十进制数字，相当于[0-9]
    public static final String REGEX_PHONE_NUM = "^[1]\\d{10}$";
    //固定电话号码
    public static final String REGEX_TEL = "^0\\d{2,3}[- ]?\\d{7,8}";
    //邮箱
    public static final String REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    //身份证号码18位
    public static final String REGEX_ID_CARD18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$";
    /*数值：*/
    //正整数
    public static final String REGEX_POSITIVE_INTEGER = "^[1-9]\\d*$";
    //负整数
    public static final String REGEX_NEGATIVE_INTEGER = "^-[0-9]\\d*$";
    //整数
    public static final String REGEX_INTEGER = "^(-?[1-9]\\d*)|0$";
    //非负整数(正整数 + 0)
    public static final String REGEX_NOT_NEGATIVE_INTEGER = "^[1-9]\\d*|0$";
    //非正整数（负整数 + 0）
    public static final String REGEX_NOT_POSITIVE_INTEGER = "^-[1-9]\\d*|0$";
    //浮点
    public static final String REGEX_FLOAT = "^-?([1-9]\\d*\\.\\d*)|(0\\.\\d*[1-9]\\d*)$";
    //正浮点数
    public static final String REGEX_POSITIVE_FLOAT = "^([1-9]\\d*\\.\\d*)|(0\\.\\d*[1-9]\\d*)$";
    //负浮点数
    public static final String REGEX_NEGATIVE_FLOAT = "^-[1-9]\\d*\\.\\d*|-0\\.\\d*[1-9]\\d*$";
    //长整型
    public static final String REGEX_LONG ="^-?\\d{0,20}$";



    /**
     * 根据"正则表达式"检验是否正确
     *
     * @param strRegex
     * @param value
     * @return
     */
    public static boolean matchesValue(String strRegex, CharSequence value) {
        return !TextUtils.isEmpty(strRegex)
                && !TextUtils.isEmpty(value)
                && Pattern.matches(strRegex, value);
    }

    /**
     * 是否是手机号码
     *
     * @param phoneNum
     * @return
     */
    public static boolean isPhoneNumber(CharSequence phoneNum) {
        return matchesValue(REGEX_PHONE_NUM, phoneNum);
    }

    /**
     * 是否是邮箱
     *
     * @param email
     * @return
     */
    public static boolean isEmail(CharSequence email) {
        return matchesValue(REGEX_EMAIL, email);
    }

    /**
     * 是否是固定电话
     *
     * @param telephone
     * @return
     */
    public static boolean isTelephone(CharSequence telephone) {
        return matchesValue(REGEX_TEL, telephone);
    }

    /**
     * 是否是18位身份证号码
     *
     * @param idCardNum
     * @return
     */
    public static boolean isIDCard(CharSequence idCardNum) {
        return matchesValue(REGEX_ID_CARD18, idCardNum);
    }

    /**
     * 是否是正整数
     *
     * @param positiveInteger
     * @return
     */
    public static boolean isPositiveInteger(CharSequence positiveInteger) {
        return matchesValue(REGEX_POSITIVE_INTEGER, positiveInteger);
    }

    /**
     * 是否是负整数
     *
     * @param negativeInteger
     * @return
     */
    public static boolean isNegativeInteger(CharSequence negativeInteger) {
        return matchesValue(REGEX_NEGATIVE_INTEGER, negativeInteger);
    }

    /**
     * 是否是整数
     *
     * @param integer
     * @return
     */
    public static boolean isInteger(CharSequence integer) {
        return matchesValue(REGEX_INTEGER, integer);
    }

    /**
     * 是否是非负整数
     *
     * @param notNegativeInteger
     * @return
     */
    public static boolean isNotNegativeInteger(CharSequence notNegativeInteger) {
        return matchesValue(REGEX_NOT_NEGATIVE_INTEGER, notNegativeInteger);
    }

    /**
     * 是否是非正整数
     *
     * @param notPositiveInteger
     * @return
     */
    public static boolean isNotPositiveInteger(CharSequence notPositiveInteger) {
        return matchesValue(REGEX_NOT_POSITIVE_INTEGER, notPositiveInteger);
    }

    /**
     * 是否是正浮点
     *
     * @param positiveFloat
     * @return
     */
    public static boolean isPositiveFloat(CharSequence positiveFloat) {
        return matchesValue(REGEX_POSITIVE_FLOAT, positiveFloat);
    }

    /**
     * 是否是负浮点
     *
     * @param negativeFloat
     * @return
     */
    public static boolean isNegativeFloat(CharSequence negativeFloat) {
        return matchesValue(REGEX_NEGATIVE_FLOAT, negativeFloat);
    }

    /**
     * 是否是浮点
     *
     * @param floatValue
     * @return
     */
    public static boolean isFloat(CharSequence floatValue) {
        return matchesValue(REGEX_FLOAT, floatValue);
    }

    /**
     * 是否是长整型
     *
     * @param longValue
     * @return
     */
    public static boolean isLong(CharSequence longValue) {
        return matchesValue(REGEX_LONG, longValue);
    }

}