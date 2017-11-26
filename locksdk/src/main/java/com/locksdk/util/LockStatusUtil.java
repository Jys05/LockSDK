package com.locksdk.util;

import com.locksdk.baseble.utils.HexUtil;
import com.locksdk.bean.LockStatus;

/**
 * Created by Mr.Su on 2017/11/26.
 * 对后台返回回来的数据，处理成锁具状态LockStatus对象
 */

public class LockStatusUtil {

    public static LockStatus getBoxStatus(byte[] btLockStatus) {
        if (btLockStatus.length != 2) return null;
        char[] charLockStatus = HToB(HexUtil.encodeHexStr(btLockStatus));
        if (charLockStatus.length != 16) return null;
        LockStatus lockStatus = new LockStatus();
        lockStatus.setLockedAlarm(charLockStatus[0]);
        lockStatus.setVibrateAlarm(charLockStatus[1]);
        lockStatus.setAlarmSwitchStatus(charLockStatus[2]);
        lockStatus.setBoxStatus(charLockStatus[3]);
        lockStatus.setSwitchError(charLockStatus[4]);
        lockStatus.setLockStatus(charLockStatus[5]);
        lockStatus.setShelfStatus(charLockStatus[12]);
        lockStatus.setBatteryLevel((charLockStatus[6] + "") + (charLockStatus[7] + ""));
        return lockStatus;
    }


    // 十六进制转二进制
    private static char[] HToB(String a) {
        String b = Integer.toBinaryString(Integer.valueOf(toD(a, 16)));
        return b.toCharArray();
    }

    // 任意进制数转为十进制数
    public static String toD(String a, int b) {
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r = (int) (r + formatting(a.substring(i, i + 1))
                    * Math.pow(b, a.length() - i - 1));
        }
        return String.valueOf(r);
    }

    private static int formatting(String a) {
        int i = 0;

        for (int u = 0; u < 10; ++u) {
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
