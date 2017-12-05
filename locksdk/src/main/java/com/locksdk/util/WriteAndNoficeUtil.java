package com.locksdk.util;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.locksdk.Constant;
import com.locksdk.DealDataUtil;
import com.locksdk.LockAPI;
import com.locksdk.LockApiBleUtil;
import com.locksdk.baseble.model.resolver.GattAttributeResolver;
import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.ReadListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.baseble.ViseBle;
import com.locksdk.baseble.callback.IBleCallback;
import com.locksdk.baseble.common.PropertyType;
import com.locksdk.baseble.core.BluetoothGattChannel;
import com.locksdk.baseble.core.DeviceMirror;
import com.locksdk.baseble.core.DeviceMirrorPool;
import com.locksdk.baseble.exception.BleException;
import com.locksdk.baseble.model.BluetoothLeDevice;
import com.locksdk.baseble.utils.HexUtil;

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
    private byte resCode = 0;   //应答码
    private static WriteAndNoficeUtil instantce;
    private NoficeCallbackData mNoficeCallbackData;
    private WriteCallbackData mWriteCallbackData;

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
        mWriteCallbackData = new WriteCallbackData();      //写入数据的回调
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.WRITE_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        mWriteDataListener = listener;
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
        mWriteCallbackData.setFunctionCode(functionCode);
        resCode = FunCode2RespCode.funCode2RespCode(functionCode);
        mNoficeCallbackData.setFunctionCode(resCode);
        deviceMirror.bindChannel(bleCallback, bluetoothGattChannel);
        //设置为正在写入
        LockAPI lockAPI = LockAPI.getInstance();
        lockAPI.setWriting(true);
        deviceMirror.writeData(data);
    }

    private WriteDataListener mWriteDataListener;

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
        //写入数据的回调
        mWriteCallbackData = new WriteCallbackData();
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.WRITE_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        mWriteDataListener = listener;
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
        mWriteCallbackData.setFunctionCode(functionCode);
        resCode = FunCode2RespCode.funCode2RespCode(functionCode);
        mNoficeCallbackData.setFunctionCode(resCode);
        deviceMirror.bindChannel(bleCallback, bluetoothGattChannel);
        //设置为正在写入
        LockAPI lockAPI = LockAPI.getInstance();
        lockAPI.setWriting(true);
        write(data);
    }


    public void writeForDeviceSleep(final WriteDataListener listener) {
        if (LockApiBleUtil.getInstance().getGatt() == null) return;
        if (LockApiBleUtil.getInstance().getConnectedBoxDevice() == null) return;
        connectedDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
        mWriteCallbackData = new WriteCallbackData();      //写入数据的回调
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
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                listener.onWirteSuccess(null);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        }, bluetoothGattChannel);
        byte[] data = new byte[2];
        data[0] = 0x00;
        data[1] = 0x14;
        deviceMirror.writeData(data);
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
                }, 500);
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

    private NoficeDataListener noficeDataListener;

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
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.NOTIFY_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mNoficeCallbackData = new NoficeCallbackData();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        mNoficeCallbackData.setFunctionCode(resCode);
        noficeDataListener = listener;
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
        deviceMirror.bindChannel(bleCallback, bluetoothGattChannel);
        deviceMirror.registerNotify(false);
    }


    public void bindChannel(BluetoothLeDevice bluetoothLeDevice, PropertyType propertyType, UUID serviceUUID,
                            UUID characteristicUUID, UUID descriptorUUID) {
        DeviceMirror deviceMirror = LockApiBleUtil.getInstance().getDeviceMirror();
        Log.e("服务：", serviceUUID.toString());
        Log.e("GATT：", characteristicUUID.toString());
        if (deviceMirror != null) {
            BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                    .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                    .setPropertyType(propertyType)
                    .setServiceUUID(serviceUUID)
                    .setCharacteristicUUID(characteristicUUID)
                    .setDescriptorUUID(descriptorUUID)
                    .builder();
            deviceMirror.bindChannel(bleCallback, bluetoothGattChannel);
        }
    }


    /**
     * 操作数据回调
     */
    private IBleCallback bleCallback = new IBleCallback() {
        @Override
        public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
            if (data == null) {
                return;
            }
            if (bluetoothGattInfo.getPropertyType() == PropertyType.PROPERTY_NOTIFY) {
                DeviceMirror deviceMirror = LockApiBleUtil.getInstance().getDeviceMirror();
                deviceMirror.setNotifyListener(bluetoothGattInfo.getGattInfoKey(), receiveCallback);
            } else if (bluetoothGattInfo.getPropertyType() == PropertyType.PROPERTY_WRITE) {
                mWriteCallbackData.setData(data);
                mWriteDataListener.onWirteSuccess(mWriteCallbackData);
            } else if (bluetoothGattInfo.getPropertyType() == PropertyType.PROPERTY_READ) {
                mReadListener.onReadListener(data);
            }
        }

        @Override
        public void onFailure(BleException exception) {
            if (mReadListener != null) {
                mReadListener.onReadListener(null);
            }
            if (mWriteCallbackData != null) {
                mWriteCallbackData.setData(null);
            }
            if (mWriteDataListener != null) {
                mWriteDataListener.onWirteSuccess(mWriteCallbackData);
            }
            if (exception == null) {
                return;
            }
        }
    };

    private IBleCallback receiveCallback = new IBleCallback() {
        @Override
        public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
            Log.e(TAG, "监听通知成功：长度—" + data.length + "；数据16进制—" + HexUtil.encodeHexStr(data));
            byte[] callBlck;
            DealDataUtil.DealtSituation situation = DealDataUtil.dealtDealData(data);
            mNoficeCallbackData.setFinish(situation.isFinish());
            if (situation.isFinish()) {
                callBlck = DealDataUtil.callbackDataMap.get(DealDataUtil.ressonpCode);
                if (!situation.isFail()) {
                    Log.e(TAG, "监听通知处理好的数据：长度—" + callBlck.length + "；数据16进制—" + HexUtil.encodeHexStr(callBlck));
                    mNoficeCallbackData.setData(callBlck);
                } else {
                    mNoficeCallbackData.setData(null);
                    if (noficeDataListener == null) return;
                    noficeDataListener.onNoficeFail(mNoficeCallbackData);
                    Log.e(TAG, "通知Notify返回数据有误或掉包现象，或者数据处理有误");
                }
            }
            if (noficeDataListener == null) return;
            noficeDataListener.onNoficeSuccess(mNoficeCallbackData);
        }

        @Override
        public void onFailure(BleException exception) {
            Log.e(TAG, "监听通知失败");
            mNoficeCallbackData.setData(null);
            if (noficeDataListener == null) return;
            noficeDataListener.onNoficeFail(mNoficeCallbackData);
        }
    };


    private ReadListener mReadListener;

    public void read(ReadListener listener) {
        mReadListener = listener;
        BluetoothLeDevice bluetoothLeDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
        DeviceMirror deviceMirror = LockApiBleUtil.getInstance().getDeviceMirror();
        List<BluetoothGattService> gattServices = deviceMirror.getBluetoothGatt().getServices();
        for (BluetoothGattService bluetoothGattService : gattServices) {
            String uuid = bluetoothGattService.getUuid().toString();
            if (GattAttributeResolver.getAttributeName(uuid, "不清楚").equals("Device Information")) {
                List<BluetoothGattCharacteristic> gattCharacteristics = bluetoothGattService.getCharacteristics();
                for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    if (GattAttributeResolver.getAttributeName(gattCharacteristic.getUuid().toString(), "不知道").contains("System")) {
                        bindChannel(bluetoothLeDevice, PropertyType.PROPERTY_READ, bluetoothGattService.getUuid(), gattCharacteristic.getUuid(), null);
                    }
                }
            }
        }
        deviceMirror.readData();
    }

}
