package com.locksdk.util;

/**
 * Created by Sujiayong on 2017/11/22.
 * 对字节数组，做补零和去零操作
 */

public class DealtByteUtil {

    //补零
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

    //去零
    public static byte[] dataClear0(byte[] plaintData) {
        int postion = 0;
        for (int i = 0; i < plaintData.length; i++) {
            if (plaintData[i] != 0) {
                postion++;
            }
        }
        byte[] result = new byte[postion];
        System.arraycopy(plaintData, 0, result, 0, result.length);
        return result;
    }
}
