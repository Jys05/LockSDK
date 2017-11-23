package com.locksdk.util;

import android.text.TextUtils;

/**
 * Created by Sujiayong on 2017/11/21.
 * 字符串转ASCII码——工具
 * 还包括补零
 */

public class ASCIICodeUtil {

    private static int char2ASCII(char c) {
        return (int) c;
    }

    public static byte[] str2ASCII(String str) {
        if (TextUtils.isEmpty(str)) return null;
        char[] chars = str.toCharArray();
        byte[] asciiArray = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            asciiArray[i] = (byte) char2ASCII(chars[i]);
        }
        return asciiArray;
    }

    public static byte[] dataAdd0(byte[] plaintData, int length) {
        if (plaintData.length < length) {
            byte[] dealtData = new byte[length];
            System.arraycopy(plaintData, 0, dealtData, 0, plaintData.length);
            for (int i = plaintData.length; i < dealtData.length; i++) {
                dealtData[i] = 0x00;
            }
            return dealtData;
        } else {
            return plaintData;
        }

    }
}
