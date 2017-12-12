package com.locksdk.lockApi;

import android.text.TextUtils;

import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.OpenLockListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.BCDCodeUtil;
import com.locksdk.util.DealtByteUtil;
import com.locksdk.util.LogUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.utils.HexUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by Sujiayong on 2017/11/20.
 * 开锁
 */

public class OpenLockUtil {
    private static final String TAG = "OpenLockUtil";
    private static OpenLockListener openLockListener;
    private static List<byte[]> data;
    private static int position;

    public static void opnenLock(Map<String, String> param, OpenLockListener lockListener) {
        openLockListener = lockListener;
        String trTime = param.get("trTime");
        String boxName = param.get("boxName");
        if (TextUtils.isEmpty(boxName)) {
            Result<String> result = new Result<>();
            result.setCode(Constant.CODE.OPEN_LOCK_FAIL);
            result.setMsg(Constant.MSG.MSG_BOX_NAME_NULL);
            result.setData(null);
            lockListener.openLockCallback(result);
            return;
        }
        String userId = param.get("userId");
        String dynamicPwd = param.get("dynamicPwd");
        byte[] btTime = BCDCodeUtil.str2Bcd(trTime);
        byte[] btBoxName = DealtByteUtil.dataAdd0(boxName.getBytes(), 16);
        byte[] btDynamicPwd = DealtByteUtil.dataAdd0(dynamicPwd.getBytes(), 6);
        byte[] btUserId = DealtByteUtil.dataAdd0(userId.getBytes(), 16);
        byte[] writeData = new byte[1 + 7 + 16 + 6 + 16];        //写入的数据
        writeData[0] = 0x12;
        System.arraycopy(btTime, 0, writeData, 1, 7);
        System.arraycopy(btBoxName, 0, writeData, 8, 16);
        System.arraycopy(btDynamicPwd, 0, writeData, 24, 6);
        System.arraycopy(btUserId, 0, writeData, 30, 16);
        //首次写入数据，写入data的第一个，剩下的在监听中完成
        WriteAndNoficeUtil.getInstantce().writeFunctionCode2(writeData[0], writeData, writeDataListener);
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
            LogUtil.i(TAG + "onWriteFail", Constant.MSG.MSG_WRITE_FAIL);
            Result<String> result = new Result<>();
            result.setCode(Constant.CODE.OPEN_LOCK_FAIL);
            result.setMsg(Constant.MSG.MSG_WRITE_FAIL);
            result.setData(null);
            openLockListener.openLockCallback(result);
        }
    };


    /***************数据*****************/
    //参数名称	参数名	是否必填	长度	说明
    //时间戳	trTime	是	14
    //款箱名称	boxName	是	16字节OpenLockListener
    //用户id	userId	是	16	用户唯一识别标识
    //动态密码	dynamicPwd	是	值：6位数字


}
