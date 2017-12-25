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
    private static byte[] data;

    public static void queryLockStatus(String lockId, LockStatusListener listener) {
        if (listener == null) return;
        lockStatusListener = listener;
        locid = lockId;
        data = new byte[2];
        data[0] = 0x00;
        data[1] = 0x14;
        WriteAndNoficeUtil.getInstantce().writeFunctionCode(data[1], data, writeDataListener, false);
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

        @Override
        public void onWriteTimout() {
            if(data != null){
                //首次写入数据，写入data的第一个，剩下的在监听中完成
                WriteAndNoficeUtil.getInstantce().writeFunctionCode(data[1], data, writeDataListener, true);
            }
        }
    };

}
