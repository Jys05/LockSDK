package com.locksdk;

import android.text.TextUtils;
import android.util.Log;

import com.locksdk.bean.ActiveLockAttr;
import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.RandomAttr;
import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.AES;
import com.locksdk.util.ActiveErrorUtil;
import com.locksdk.util.Base64Util;
import com.locksdk.util.LockSDKHexUtil;
import com.locksdk.util.RSAUtil;
import com.locksdk.util.RandomUtil;
import com.vise.baseble.ViseBle;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.HexUtil;

import static com.locksdk.util.WriteAndNoficeUtil.noficeFunctionCode;
import static com.locksdk.util.WriteAndNoficeUtil.writeFunctionCode;

/**
 * Created by Sujiayong on 2017/11/15.
 * 对款箱操作：激活（身份验证） ，获取随机数
 * <p>都应在连接款箱成功后操作</>
 */

public class LockActiveUtil {

    private static BluetoothLeDevice connectedDevice;
    private static byte[] dpKey;
    private static final String TAG = "LockActiveUtil";
    private static byte[] R2;
    private static String random;      //随机数
    private static Result<ActiveLockAttr> activeLockAttrResult;


    private static Result<RandomAttr> randomAttrResult;
    private static ActiveLockAttr activeLockAttr;
    private static RandomAttr randomAttr;       //对于“2.7.	获取随机数”方法的
    private static Result<String> openLockResult;



    /**
     * 获取和处理“身份验证秘钥”
     *
     * @param publicKey   登录时，保存的公钥
     * @param validateKey 后台返回回来的加密后的——秘钥Key
     *                    key传空：代表app已经保存了key
     */
    public static void activeLock(String publicKey, String validateKey) {
        activeLockAttrResult = new Result<>();
        randomAttrResult = new Result<>();
        activeLockAttr = new ActiveLockAttr();
        randomAttr = new RandomAttr();
        connectedDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
        writeLoginCode(publicKey, validateKey);
    }

    //返回激活过程的数据
    public static Result<ActiveLockAttr> getActiveLockAttrResult() {
        return activeLockAttrResult;

    }

    //返回获取随机数的数据
    public static Result<RandomAttr> getRandomAttrResult(String boxName) {
        if (boxName.equals(connectedDevice.getName())) {
            return randomAttrResult;
        } else {
            randomAttrResult.setCode(Constant.CODE.GET_RANDOM_FAIL);
            randomAttrResult.setMsg(Constant.MSG.MSG_GET_RANDOM_FAIL);
            randomAttrResult.setData(null);
            return randomAttrResult;
        }

    }

    //返回开锁的数据
    public static Result<String> getOpenLockResult() {
        return openLockResult;
    }

    /**
     * 第一步：写入登录功能码0x01
     *
     * @param publicKey   登录时，后台获取的RSA公钥
     * @param validateKey 从后台获取的秘钥
     */
    private static void writeLoginCode(String publicKey, String validateKey) {
        dpKey = dealValidateKey(publicKey, validateKey);
        if (dpKey != null) {
            activeLockAttr.setDpKey(HexUtil.encodeHexStr(dpKey));
            activeLockAttr.setDpKeyVer(HexUtil.encodeHexStr(dpKey));
            randomAttr.setDpKeyVer(HexUtil.encodeHexStr(dpKey));
            byte[] bytes = new byte[2];
            bytes[0] = 0x00;
            bytes[1] = 0x01;
            writeFunctionCode(1, bytes, mWriteDataListener);
        } else {
            activeLockAttrResult.setCode(Constant.CODE.CODEACTIVE_FAIL);
            activeLockAttrResult.setMsg(Constant.MSG.MSG_DPKEY_FAIL);
            activeLockAttrResult.setData(null);
            randomAttrResult.setCode(Constant.CODE.GET_RANDOM_FAIL);
            randomAttrResult.setMsg(Constant.MSG.MSG_DPKEY_FAIL);
            randomAttrResult.setData(null);
            Log.e(TAG, Constant.MSG.MSG_DPKEY_FAIL);
        }
    }

    /**
     * 第二步:写入发送身份验证的功能码0x03
     * 款箱返回来的身份验证信息：R1，R0
     *
     * @param bytes
     */
    private static void writeValidate(byte[] bytes) {
        byte[] R1 = new byte[8];
        byte[] R0 = new byte[8];
        //款箱随机数
        System.arraycopy(bytes, 2, R1, 0, R1.length);
        System.arraycopy(bytes, 10, R0, 0, R0.length);
        random = HexUtil.encodeHexStr(bytes);
        randomAttr.setRandom(random);
        randomAttrResult.setData(randomAttr);
        //生成一个自身的随机数
        R2 = RandomUtil.getRandom();
        byte[] R2R1 = new byte[16];
        System.arraycopy(R1, 0, R2R1, 0, R1.length);
        System.arraycopy(R2, 0, R2R1, 8, R2.length);
        try {
            byte[] K2K1 = AES.Encrypt(R2R1, dpKey);
            byte[] validateData = new byte[(2 + K2K1.length)];
            validateData[0] = 0x00;
            validateData[1] = 0x03;
            System.arraycopy(K2K1, 0, validateData, 2, K2K1.length);
            writeFunctionCode(3, validateData, mWriteDataListener);
        } catch (Exception e) {
            activeLockAttrResult.setCode(Constant.CODE.CODEACTIVE_FAIL);
            activeLockAttrResult.setMsg(Constant.MSG.MSG_ENCODE_FAIL);
            activeLockAttrResult.setData(null);
            Log.e(TAG, "功能码0x03：数据加密有误");
            e.printStackTrace();
        }
    }

    /**
     * 第五步：  app端的身份验证最后一步（激活的最后一步）
     *
     * @param bytes
     */
    private static void activeLastStep(byte[] bytes) {
        if (bytes.length == 18) {
            byte[] M1M2 = new byte[16];
            System.arraycopy(bytes, 2, M1M2, 0, M1M2.length);
            try {
                byte[] R3R2 = AES.Decrypt(M1M2, dpKey);
                byte[] R2_2 = new byte[8];
                System.arraycopy(R3R2, 8, R2_2, 0, R2_2.length);
                if (HexUtil.encodeHexStr(R2_2).contains(HexUtil.encodeHexStr(R2))) {
                    byte[] dpCommKey = R3R2;
                    activeLockAttr.setDpCommKey(HexUtil.encodeHexStr(dpCommKey));
                    activeLockAttr.setDpCommKeyVer(HexUtil.encodeHexStr(dpCommKey));
                    activeLockAttr.setBoxName(connectedDevice.getName());
                    randomAttr.setBoxName(connectedDevice.getName());
                    randomAttr.setDpCommKeyVer(HexUtil.encodeHexStr(dpCommKey));
                    activeLockAttrResult.setCode(Constant.CODE.CODE_SUCCESS);
                    activeLockAttrResult.setMsg(Constant.MSG.MSG_ACTIVE_SUCCESS);
                    activeLockAttrResult.setData(activeLockAttr);
                    randomAttrResult.setCode(Constant.CODE.CODE_SUCCESS);
                    randomAttrResult.setMsg(Constant.MSG.MSG_GET_RANDOM_SUCCESS);
                    randomAttrResult.setData(randomAttr);
                    Log.i(TAG, Constant.MSG.MSG_ACTIVE_SUCCESS + "接下来可以进行开锁");
//                    sendControlRequest(communicationKey);
                } else {
                    Log.e("=====>", "身份验证失败");
                    ViseBle.getInstance().disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            activeLockAttrResult.setCode(Constant.CODE.CODEACTIVE_FAIL);
            activeLockAttrResult.setMsg(ActiveErrorUtil.getErrorMsg(bytes));
            activeLockAttrResult.setData(null);
            Log.e(TAG, "激活最后一步：款箱返回错误码：" + HexUtil.encodeHexStr(bytes));
        }
    }


    //写入监听：第一步0x01，和，第三步0x03
    private static WriteDataListener mWriteDataListener = new WriteDataListener() {
        @Override
        public void onWirteSuccess(WriteCallbackData data) {
            switch (data.getFunctionCode()) {
                case 1:
                    //监听通知：第二步0x02
                    noficeFunctionCode(2, mNoficeDataListener);
                    break;
                case 3:
                    //第四步（最后一步）0x04
                    noficeFunctionCode(4, mNoficeDataListener);
                    break;
                case 5:
                    //第五步：写入开箱指令(0x05)——监听款箱返回
                    noficeFunctionCode(5, mNoficeDataListener);
                    break;
            }
        }

        @Override
        public void onWriteFail(WriteCallbackData data) {
            activeLockAttrResult.setCode(Constant.CODE.CODEACTIVE_FAIL);
            activeLockAttrResult.setMsg(Constant.MSG.MSG_WRITE_NOFICE_FAIL);
            activeLockAttrResult.setData(null);
            openLockResult.setCode(Constant.CODE.OPEN_LOCK_FAIL);
            openLockResult.setMsg(Constant.MSG.MSG_WRITE_NOFICE_FAIL);
            openLockResult.setData(null);
            Log.e(TAG, "功能码" + data.getFunctionCode() + "：写入数据错误");
        }
    };

    //监听通知：第二步0x02 , 和 第四步（最后一步）0x04
    private static NoficeDataListener mNoficeDataListener = new NoficeDataListener() {
        @Override
        public void onNoficeSuccess(NoficeCallbackData data) {
            switch (data.getFunctionCode()) {
                case 2:
                    //写入监听：第三步0x03
                    Log.e("=====>", "监听通知成功");
                    Log.e("=02====>步", "长度为：：" + data.getData().length);
                    writeValidate(data.getData());
                    break;
                case 4:
                    Log.e("=====>", "监听通知成功");
                    Log.e("=04==最后一步==>步", "长度为：：" + data.getData().length);
                    activeLastStep(data.getData());
                    break;
                case 5:
                    Log.e("=====>", "监听通知成功");
                    Log.e("=5==开锁==>", "长度为：：" + data.getData().length);
                    openLockResult.setCode(HexUtil.encodeHexStr(data.getData()));
                    openLockResult.setMsg(ActiveErrorUtil.getErrorMsg(data.getData()));
                    openLockResult.setData(connectedDevice.getName());
                    break;
            }
        }

        @Override
        public void onNoficeFail(NoficeCallbackData data) {
            activeLockAttrResult.setCode(Constant.CODE.CODEACTIVE_FAIL);
            activeLockAttrResult.setMsg(Constant.MSG.MSG_WRITE_NOFICE_FAIL);
            activeLockAttrResult.setData(null);
            Log.e(TAG, "功能码" + data.getFunctionCode() + "：监听通知数据错误");
        }
    };

    /**
     * 获取和处理“身份验证秘钥”
     *
     * @param publicKey
     * @param ValidateKey 后台返回回来的加密后的——秘钥Key
     *                    key传空：代表app已经保存了key
     * @return
     */
    private static byte[] dealValidateKey(String publicKey, String ValidateKey) {
        if (!TextUtils.isEmpty(ValidateKey)) {
            try {
                byte[] dpKey = RSAUtil.decryptByPublic(publicKey, Base64Util.decode(ValidateKey));
                return dpKey;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /****************************** 开锁 ***********************************************/
    public static void openLock(String accountName, byte[] communicationKey) {
        openLockResult = new Result<>();
        if (TextUtils.isEmpty(accountName)) {
            Log.e(TAG, "用户名保存为空了");
            openLockResult.setData(null);
            openLockResult.setMsg(Constant.MSG.MSG_OPEN_LOCk_FAIL2);
            openLockResult.setCode(Constant.CODE.OPEN_LOCK_FAIL);
            return;
        }
        try {
            byte[] accountNameByte = accountName.getBytes();           //用户名字节
            Log.e(TAG, new String(accountName) + "长度：" + accountNameByte.length);
            byte[] controlRequest = LockSDKHexUtil.hexStringToByte("68E8" , true);              //开箱请求命令
            byte[] requestCode = new byte[16];
            System.arraycopy(controlRequest, 0, requestCode, 0, controlRequest.length);
            System.arraycopy(accountNameByte, 0, requestCode, 2, accountNameByte.length);
            int srcPos = accountNameByte.length + controlRequest.length;
            requestCode = byteAdd0(requestCode, srcPos);
            byte[] requestCode_2 = AES.Encrypt(requestCode, communicationKey);          //加密后
            byte[] requestData = new byte[18];
            requestData[0] = 0x00;
            requestData[1] = 0x05;
            System.arraycopy(requestCode_2, 0, requestData, 2, requestCode_2.length);
            //写入数据给款箱
//                SGSGBlueUtil.getInstance().writeCharacteristicForFca3(5, requestData);
//                SGSGBlueUtil.getInstance().myWrite(5, requestData);
            writeFunctionCode(5, requestData, mWriteDataListener);
        } catch (Exception e) {
            Log.e(TAG, Constant.MSG.MSG_OPEN_LOCk_FAIL);
            openLockResult.setData(null);
            openLockResult.setMsg(Constant.MSG.MSG_OPEN_LOCk_FAIL);
            openLockResult.setCode(Constant.CODE.OPEN_LOCK_FAIL);
            e.printStackTrace();
        }
    }

    /**
     * 字节数组补零
     *
     * @param src
     * @param srcPos 从srcPos位开始补零
     * @return
     */
    private static byte[] byteAdd0(byte[] src, int srcPos) {
        for (int i = srcPos; i < src.length; i++) {
            src[i] = 0x00;
        }
        return src;
    }

}
