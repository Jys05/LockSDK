package com.locksdk.util;

import android.util.Log;

/**
 * Created by Sujiayong on 2017/11/23.
 * 功能码转功能码
 */

public class FunCode2RespCode {

    private static final String TAG = "FunCode2RespCode";

    public static byte funCode2RespCode(byte funCode) {
        byte respCode = 0x00;
        String funMsg = "";
        switch (funCode) {
            case 0x10:
                funMsg = "初始化锁/激活";
                respCode = (byte) 0x90;
                break;
            case 0x11:
                funMsg = "开锁触发/获取随机数";
                respCode = (byte) 0x91;
                break;
            case 0x12:
                funMsg = "请求开锁/开锁";
                respCode = (byte) 0x92;
                break;
            case 0x13:
                funMsg = "读日志/查询日志";
                respCode = (byte) 0x93;
                break;
            case 0x14:
                funMsg = "查询锁具状态/查询锁具状态";
                respCode = (byte) 0x94;
                break;
            default:
                funMsg = "功能码错误！";
                respCode = 0x00;
                break;

        }
        LogUtil.i(TAG, "响应码：" + respCode + "功能信息：" + funMsg);
        return respCode;
    }
}
