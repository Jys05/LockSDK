package com.locksdk.util;

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

    public static DeatedLogsData dealLogsData(byte[] data) {
        DeatedLogsData deatedLogsData = new DeatedLogsData();
        byte[] btBoxName1 = new byte[16];
        System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
        byte[] btBoxName = DealtByteUtil.dataClear0(btBoxName1);
        String boxName = new String(btBoxName);

        byte[] callBackResult = new byte[2];        //返回的操作是否正确
        System.arraycopy(data, 16, callBackResult, 0, callBackResult.length);
        int callBack = Integer.valueOf(HexUtil.encodeHexStr(callBackResult));
        LogUtil.i(TAG, "返回的操作是否正确：" + callBackResult.length + "====>" + HexUtil.encodeHexStr(callBackResult) + "转成int：" + callBack);

        List<LockLog> logsDataLists = new ArrayList<>();

        if (callBack == 0) {        //板子返回0000
            byte btLogNum = data[18];     //日志数量
            int intLogNum = btLogNum & 0xFF;
            byte[] logDatas = new byte[data.length - 19];
            System.arraycopy(data, 19, logDatas, 0, logDatas.length);
            LogUtil.i(TAG, "日志个数：" + (logDatas.length / 28) + "==日志数据：==>" + HexUtil.encodeHexStr(logDatas));
            //TODO : 2017/12/1 此判断不知道需不需要
            for (int i = 0; i < logDatas.length / 28; i++) {
                byte[] logData = new byte[28];
                System.arraycopy(logDatas, i * 28, logData, 0, logData.length);
                LogUtil.i(TAG, HexUtil.encodeHexStr(logData));
                //日志序号
                byte[] logNo = new byte[4];
                System.arraycopy(logData, 0, logNo, 0, logNo.length);
                LogUtil.i(TAG, "日志序号：" + ToDecimalUtil.toD(HexUtil.encodeHexStr(DealtByteUtil.dataClear0(logNo)), 16));
                String logNoStr = ToDecimalUtil.toD(HexUtil.encodeHexStr(DealtByteUtil.dataClear0(logNo)), 16);

                //操作类型
                byte btOptType = logData[4];
                String strOptType = getOptTyp(btOptType);
                LogUtil.i(TAG, "操作类型：" + strOptType + "===" + (btOptType & 0xFF));
                //操作时间
                byte[] operationTime = new byte[7];
                System.arraycopy(logData, 5, operationTime, 0, operationTime.length);
                String strOptTime = BCDCodeUtil.bcd2Str(operationTime);
                LogUtil.i(TAG, "操作时间：" + strOptTime);
                //用户ID
                byte[] btUserId = new byte[16];
                System.arraycopy(logData, 12, btUserId, 0, btUserId.length);
                String strUserId = new String(DealtByteUtil.dataClear0(btUserId));
                LogUtil.i(TAG, "操作UserID：" + strUserId + "====" + HexUtil.encodeHexStr(btUserId));
                LockLog lockLog = new LockLog();
                lockLog.setOptType(strOptType);
                lockLog.setOptTime(strOptTime);
                lockLog.setUserId(strUserId);
                lockLog.setSeq(logNoStr);
                logsDataLists.add(lockLog);
            }
            deatedLogsData.setLockLogList(logsDataLists);
            deatedLogsData.setSuccess(true);
        } else {         //板子返回错误（获取日志失败）
            deatedLogsData.setLockLogList(logsDataLists);
            deatedLogsData.setSuccess(false);
        }
        deatedLogsData.setCallBackResult(HexUtil.encodeHexStr(callBackResult));         //将板子返回数据设置进“返回数据”中
        return deatedLogsData;
    }

    //获取操作类型
    private static String getOptTyp(byte btOptType) {
        String strOptType = "";
        switch (btOptType) {
            case (byte) 0x01:
                strOptType = "开锁";
                break;
            case (byte) 0x02:
                strOptType = "关锁";
                break;
            case (byte) 0x03:
                strOptType = "激活";
                break;
            case (byte) 0x07:
                strOptType = "开锁授权失败";
                break;
            case (byte) 0x25:
                strOptType = "移动报警";
                break;
            case (byte) 0x26:
                strOptType = "非法开箱";
                break;
            case (byte) 0x27:
                strOptType = "开箱超时";
                break;
        }
        return strOptType;
    }

    //处理后的日志数据
    public static class DeatedLogsData {
        private String callBackResult;          //如果成功板子返回0000
        private boolean isSuccess;                //是否成功获取日志数据
        private List<LockLog> mLockLogList;

        public DeatedLogsData() {
        }

        public String getCallBackResult() {
            return callBackResult;
        }

        public void setCallBackResult(String callBackResult) {
            this.callBackResult = callBackResult;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }

        public List<LockLog> getLockLogList() {
            return mLockLogList;
        }

        public void setLockLogList(List<LockLog> lockLogList) {
            mLockLogList = lockLogList;
        }
    }
}
