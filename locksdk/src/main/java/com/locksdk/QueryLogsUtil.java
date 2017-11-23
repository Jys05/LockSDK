package com.locksdk;

import android.text.TextUtils;
import android.util.Log;

import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.QueryLogsListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.LockSDKHexUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.vise.baseble.utils.HexUtil;

import java.util.Map;

/**
 * Created by Sujiayong on 2017/11/21.
 * 查询日志
 */

public class QueryLogsUtil {

    private static QueryLogsListener queryLogsListener;
    private static final String TAG = "QueryLogsUtil";

    public static void queryLogs(Map<String, String> param, QueryLogsListener logsListener) {
        queryLogsListener = logsListener;
        String lockId = param.get("lockId");
        String startSeq = param.get("startSeq");
        String endSeq = param.get("endSeq");
        if (TextUtils.isEmpty(startSeq)) return;
        if (Integer.valueOf(startSeq) > Integer.valueOf(endSeq)) return;
        byte[] btStartSeq = LockSDKHexUtil.hexStringToByte(startSeq, true);
        byte[] btEndSeq = LockSDKHexUtil.hexStringToByte(endSeq, true);
        Log.e(TAG, HexUtil.encodeHexStr(btStartSeq));
        Log.e(TAG, HexUtil.encodeHexStr(btEndSeq));
        byte[] data = new byte[2 + btEndSeq.length + btStartSeq.length];
        data[0] = 0x00;
        data[1] = 0x13;
        System.arraycopy(btStartSeq, 0, data, 2, btStartSeq.length);
        System.arraycopy(btEndSeq, 0, data, 2 + btStartSeq.length, btEndSeq.length);
        WriteAndNoficeUtil.getInstantce().writeFunctionCode(data[1], data, writeDataListener);
    }

    private static WriteDataListener writeDataListener = new WriteDataListener() {
        @Override
        public void onWirteSuccess(WriteCallbackData data) {

        }

        @Override
        public void onWriteFail(WriteCallbackData data) {

        }
    };


    /*************数据************/
    // 参数名称	参数名	是否必填	长度	说明
    // 锁具ID	lockId	是	24
    // 起始序号	startSeq	是
    // 结束序号	endSeq	是		endSeq-startSeq<=20;返回[startSeq，endSeq)间的日志


}
