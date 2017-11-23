package com.locksdk.util;

/**
 * Created by Sujiayong on 2017/11/22.
 * 进制转换
 */

public class Util {

    public static String HtoB(String a) {
        String b = Integer.toBinaryString(Integer.valueOf(toD(a, 16)));
        return b;
    }

    /**
     * 任意进制换十进制
     *
     * @param a
     * @param b 当前进制数
     * @return
     */
    public static String toD(String a, int b) {
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r = (int) ((r + formatting(a.substring(i, i + 1))) * Math.pow(b, a.length() - i - 1));
        }
        return String.valueOf(r);
    }

    public static int formatting(String a) {
        int i = 0;
        for (int u = 0; u < 10; u++) {
            if (a.equals(String.valueOf(u))) {
                i = u;
            }
        }
        if (a.equals("a") || a.equals("A")) {
            i = 10;
        }
        if (a.equals("b") || a.equals("B")) {
            i = 11;
        }
        if (a.equals("c") || a.equals("C")) {
            i = 12;
        }
        if (a.equals("d") || a.equals("D")) {
            i = 13;
        }
        if (a.equals("e") || a.equals("E")) {
            i = 14;
        }
        if (a.equals("f") || a.equals("F")) {
            i = 15;
        }
        return i;
    }
}
