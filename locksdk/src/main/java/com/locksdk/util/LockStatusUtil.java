package com.locksdk.util;

import android.util.Log;

import com.locksdk.baseble.utils.HexUtil;
import com.locksdk.bean.LockStatus;

/**
 * Created by Mr.Su on 2017/11/26.
 * 对后台返回回来的数据，处理成锁具状态LockStatus对象
 */

public class LockStatusUtil {

    private static final String TAG = "LockStatusUtil";

    public static LockStatus getBoxStatus(byte btLockStatus1, byte btLockStatus2) {
        char[] binaryStatus1 = byte2Binary(btLockStatus1);
        char[] binaryStatus2 = byte2Binary(btLockStatus2);
        if (binaryStatus1.length > 8) return null;
        if (binaryStatus2.length > 8) return null;
        char[] charLockStatus1 = new char[]{'0', '0', '0', '0', '0', '0', '0', '0'};
        System.arraycopy(binaryStatus1, 0, charLockStatus1, 8 - binaryStatus1.length, binaryStatus1.length);
        char[] charLockStatus2 = new char[]{'0', '0', '0', '0', '0', '0', '0', '0'};
        System.arraycopy(binaryStatus2, 0, charLockStatus2, 8 - binaryStatus2.length, binaryStatus2.length);

        /******************************/
        //TODO : 2017/11/30 测试
        for (int i = 0; i < charLockStatus1.length; i++) {
            Log.e(TAG, charLockStatus1[i] + "");
        }
        Log.e(TAG, "====" + charLockStatus2.length);
        for (int i = 0; i < charLockStatus2.length; i++) {
            Log.e(TAG, charLockStatus2[i] + "");
        }
        /******************************/
        LockStatus lockStatus = new LockStatus();
        lockStatus.setLockedAlarm(charLockStatus1[0]);
        lockStatus.setVibrateAlarm(charLockStatus1[1]);
        lockStatus.setCloseTimoutAlarm(charLockStatus1[2]);
        lockStatus.setBoxStatus(charLockStatus1[3]);
        lockStatus.setSwitchError(charLockStatus1[4]);
        lockStatus.setLockStatus(charLockStatus1[5]);
        lockStatus.setBatteryLevel((charLockStatus1[6] + "") + (charLockStatus1[7] + ""));
        //款箱状态2
        lockStatus.setShelfStatus(charLockStatus2[4]);
        lockStatus.setAlarmSwitchStatus(charLockStatus2[5]);
        return lockStatus;
    }

    /**
     * 字节转二进制
     *
     * @return
     */
    private static char[] byte2Binary(byte data) {
        char[] result = Integer.toBinaryString((data & 0xFF)).toCharArray();
        return result;
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
