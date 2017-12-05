package com.locksdk.util;

import android.util.Log;

import com.locksdk.baseble.utils.HexUtil;
import com.locksdk.bean.LockLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sujiayong on 2017/11/30.
 * 处理日志数据的工具类
 */

public class LogsDataUtil {
    private static final String TAG = "LogsDataUtil";

    public static List<LockLog> dealLogsData(byte[] data) {
        byte[] btBoxName1 = new byte[16];
        System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
        byte[] btBoxName = DealtByteUtil.dataClear0(btBoxName1);
        String boxName = new String(btBoxName);

        byte[] callBackResult = new byte[2];        //返回的操作是否正确
        System.arraycopy(data, 16, callBackResult, 0, callBackResult.length);
        Log.e(TAG, "返回的操作是否正确：" + callBackResult.length + "====>" + HexUtil.encodeHexStr(callBackResult));
        int callBack = Integer.valueOf(HexUtil.encodeHexStr(callBackResult));
        List<LockLog> logsDataLists = new ArrayList<>();
        if (callBack == 0) {
            byte btLogNum = data[18];     //日志数量
            int intLogNum = btLogNum & 0xFF;
            byte[] logDatas = new byte[data.length - 19];
            System.arraycopy(data, 19, logDatas, 0, logDatas.length);
            Log.e(TAG, "日志个数：" + (logDatas.length / 28) + "==日志数据：==>" + HexUtil.encodeHexStr(logDatas));
            //TODO : 2017/12/1 此判断不知道需不需要
//            if (intLogNum != logDatas.length / 28) return null;
//            List<byte[]> logsDataLists = new ArrayList<>();
            for (int i = 0; i < logDatas.length / 28; i++) {
                byte[] logData = new byte[28];
                System.arraycopy(logDatas, i * 28, logData, 0, logData.length);
                Log.e(TAG, HexUtil.encodeHexStr(logData));
                //操作类型
                byte btOptType = logData[4];
                String strOptType = getOptTyp(btOptType);
                Log.e(TAG, "操作类型：" + strOptType + "===" + (btOptType & 0xFF));
                //操作时间
                byte[] operationTime = new byte[7];
                System.arraycopy(logData, 5, operationTime, 0, operationTime.length);
                String strOptTime = BCDCodeUtil.bcd2Str(operationTime);
                Log.e(TAG, "操作时间：" + strOptTime);
                //用户ID
                byte[] btUserId = new byte[16];
                System.arraycopy(logData, 12, btUserId, 0, btUserId.length);
                String strUserId = new String(DealtByteUtil.dataClear0(btUserId));
                Log.e(TAG, "操作UserID：" + strUserId+"===="+HexUtil.encodeHexStr(btUserId));
                LockLog lockLog = new LockLog();
                lockLog.setOptType(strOptType);
                lockLog.setOptTime(strOptTime);
                lockLog.setUserId(strUserId);
                logsDataLists.add(lockLog);
            }

        }
        return logsDataLists;
    }

    //获取操作类型
    private static String getOptTyp(byte btOptType) {
        String strOptType = "";
        switch (btOptType) {
            case (byte) 0x01:
                strOptType = "开锁";
                break;
            case (byte)0x02:
                strOptType = "关锁";
                break;
            case (byte)0x03:
                strOptType = "激活";
                break;
            case (byte)0x07:
                strOptType = "开锁授权失败";
                break;
            case (byte)0x25:
                strOptType = "移动报警";
                break;
            case (byte)0x26:
                strOptType = "非法开箱";
                break;
            case (byte)0x27:
                strOptType = "开箱超时";
                break;
        }
        return strOptType;
    }
}
