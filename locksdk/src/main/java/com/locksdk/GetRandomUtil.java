package com.locksdk;

import android.text.TextUtils;
import android.util.Log;

import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.RandomAttr;
import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.GetRandomListener;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.DealtByteUtil;
import com.locksdk.util.LogUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.utils.HexUtil;

/**
 * Created by Sujiayong on 2017/11/21.
 * 获取随机数
 */

public class GetRandomUtil {

    private static GetRandomListener getRandomListener;
    private static final String TAG = "GetRandomUtil";

    public static void getRandom(String boxName, GetRandomListener listener) {
        getRandomListener = listener;
        if (TextUtils.isEmpty(boxName)) {
            Result<RandomAttr> result = new Result<>();
            result.setCode(Constant.CODE.GET_RANDOM_FAIL);
            result.setMsg(Constant.MSG.MSG_BOX_NAME_NULL);
            result.setData(null);
            listener.getRandomCallback(result);
            return;
        }
        byte[] data = new byte[18];
        data[0] = 0x00;
        data[1] = 0x11;
        byte[] boxNameForByte = boxName.getBytes();
        byte[] boxNameForByte2 = DealtByteUtil.dataAdd0(boxNameForByte, 16);        //补零后的
        System.arraycopy(boxNameForByte2, 0, data, 2, boxNameForByte2.length);
        WriteAndNoficeUtil.getInstantce().writeFunctionCode(data[1], data, writeDataListener);
    }

    private static WriteDataListener writeDataListener = new WriteDataListener() {
        @Override
        public void onWirteSuccess(WriteCallbackData callbackData) {
            if (callbackData != null && callbackData.getData() != null) {
                LogUtil.i(TAG + "_onWirteSuccess", callbackData.getData().length + "---" + HexUtil.encodeHexStr(callbackData.getData()));
            }
        }

        @Override
        public void onWriteFail(WriteCallbackData data) {
            Result<RandomAttr> randomAttrResult = new Result<>();
            randomAttrResult.setCode(Constant.CODE.GET_RANDOM_FAIL);
            randomAttrResult.setMsg(Constant.MSG.MSG_WRITE_FAIL);
            randomAttrResult.setData(null);
            getRandomListener.getRandomCallback(randomAttrResult);
        }
    };
}
