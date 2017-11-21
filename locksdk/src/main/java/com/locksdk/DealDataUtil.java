package com.locksdk;

import android.util.Log;

import com.vise.baseble.utils.HexUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sujiayong on 2017/11/20.
 * 对数据分包处理
 */

public class DealDataUtil {

    /**
     * 分包逻辑
     * @param data
     * @return
     */
    public static List<byte[]> dealData(byte[] data) {
        List<byte[]> result = new ArrayList<>();
        if (data.length > 20) {
            byte[] data1 = new byte[17];
            byte[] data2 = new byte[data.length - 17];
            System.arraycopy(data, 0, data1, 0, data1.length);
            System.arraycopy(data, 17, data2, 0, data2.length);
            result.add(data1);
            if (data2.length > 19) {
                int length = data2.length;
                int position = length / 19;        //看有多少个19字节数组
                for (int i = 0; i < position; i++) {
                    byte[] data3 = new byte[19];
                    System.arraycopy(data2, i * data3.length, data3, 0, data3.length);
                    result.add(data3);
                }
                int lastPostion = length % 19;          //最后剩多少个字节
                if (lastPostion != 0) {
                    byte[] dataLast = new byte[lastPostion];
                    System.arraycopy(data2, 19 * position, dataLast, 0, dataLast.length);
                    result.add(dataLast);
                }
            } else {
                result.add(data2);
            }
        } else {
            result.add(data);
        }
        return result;
    }


}
