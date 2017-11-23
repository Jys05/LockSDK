package com.locksdk;

import android.nfc.Tag;
import android.util.Log;

import com.locksdk.util.Util;
import com.vise.baseble.utils.HexUtil;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sujiayong on 2017/11/20.
 * 对数据分包处理
 */

public class DealDataUtil {

    private static final String TAG = "DealDataUtil";
    private static DealDataUtil dealDataUtil;

    private DealDataUtil() {
    }

    public static DealDataUtil getIntantce() {
        if (dealDataUtil == null) {
            synchronized (DealDataUtil.class) {
                if (dealDataUtil == null) {
                    dealDataUtil = new DealDataUtil();
                }
            }
        }
        return dealDataUtil;
    }

    /**
     * 分包逻辑
     *
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

    public static Map<Byte, byte[]> callbackDataMap = new HashMap<>();
    private static byte[] data;
    private static int pakeSize = 0;//报文分包的总个数
    private static int surplusPakeSize = 0;//剩余报文的包个数
    public static byte ressonpCode;            //应答码


    /**
     * @param callbackData
     * @return      true：组包完成
     */
    //组包
    public static boolean dealtDealData(byte[] callbackData) {
        if (callbackData[0] == 0x00) {
            data = new byte[callbackData.length - 1];
            System.arraycopy(callbackData, 1, data, 0, data.length);
            ressonpCode = callbackData[1];
            callbackDataMap.put(callbackData[1], data);
            return true;
        }
        if (callbackData[0] == ((byte) 0x80)) {
            int number = (((callbackData[1] << 8) & 0xFFFF) + (callbackData[2] & 0xFF));
            Log.e(TAG, "报文长度：" + number);
            data = new byte[number - 1];
            ressonpCode = callbackData[3];
            callbackDataMap.put(ressonpCode, data);
            System.arraycopy(callbackData, 4, data, 0, callbackData.length - 4);
            pakeSize = (number - 17) / 19 + 1;  //报文分包的个数
            if ((number - 17) % 19 != 0) {
                pakeSize++;         //如果有余数，证明还需要分一个包；
            }
            surplusPakeSize = pakeSize - 1;       //剩余包数：（已经处理了0x80这个包），所以pakeSize-1
            Log.e(TAG, "，剩余的包数：" + surplusPakeSize);
            return false;
        } else if (surplusPakeSize != 0 && data != null && callbackData[0] == (byte) (0x80 + (pakeSize - surplusPakeSize))) {
            System.arraycopy(callbackData, 1, data, 16 + (pakeSize - surplusPakeSize - 1) * 19, callbackData.length - 1);
            surplusPakeSize--;
            return false;
        } else if (surplusPakeSize != 0 && callbackData[0] == (byte) (0x00 + (pakeSize - surplusPakeSize))) {
            //最后一个包：
            System.arraycopy(callbackData, 1, data, 16 + (pakeSize - surplusPakeSize - 1) * 19, callbackData.length - 1);
            surplusPakeSize--;
//            Log.e(TAG, HexUtil.encodeHexStr(data) + "=====" + data.length);
            callbackDataMap.put(ressonpCode, data);
            return true;
        }else {
            //有错误
            //TODO : 2017/11/23 对错误未处理
            return true;
        }
    }
}
