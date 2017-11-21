package com.locksdk;

import android.util.Log;

import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.ActiveLockListener;
import com.locksdk.listener.WriteDataListener;
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


    private static List<byte[]> data;
    private static int finalI;
    private static int position;
    private static ActiveLockListener activeLockListener;

    public static void activeLock(Map<String, String> param, ActiveLockListener lockListener) {
        activeLockListener = lockListener;
        String trTime = param.get("trTime");
        String lockId = param.get("lockId");
        String dpKey = param.get("dpKey");
        String dpCommKey = param.get("dpCommKey");
        String dpCommKeyVer = param.get("dpCommKeyVer");
        String dpKeyVer = param.get("dpKeyVer");
        String dpKeyChkCode = param.get("dpKeyChkCode");
        String dpCommChkCode = param.get("dpCommChkCode");
        String boxName = param.get("boxName");
        String paramData = trTime + lockId + dpKey + dpCommKey + dpCommKeyVer + dpKeyVer + dpKeyChkCode + dpCommChkCode + boxName;
//        List<byte[]> dealtData = DealDataUtil.dealData(paramData.getBytes());       //处理后的数据，但是每个包好没有根据文档中添加，80,00，总字节长度
        List<byte[]> dealtData = DealDataUtil.dealData(LockSDKHexUtil.hexStringToByte(paramData, true));       //处理后的数据，但是每个包好没有根据文档中添加，80,00，总字节长度
        Log.i("======", LockSDKHexUtil.hexStringToByte(paramData, true).length + "");
        data = new ArrayList<>();
        if (dealtData.size() > 1) {
            for (int i = 0; i < dealtData.size(); i++) {
                byte[] dataByte = new byte[20];
                dataByte[0] = (byte) (0x80 + i);
                if (i == 0) {
                    dataByte[1] = 0x00;
//                    dataByte[2] = (byte) paramData.getBytes().length;
                    dataByte[2] = (byte) LockSDKHexUtil.hexStringToByte(paramData, true).length;
                    System.arraycopy(dealtData.get(i), 0, dataByte, 3, dealtData.get(i).length);
                } else if (i == dealtData.size() - 1) {
                    dataByte[0] = (byte) (0x00 + i);
                    System.arraycopy(dealtData.get(i), 0, dataByte, 1, dealtData.get(i).length);
                } else {
                    System.arraycopy(dealtData.get(i), 0, dataByte, 1, dealtData.get(i).length);
                }
                data.add(dataByte);
            }
        }
        for (int i = 0; i < data.size(); i++) {
            Log.i(i + "======>", data.get(i).length
                    + "：" + HexUtil.encodeHexStr(data.get(i), true));
        }
        position = dealtData.size();
        Log.i("一共", position + "");
        //首次写入数据，写入data的第一个，剩下的在监听中完成
        WriteAndNoficeUtil.writeFunctionCode(0, dealtData.get(0), writeDataListener);
    }


    private static WriteDataListener writeDataListener = new WriteDataListener() {
        @Override
        public void onWirteSuccess(WriteCallbackData callbackData) {
            position--;
            if (position > 0) {
                WriteAndNoficeUtil.writeFunctionCode(0, data.get(data.size() - position), writeDataListener);
                Log.i(position + "第写", "成功");

            }
        }

        @Override
        public void onWriteFail(WriteCallbackData data) {

        }
    };

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
