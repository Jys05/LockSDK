package com.locksdk;

import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.AES;
import com.locksdk.util.Base64Util;
import com.locksdk.util.RSAUtil;
import com.locksdk.util.RandomUtil;
import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IBleCallback;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.core.DeviceMirrorPool;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.HexUtil;

import java.util.UUID;

/**
 * Created by Sujiayong on 2017/11/15.
 * 对款箱操作：激活（身份验证） ，获取随机数 ，开锁，查询锁具状态 ， 查询日志
 * <p>都应在连接款箱成功后操作</>
 */

public class LockAuthValidateUtil {

    private static DeviceMirrorPool mDeviceMirrorPool;
    private static BluetoothLeDevice mBluetoothLeDevice;
    private static byte[] mAuthValidateKey;
    private static final String TAG = "LockAuthValidateUtil";
    private static byte[] mR2;

    public static Result<String> activeLock() {

    }


    /**
     * 第一步：写入登录功能码
     *
     * @param publicKey   登录时，后台获取的RSA公钥
     * @param validateKey 从后台获取的秘钥
     */
    public static void sendLoginCode(String publicKey, String validateKey) {
        mAuthValidateKey = dealValidateKey(publicKey, validateKey);
        byte[] bytes = new byte[2];
        bytes[0] = 0x00;
        bytes[1] = 0x01;
        writeFunctionCode(1, bytes, mWriteDataListener);
    }

    /**
     * 第二步:写入发送身份验证的功能码
     * 款箱返回来的身份验证信息：R1，R0
     *
     * @param bytes
     */
    public static void sendValidate(byte[] bytes) {
        byte[] mR1 = new byte[8];
        byte[] mR0 = new byte[8];
        System.arraycopy(bytes, 2, mR1, 0, mR1.length);
        System.arraycopy(bytes, 10, mR0, 0, mR0.length);
        //生成一个自身的随机数
        mR2 = RandomUtil.getRandom();
        byte[] R2R1 = new byte[16];
        System.arraycopy(mR1, 0, R2R1, 0, mR1.length);
        System.arraycopy(mR2, 0, R2R1, 8, mR2.length);
        try {
            byte[] K2K1 = AES.Encrypt(R2R1, mAuthValidateKey);
            byte[] validateData = new byte[(2 + K2K1.length)];
            validateData[0] = 0x00;
            validateData[1] = 0x03;
            System.arraycopy(K2K1, 0, validateData, 2, K2K1.length);
            writeFunctionCode(3, validateData, mWriteDataListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第五步：  app端的身份验证最后一步（激活的最后一步）
     *
     * @param bytes
     */
    public void step4(byte[] bytes) {
        if (bytes.length == 18) {
            Result<String>
            byte[] M1M2 = new byte[16];
            System.arraycopy(bytes, 2, M1M2, 0, M1M2.length);
            try {
                byte[] R3R2 = AES.Decrypt(M1M2, mAuthValidateKey);
                byte[] R2_2 = new byte[8];
                System.arraycopy(R3R2, 8, R2_2, 0, R2_2.length);
                if (HexUtil.encodeHexStr(R2_2).contains(HexUtil.encodeHexStr(mR2))) {
//                    byte[] communicationKey = R3R2;
//                    sendControlRequest(communicationKey);
                } else {
                    Log.e("=====>", "身份验证失败");
                    ViseBle.getInstance().disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            SGSGValidateErrorUtil.getErrorMsg(bytes);
        }
    }


    private static WriteDataListener mWriteDataListener = new WriteDataListener() {
        @Override
        public void onWirteSuccess(WriteCallbackData data) {
            switch (data.getFunctionCode()) {
                case 1:
                    noficeFunctionCode(2, mNoficeDataListener);
                    break;
                case 3:
                    noficeFunctionCode(4, mNoficeDataListener);
                    break;
            }
        }

        @Override
        public void onWriteFail() {

        }
    };

    private static NoficeDataListener mNoficeDataListener = new NoficeDataListener() {
        @Override
        public void onNoficeSuccess(NoficeCallbackData data) {
            switch (data.getFunctionCode()) {
                case 2:
                    Log.e("=====>", "监听通知成功");
                    Log.e("=02====>步", "长度为：：" + data.getData().length);
                    sendValidate(data.getData());
                    break;
                case 4:
                    Log.e("=====>", "监听通知成功");
                    Log.e("=02====>步", "长度为：：" + data.getData().length);

                    break;
            }
        }

        @Override
        public void onNoficeFail() {

        }
    };

    /**
     * 获取和处理“身份验证秘钥”
     *
     * @param validateKey 后台返回回来的加密后的——秘钥Key
     *                    key传空：代表app已经保存了key
     */
    private static byte[] dealValidateKey(String publicKey, String validateKey) {
        if (!TextUtils.isEmpty(validateKey)) {
            try {
                byte[] authValidateKey = RSAUtil.decryptByPublic(publicKey, Base64Util.decode(validateKey));
                return authValidateKey;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 根据功能码写入数据
     *
     * @param functionCode 功能码：是作为监听回调的一个依据，判断目前是写入的那个功能码（哪一步）
     *                     。例如功能码：0x02——funcationCode为2。
     * @param data
     */
    public static void writeFunctionCode(int functionCode, byte[] data, final WriteDataListener listener) {
        if (LockApiBleUtil.getInstance().getGatt() == null) return;
        if (LockApiBleUtil.getInstance().getConnectedBoxDevice() == null) return;
        mBluetoothLeDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
        final WriteCallbackData writeCallbackData = new WriteCallbackData();      //写入数据的回调
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.WRITE_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(mBluetoothLeDevice);
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setServiceUUID(mServiceUUID)
                .setCharacteristicUUID(characteristicUUID)
                .setDescriptorUUID(null)
                .builder();
        //将功能码设进回调数据中。
        writeCallbackData.setFunctionCode(functionCode);
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
                Log.e("=====>", "写入成功");
                writeCallbackData.setData(data);
                listener.onWirteSuccess(writeCallbackData);
            }

            @Override
            public void onFailure(BleException exception) {
                Log.e("=====>", "写入失败");
                listener.onWriteFail();
            }
        }, bluetoothGattChannel);
        deviceMirror.writeData(data);
    }

    /**
     * 根据功能码通知数据
     *
     * @param functionCode 功能码：是作为监听回调的一个依据，判断目前是写入的那个功能码（哪一步）
     *                     。例如功能码：0x02——funcationCode为2。
     */
    public static void noficeFunctionCode(int functionCode, final NoficeDataListener listener) {
        if (LockApiBleUtil.getInstance().getGatt() == null) return;
        if (LockApiBleUtil.getInstance().getConnectedBoxDevice() == null) return;
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.READ_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        final NoficeCallbackData noficeCallbackData = new NoficeCallbackData();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        noficeCallbackData.setFunctionCode(functionCode);
        final DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(mBluetoothLeDevice);
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setServiceUUID(mServiceUUID)
                .setCharacteristicUUID(characteristicUUID)
                .setDescriptorUUID(null)
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
                deviceMirror.setNotifyListener(bluetoothGattInfo.getGattInfoKey(), new IBleCallback() {
                    @Override
                    public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                        Log.e("=====>", "监听通知成功");
                        noficeCallbackData.setData(data);
                        listener.onNoficeSuccess(noficeCallbackData);
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        Log.e("=====>", "监听通知失败");
                        listener.onNoficeFail();
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                listener.onNoficeFail();
            }
        }, bluetoothGattChannel);
        deviceMirror.registerNotify(false);
    }

}
