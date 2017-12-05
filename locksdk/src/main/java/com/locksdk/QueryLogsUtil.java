package com.locksdk;

import android.text.TextUtils;
import android.util.Log;

import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.QueryLogsListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.LockSDKHexUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.utils.HexUtil;

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
        if (Integer.valueOf(startSeq) > Integer.valueOf(endSeq)) {
            Log.e("====>", "大小不对");
            return;
        }
        byte[] btStartSeq = intToBytes2(Integer.valueOf(startSeq));
        byte[] btEndSeq = intToBytes2(Integer.valueOf(endSeq));
        Log.e(TAG, HexUtil.encodeHexStr(btStartSeq));
        Log.e(TAG, HexUtil.encodeHexStr(btEndSeq));
        byte[] data = new byte[2 + btEndSeq.length + btStartSeq.length];
        data[0] = 0x00;
        data[1] = 0x13;
        System.arraycopy(btStartSeq, 0, data, 2, btStartSeq.length);
        System.arraycopy(btEndSeq, 0, data, 2 + btStartSeq.length, btEndSeq.length);
        WriteAndNoficeUtil.getInstantce().writeFunctionCode(data[1], data, writeDataListener);
    }

    /**
     * 将int类型的数据转换为byte数组
     * 原理：将int数据中的四个byte取出，分别存储
     *
     * @param n int数据
     * @return 生成的byte数组
     */
    public static byte[] intToBytes2(int n) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (n >> (24 - i * 8));
        }
        byte[] result = new byte[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = b[result.length-1-i];
        }
        return result;
    }

    private static WriteDataListener writeDataListener = new WriteDataListener() {
        @Override
        public void onWirteSuccess(WriteCallbackData callbackData) {
            if (callbackData != null && callbackData.getData() != null) {
                Log.i(TAG + "======>", callbackData.getData().length + "---" + HexUtil.encodeHexStr(callbackData.getData()));
            }
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
