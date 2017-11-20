package com.locksdk.util;

import com.vise.baseble.utils.HexUtil;

/**
 * Created by Sujiayong on 2017/11/2.
 */

public class LockSDKHexUtil {
    /**
     * 把16进制字符串转换成字节数组
     *
     * @param hexString * @param toLowerCase <code>true</code> 传换成大写格式， <code>false</code> 传换成小写格式
     * @return byte[]
     */
    public static byte[] hexStringToByte(String hex, boolean lowerCase) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            if (lowerCase) {
                result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
            } else {
                result[i] = (byte) (toByte2(achar[pos]) << 4 | toByte2(achar[pos + 1]));
            }

        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    private static int toByte2(char c) {
        byte b = (byte) "0123456789abcdef".indexOf(c);
        return b;
    }
}
