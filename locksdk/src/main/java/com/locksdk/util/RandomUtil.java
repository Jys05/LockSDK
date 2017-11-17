package com.locksdk.util;

/**
 * Created by Sujiayong on 2017/10/25.
 * 随机数工具类
 */

public class RandomUtil {

    /**
     * 生成8字节的随机数
     * @return
     */
    public static byte[] getRandom() {
        long r = (long) (Long.MAX_VALUE * Math.random());
        byte[] targets = new byte[8];
        for (int i = 0; i <= targets.length - 1; i++) {
            int offset = i * 8;
            targets[i] = (byte) ((r >> offset) & 0xFF);
        }
        return targets;
    }

}
