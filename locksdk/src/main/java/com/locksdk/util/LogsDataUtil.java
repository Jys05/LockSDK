package com.locksdk.util;

import com.locksdk.bean.LockLog;

/**
 * Created by Sujiayong on 2017/11/30.
 * 处理日志数据的工具类
 */

public class LogsDataUtil {

    public static LockLog dealLogsData(byte[] data) {
        byte[] btBoxName1 = new byte[16];
        System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
        byte[] btBoxName = DealtByteUtil.dataClear0(btBoxName1);
        String boxName = new String(btBoxName);
        byte[] callBackResult = new byte[2];        //返回的操作是否正确
        System.arraycopy(callBackResult , 0 , );
    }
}
