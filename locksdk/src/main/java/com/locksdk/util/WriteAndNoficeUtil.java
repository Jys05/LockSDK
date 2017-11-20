package com.locksdk.util;

import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.locksdk.Constant;
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

import java.util.UUID;

/**
 * Created by Sujiayong on 2017/11/20.
 * 蓝牙设备的写入，和Nofice
 */

public class WriteAndNoficeUtil {

    private static final String TAG = "WriteAndNoficeUtil";
    private static DeviceMirrorPool mDeviceMirrorPool;
    private static BluetoothLeDevice connectedDevice;

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
        connectedDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
        final WriteCallbackData writeCallbackData = new WriteCallbackData();      //写入数据的回调
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.WRITE_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(connectedDevice);
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
        final DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(connectedDevice);
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
                        Log.e(TAG, "监听通知成功");
                        noficeCallbackData.setData(data);
                        listener.onNoficeSuccess(noficeCallbackData);
                    }

                    @Override
                    public void onFailure(BleException exception) {
                        Log.e(TAG, "监听通知失败");
                        noficeCallbackData.setData(null);
                        listener.onNoficeFail(noficeCallbackData);
                    }
                });
            }

            @Override
            public void onFailure(BleException exception) {
                noficeCallbackData.setData(null);
                listener.onNoficeFail(noficeCallbackData);
            }
        }, bluetoothGattChannel);
        deviceMirror.registerNotify(false);
    }
}
