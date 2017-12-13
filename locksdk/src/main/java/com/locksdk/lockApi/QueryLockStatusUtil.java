package com.locksdk.lockApi;

import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.LockStatusListener;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.DealtByteUtil;
import com.locksdk.util.LogUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.utils.HexUtil;

/**
 * Created by Sujiayong on 2017/11/22.
 * 查看锁具状态
 */

public class QueryLockStatusUtil {

    private static final String TAG = "QueryLockStatusUtil";
    private static LockStatusListener lockStatusListener;
    private static String locid;

    public static void queryLockStatus(String lockId, LockStatusListener listener) {
        if (listener == null) return;
        lockStatusListener = listener;
        locid = lockId;
        byte[] data = new byte[2];
        data[0] = 0x00;
        data[1] = 0x14;
        WriteAndNoficeUtil.getInstantce().writeFunctionCode(data[1], data, writeDataListener);
    }

    private static WriteDataListener writeDataListener = new WriteDataListener() {
        @Override
        public void onWirteSuccess(WriteCallbackData callbackData) {
            if (callbackData != null && callbackData.getData() != null) {
                LogUtil.i(TAG , callbackData.getData().length + "---" + HexUtil.encodeHexStr(callbackData.getData()));
            }
        }

        @Override
        public void onWriteFail(WriteCallbackData data) {
            LogUtil.i(TAG + "onWriteFail", Constant.MSG.MSG_WRITE_FAIL);
        }
    };

    private static NoficeDataListener noficeDataListener = new NoficeDataListener() {
        @Override
        public void onNoficeSuccess(NoficeCallbackData callbackData) {
            LogUtil.i(TAG, callbackData.getData().length + "---" + HexUtil.encodeHexStr(callbackData.getData()));
            byte[] data = callbackData.getData();
            if (data.length == 20) {
                byte[] resonpenCode = new byte[1];      //应答码：
                resonpenCode[0] = data[1];
                if (HexUtil.encodeHexStr(resonpenCode).equals("94")) {
                    byte[] boxNamePaint = new byte[16];
                    System.arraycopy(data, 2, boxNamePaint, 0, boxNamePaint.length);
                    byte[] btBoxName = DealtByteUtil.dataClear0(boxNamePaint);
                    LogUtil.i(TAG, new String(btBoxName));
                    String boxName = new String(btBoxName);
                    lockStatusListener.onChange(boxName, locid, null);
                }
            }
        }

        @Override
        public void onNoficeFail(NoficeCallbackData data) {

        }
    };
}