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
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.utils.HexUtil;

/**
 * Created by Sujiayong on 2017/11/21.
 * 获取随机数
 */

public class GetRandomUtil {

    private static GetRandomListener getRandomListener;

    public static void getRandom(String boxName, GetRandomListener listener) {
        getRandomListener = listener;
        if (TextUtils.isEmpty(boxName)) return;
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
        public void onWirteSuccess(WriteCallbackData data) {
            Log.i("=====>", "写入成功" + data.getFunctionCode() + "====" + data.getData().length + "====" + HexUtil.encodeHexStr(data.getData()));
            Result<RandomAttr> randomAttrResult = new Result<>();
            randomAttrResult.setMsg("=====");
            randomAttrResult.setCode("=====");
            getRandomListener.getRandomCallback(randomAttrResult);
        }

        @Override
        public void onWriteFail(WriteCallbackData data) {

        }
    };
}
