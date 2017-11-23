package com.locksdk;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.ActiveLockListener;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.ASCIICodeUtil;
import com.locksdk.util.BCDCodeUtil;
import com.locksdk.util.DateUtil;
import com.locksdk.util.DealtByteUtil;
import com.locksdk.util.LockSDKHexUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.vise.baseble.utils.HexUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sujiayong on 2017/11/20.
 * 激活
 */

public class ActiveLockUtil {


    private static List<byte[]> writeData;      //准备写入的数据
    private static int finalI;
    private static int position;
    private static ActiveLockListener activeLockListener;
    private static final String TAG = "ActiveLockUtil";

    public static void activeLock(Map<String, String> param, ActiveLockListener lockListener) {
        activeLockListener = lockListener;
        String trTime = param.get("trTime");        //时间戳
        String lockId = param.get("lockId");        //锁具ID
        if (TextUtils.isEmpty(lockId)) return;
        String dpKey = param.get("dpKey");      //动态密码密钥
        String dpCommKey = param.get("dpCommKey");      //动态密码传输密钥
        String dpCommKeyVer = param.get("dpCommKeyVer");        //动态密码传输密钥版本
        String dpKeyVer = param.get("dpKeyVer");        //动态密码密钥版本
        String dpKeyChkCode = param.get("dpKeyChkCode");        //动态密码密钥校验值
        String dpCommChkCode = param.get("dpCommChkCode");      //动态密码传输密钥校验值
        String boxName = param.get("boxName");      //款箱名称
        String time = DateUtil.format(DateUtil.yyyyMMddHHmmss_not, System.currentTimeMillis());
        byte[] btTime = BCDCodeUtil.str2Bcd(trTime);
        //TODO : 2017/11/23 数据固定
//        byte[] btLockId = LockApiBleUtil.getInstance().getLockID();
        byte[] btLockId = HexUtil.decodeHex(lockId.toCharArray());
        byte[] btDpKey = DealtByteUtil.dataAdd0(HexUtil.decodeHex(dpKey.toCharArray()), 16);
        byte[] btDpCommKey = DealtByteUtil.dataAdd0(HexUtil.decodeHex(dpCommKey.toCharArray()), 16);
        byte[] btDpCommKeyVer = DealtByteUtil.dataAdd0(dpCommKeyVer.getBytes(), 36);
        byte[] btDpKeyVer = DealtByteUtil.dataAdd0(dpKeyVer.getBytes(), 36);
        byte[] btDpKeyChkCode = DealtByteUtil.dataAdd0(HexUtil.decodeHex(dpKeyChkCode.toCharArray()), 16);
        byte[] btDpCommChkCode = DealtByteUtil.dataAdd0(HexUtil.decodeHex(dpCommChkCode.toCharArray()), 16);
        byte[] btBoxName = DealtByteUtil.dataAdd0(boxName.getBytes(), 16);
        /*********************** 测试版 ****************************************/
//        String time = DateUtil.format(DateUtil.yyyyMMddHHmmss_not, System.currentTimeMillis());
//        byte[] btTime = BCDCodeUtil.str2Bcd(time);
//        byte[] btLockId = DealtByteUtil.dataAdd0(LockSDKHexUtil.hexStringToByte("546C0E1B8068", true), 12);
//        byte[] btDpKey = DealtByteUtil.dataAdd0(LockSDKHexUtil.hexStringToByte("A1B2C3D4E5F6", true), 16);
//        byte[] btDpCommKey = DealtByteUtil.dataAdd0(LockSDKHexUtil.hexStringToByte("A1B2C3D4E5F6", true), 16);
//        byte[] btDpCommKeyVer = DealtByteUtil.dataAdd0("KX001".getBytes(), 36);
//        byte[] btDpKeyVer = DealtByteUtil.dataAdd0("KX001".getBytes(), 36);
//        byte[] btDpKeyChkCode = DealtByteUtil.dataAdd0(LockSDKHexUtil.hexStringToByte("A1B2C3D4E5F6", true), 16);
//        byte[] btDpCommChkCode = DealtByteUtil.dataAdd0(LockSDKHexUtil.hexStringToByte("A1B2C3D4E5F6", true), 16);
//        byte[] btBoxName = DealtByteUtil.dataAdd0("KX001".getBytes(), 16);
//        Log.e(TAG, new String(btBoxName));
//        Log.e(TAG, " btTime          ：" + btTime.length + "=====>" + HexUtil.encodeHexStr(btTime));
//        Log.e(TAG, " btLockId        ：" + btLockId.length + "=====>" + HexUtil.encodeHexStr(btLockId));
//        Log.e(TAG, " btDpKey         ：" + btDpKey.length + "=====>" + HexUtil.encodeHexStr(btDpKey));
//        Log.e(TAG, " btDpCommKey     ：" + btDpCommKey.length + "=====>" + HexUtil.encodeHexStr(btDpCommKey));
//        Log.e(TAG, " btDpCommKeyVer  ：" + btDpCommKeyVer.length + "=====>" + HexUtil.encodeHexStr(btDpCommKeyVer));
//        Log.e(TAG, " btDpKeyVer      ：" + btDpKeyVer.length + "=====>" + HexUtil.encodeHexStr(btDpKeyVer));
//        Log.e(TAG, " btDpKeyChkCode  ：" + btDpKeyChkCode.length + "=====>" + HexUtil.encodeHexStr(btDpKeyChkCode));
//        Log.e(TAG, " btDpCommChkCode ：" + btDpCommChkCode.length + "=====>" + HexUtil.encodeHexStr(btDpCommChkCode));
//        Log.e(TAG, " btBoxName       ：" + btBoxName.length + "=====>" + HexUtil.encodeHexStr(btBoxName));
        /*********************** 测试版 ****************************************/
        byte[] data = new byte[1 + 7 + 12 + 16 + 16 + 36 + 36 + 16 + 16 + 16];
        data[0] = 0x10; //功能码
        System.arraycopy(btTime, 0, data, 1, btTime.length);
        System.arraycopy(btLockId, 0, data, 8, btLockId.length);
        System.arraycopy(btDpKey, 0, data, 20, btDpKey.length);
        System.arraycopy(btDpCommKey, 0, data, 36, btDpCommKey.length);
        System.arraycopy(btDpCommKeyVer, 0, data, 52, btDpCommKeyVer.length);
        System.arraycopy(btDpKeyVer, 0, data, 88, btDpKeyVer.length);
        System.arraycopy(btDpKeyChkCode, 0, data, 124, btDpKeyChkCode.length);
        System.arraycopy(btDpCommChkCode, 0, data, 140, btDpCommChkCode.length);
        System.arraycopy(btBoxName, 0, data, 156, btBoxName.length);
        //首次写入数据，写入data的第一个，剩下的在监听中完成
        WriteAndNoficeUtil.getInstantce().writeFunctionCode2(data[0], data, writeDataListener);
    }


    private static WriteDataListener writeDataListener = new WriteDataListener() {
        @Override
        public void onWirteSuccess(WriteCallbackData callbackData) {
            Log.i(TAG + "======>", callbackData.getData().length + "---" + HexUtil.encodeHexStr(callbackData.getData()));
        }

        @Override
        public void onWriteFail(WriteCallbackData data) {

        }
    };

    private static Handler handler = new Handler();

    /****数据：***/
    //参数名称	  参数名	是否必填	长度
    //时间戳        	trTime	是	14
    //锁具ID	        lockId	是	24
    //动态密码密钥	dpKey	是	16
    //动态密码传输密钥	dpCommKey	是	16
    //动态密码传输密钥版本	dpCommKeyVer	是	36
    //动态密码密钥版本	dpKeyVer	是	36
    //动态密码密钥校验值	dpKeyChkCode	是	32
    //动态密码传输密钥校验值	dpCommChkCode	是	32
    //款箱名称	boxName	是	16字节

//

}
