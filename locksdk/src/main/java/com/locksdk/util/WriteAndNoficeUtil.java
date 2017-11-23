package com.locksdk.util;

import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.locksdk.Constant;
import com.locksdk.DealDataUtil;
import com.locksdk.LockApiBleUtil;
import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.WriteDataListener;
import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IBleCallback;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.core.DeviceMirrorPool;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.HexUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by Sujiayong on 2017/11/20.
 * 蓝牙设备的写入，和Nofice
 */

public class WriteAndNoficeUtil {

    private final String TAG = "WriteAndNoficeUtil";
    private DeviceMirrorPool mDeviceMirrorPool;
    private BluetoothLeDevice connectedDevice;
    public boolean isWriting = false;
    private byte resCode = 0;   //应答码
    private static WriteAndNoficeUtil instantce;
    private NoficeCallbackData mNoficeCallbackData;

    private WriteAndNoficeUtil() {

    }

    public static WriteAndNoficeUtil getInstantce() {
        if (instantce == null) {
            synchronized (WriteAndNoficeUtil.class) {
                if (instantce == null) {
                    instantce = new WriteAndNoficeUtil();
                }
            }
        }
        return instantce;
    }

    /**
     * 根据功能码写入数据
     *
     * @param functionCode 功能码：是作为监听回调的一个依据，判断目前是写入的那个功能码（哪一步）
     *                     。例如功能码：0x02——funcationCode为2。
     * @param data
     */
    public void writeFunctionCode(byte functionCode, byte[] data, final WriteDataListener listener) {
        if (LockApiBleUtil.getInstance().getGatt() == null) return;
        if (LockApiBleUtil.getInstance().getConnectedBoxDevice() == null) return;
        connectedDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
        final WriteCallbackData writeCallbackData = new WriteCallbackData();      //写入数据的回调
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.WRITE_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        //        DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(connectedDevice);
        final DeviceMirror deviceMirror = LockApiBleUtil.getInstance().getDeviceMirror();
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setServiceUUID(mServiceUUID)
                .setCharacteristicUUID(characteristicUUID)
                .setDescriptorUUID(null)
                .builder();
        //将功能码设进回调数据中。
        writeCallbackData.setFunctionCode(functionCode);
        resCode = FunCode2RespCode.funCode2RespCode(functionCode);
        mNoficeCallbackData.setFunctionCode(resCode);
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
                Log.e(TAG, "写入成功");
                writeCallbackData.setData(data);
                listener.onWirteSuccess(writeCallbackData);
            }

            @Override
            public void onFailure(BleException exception) {
                Log.e(TAG, "写入失败");
                writeCallbackData.setData(null);
                listener.onWriteFail(writeCallbackData);
            }
        }, bluetoothGattChannel);
        isWriting = true;
        deviceMirror.writeData(data);
    }

    /**
     * 根据功能码写入数据
     *
     * @param functionCode 功能码：是作为监听回调的一个依据，判断目前是写入的那个功能码（哪一步）
     *                     。例如功能码：0x02——funcationCode为2。
     * @param data
     */
    public void writeFunctionCode2(byte functionCode, byte[] data, final WriteDataListener listener) {
        if (LockApiBleUtil.getInstance().getGatt() == null) return;
        if (LockApiBleUtil.getInstance().getConnectedBoxDevice() == null) return;
        connectedDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
        final WriteCallbackData writeCallbackData = new WriteCallbackData();      //写入数据的回调
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.WRITE_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
//        DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(connectedDevice);
        final DeviceMirror deviceMirror = LockApiBleUtil.getInstance().getDeviceMirror();
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setServiceUUID(mServiceUUID)
                .setCharacteristicUUID(characteristicUUID)
                .setDescriptorUUID(null)
                .builder();
        //将功能码设进回调数据中。
        writeCallbackData.setFunctionCode(functionCode);
        resCode = FunCode2RespCode.funCode2RespCode(functionCode);
        mNoficeCallbackData.setFunctionCode(resCode);
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
                Log.e(TAG, "写入成功");
                writeCallbackData.setData(data);
                listener.onWirteSuccess(writeCallbackData);
            }

            @Override
            public void onFailure(BleException exception) {
                Log.e(TAG, "写入失败");
                writeCallbackData.setData(null);
                listener.onWriteFail(writeCallbackData);
            }
        }, bluetoothGattChannel);
        isWriting = true;
        write(data);
    }

    //发送队列，提供一种简单的处理方式，实际项目场景需要根据需求优化
    private Queue<byte[]> dataInfoQueue = new LinkedList<>();

    private void send(final BluetoothLeDevice bluetoothLeDevice) {
        if (dataInfoQueue != null && !dataInfoQueue.isEmpty()) {
            DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(bluetoothLeDevice);
            if (dataInfoQueue.peek() != null && deviceMirror != null) {
                deviceMirror.writeData(dataInfoQueue.poll());
            }
            if (dataInfoQueue.peek() != null) {
                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        send(bluetoothLeDevice);
                    }
                }, 1000);
            }
        }
    }

    public void write(byte[] data) {
        if (dataInfoQueue != null) {
            dataInfoQueue.clear();
            dataInfoQueue = splitPacketFor20Byte(data);
            connectedDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
            new Handler(Looper.myLooper()).post(new Runnable() {
                @Override
                public void run() {
                    send(connectedDevice);
                }
            });
        }
    }

    /**
     * 数据分包
     *
     * @param data
     * @return
     */
    private Queue<byte[]> splitPacketFor20Byte(byte[] data) {
        Queue<byte[]> dataInfoQueue = new LinkedList<>();
        List<byte[]> dealtData = DealDataUtil.dealData(data);
        for (int i = 0; i < dealtData.size(); i++) {
            byte[] dataByte = new byte[20];
            dataByte[0] = (byte) (0x80 + i);
            if (i == 0) {
                dataByte[1] = 0x00;
                dataByte[2] = (byte) data.length;
                System.arraycopy(dealtData.get(i), 0, dataByte, 3, dealtData.get(i).length);
                dataInfoQueue.offer(dataByte);
            } else if (i == dealtData.size() - 1) {
                byte[] dataLast = new byte[dealtData.get(i).length + 1];
                dataLast[0] = (byte) (0x00 + i);
                System.arraycopy(dealtData.get(i), 0, dataLast, 1, dealtData.get(i).length);
                dataInfoQueue.offer(dataLast);
            } else {
                System.arraycopy(dealtData.get(i), 0, dataByte, 1, dealtData.get(i).length);
                dataInfoQueue.offer(dataByte);
            }

        }
        return dataInfoQueue;
    }


    /**
     * 根据功能码通知数据
     * <p>
     * 功能码：是作为监听回调的一个依据，判断目前是写入的那个功能码（哪一步）
     * 。例如功能码：0x02——funcationCode为2。
     */
    public void noficeFunctionCode(final NoficeDataListener listener) {
        if (LockApiBleUtil.getInstance().getGatt() == null) return;
        if (LockApiBleUtil.getInstance().getConnectedBoxDevice() == null) return;
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.READ_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mNoficeCallbackData = new NoficeCallbackData();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        mNoficeCallbackData.setFunctionCode(resCode);
//        final DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(connectedDevice);
        final DeviceMirror deviceMirror = LockApiBleUtil.getInstance().getDeviceMirror();
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setBluetoothGatt(LockApiBleUtil.getInstance().getGatt())
                .setPropertyType(PropertyType.PROPERTY_NOTIFY)
                .setServiceUUID(mServiceUUID)
                .setCharacteristicUUID(characteristicUUID)
                .setDescriptorUUID(null)
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
                Log.e(TAG, "" + data.length + "====" + HexUtil.encodeHexStr(data));
                deviceMirror.setNotifyListener(bluetoothGattInfo.getGattInfoKey(), new IBleCallback() {
                    @Override
                    public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                        Log.e(TAG, "监听通知成功" + data.length + "====" + HexUtil.encodeHexStr(data));
                        byte[] callBlck;
                        boolean isFinish = DealDataUtil.dealtDealData(data);
                        mNoficeCallbackData.setFinish(isFinish);
                        if (isFinish) {
                            callBlck = DealDataUtil.callbackDataMap.get(DealDataUtil.ressonpCode);
                            mNoficeCallbackData.setData(callBlck);
                        }
                        listener.onNoficeSuccess(mNoficeCallbackData);
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        Log.e(TAG, "监听通知失败");
                        mNoficeCallbackData.setData(null);
                        listener.onNoficeFail(mNoficeCallbackData);
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                mNoficeCallbackData.setData(null);
                listener.onNoficeFail(mNoficeCallbackData);
            }
        }, bluetoothGattChannel);
        deviceMirror.registerNotify(false);
    }

}
