package com.locksdk.lockApi;

import android.text.TextUtils;
import android.util.Log;

import com.locksdk.bean.LockLog;
import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.QueryLogsListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.LogUtil;
import com.locksdk.util.RegexUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.utils.HexUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by Sujiayong on 2017/11/21.
 * 查询日志
 */

public class QueryLogsUtil {

    private static QueryLogsListener queryLogsListener;
    private static final String TAG = "QueryLogsUtil";
    private static byte[] data;

    public static void queryLogs(Map<String, String> param, QueryLogsListener logsListener) {
        queryLogsListener = logsListener;
        String lockId = param.get("lockId");
        String startSeq = param.get("startSeq");
        String endSeq = param.get("endSeq");
        if (TextUtils.isEmpty(startSeq)) return;
        if (TextUtils.isEmpty(endSeq)) return;
        int startSeqNum = Integer.valueOf(startSeq);        //起始序号
        int endSeqNum = Integer.valueOf(endSeq);            //结束序号
        if (!RegexUtil.isNotNegativeInteger(startSeq) || !RegexUtil.isNotNegativeInteger(endSeq)) {
            LogUtil.i(TAG, Constant.MSG.MSG_START_END_FAIL2);
            Result<List<LockLog>> result = new Result<>();
            result.setCode(Constant.CODE.QUERY_LOGS_FAIL);
            result.setMsg(Constant.MSG.MSG_START_END_FAIL2);
            result.setData(null);
            logsListener.queryLogsCallback(result);
            return;
        }
//        if (startSeqNum > endSeqNum) {
//            LogUtil.i(TAG, Constant.MSG.MSG_START_END_FAIL);
//            Result<List<LockLog>> result = new Result<>();
//            result.setCode(Constant.CODE.QUERY_LOGS_FAIL);
//            result.setMsg(Constant.MSG.MSG_START_END_FAIL);
//            result.setData(null);
//            logsListener.queryLogsCallback(result);
//            return;
//        }
//        if ((endSeqNum-startSeqNum+1) > 10) {
//            LogUtil.i(TAG, Constant.MSG.MSG_END_FAIL);
//            Result<List<LockLog>> result = new Result<>();
//            result.setCode(Constant.CODE.QUERY_LOGS_FAIL);
//            result.setMsg(Constant.MSG.MSG_END_FAIL);
//            result.setData(null);
//            logsListener.queryLogsCallback(result);
//            return;
//        }
        byte[] btStartSeq = intToBytes2(startSeqNum);
        byte[] btEndSeq = intToBytes2(endSeqNum);
        LogUtil.i(TAG, HexUtil.encodeHexStr(btStartSeq));
        LogUtil.i(TAG, HexUtil.encodeHexStr(btEndSeq));
        data = new byte[2 + btEndSeq.length + btStartSeq.length];
        data[0] = 0x00;
        data[1] = 0x13;
        System.arraycopy(btStartSeq, 0, data, 2, btStartSeq.length);
        System.arraycopy(btEndSeq, 0, data, 2 + btStartSeq.length, btEndSeq.length);
//        LogUtil.i(TAG, "重发次数" + LockApiBleUtil.getInstance().getTryAgainCount());
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
            result[i] = b[result.length - 1 - i];
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
            LogUtil.i(TAG + "onWriteFail", Constant.MSG.MSG_WRITE_FAIL);
            Result<List<LockLog>> result = new Result<>();
            result.setCode(Constant.CODE.QUERY_LOGS_FAIL);
            result.setMsg(Constant.MSG.MSG_WRITE_FAIL);
            result.setData(null);
            queryLogsListener.queryLogsCallback(result);
        }
//
//        @Override
//        public void onWriteTimout() {
//            if (data != null) {
//                //首次写入数据，写入data的第一个，剩下的在监听中完成
//                LogUtil.i(TAG, "重发剩余次数" + LockApiBleUtil.getInstance().getTryAgainCount());
//                WriteAndNoficeUtil.getInstantce().writeFunctionCode(data[1], data, writeDataListener, LockApiBleUtil.getInstance().getTryAgainCount());
//            }
//        }
    };


    /*************数据************/
    // 参数名称	参数名	是否必填	长度	说明
    // 锁具ID	lockId	是	24
    // 起始序号	startSeq	是
    // 结束序号	endSeq	是		endSeq-startSeq<=20;返回[startSeq，endSeq)间的日志


}
