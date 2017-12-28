package com.locksdk.util;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;

import com.locksdk.lockApi.Constant;
import com.locksdk.lockApi.DealDataUtil;
import com.locksdk.lockApi.LockAPI;
import com.locksdk.lockApi.LockApiBleUtil;
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

    private WriteDataListener mWriteDataListener;
    private byte mFunCode;          //功能码
    //    private int mTryAgainCount;
    //    private boolean isWriterSecond;

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


    public byte[] writeData;

    public byte[] getWriteData() {
        return writeData;
    }

    //填写null,表示清除接口写入的数据，使得其他接口也可写入（当其中一个接口写入，另一个接口会用writeData判断是否在写入）
    public void setWriteData(byte[] writeData) {
        this.writeData = writeData;
    }


    private int mTryAgainCount = 1;             //重发的总次数
    private long mTryAgainTime = 3500;           //写入后的倒计时，默认3500
    private int mTryAgainNum = 0;           //用于计算重发次数
    private Handler mTryAgainHandler = new Handler();
    private Runnable mTryAgainRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.e(TAG, "开始第二次写入");
            writeFunctionCode(mFunCode, writeData, mWriteDataListener);
        }
    };

    private Runnable mSecondWriteRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.e(TAG, "第二次写入后，还没有数据");
            mNoficeCallbackData.setData(null);
            setWriteData(null);
            if (mNoficeDataListener != null) {
                mNoficeDataListener.onNoficeFail(mNoficeCallbackData);
            }
        }
    };


    /**
     * 根据功能码写入数据
     *
     * @param functionCode      功能码：是作为监听回调的一个依据，判断目前是写入的那个功能码（哪一步）
     *                          。例如功能码：0x02——funcationCode为2。
     * @param writerData
     * @param writeDataListener
     */
    public void writeFunctionCode(byte functionCode, byte[] writerData, final WriteDataListener writeDataListener) {
        if (LockApiBleUtil.getInstance().getGatt() == null) return;
        if (LockApiBleUtil.getInstance().getConnectedBoxDevice() == null) return;
        //简单用对象保存写入数据
        this.writeData = writerData;
        mFunCode = functionCode;
        connectedDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
        mWriteCallbackData = new WriteCallbackData();      //写入数据的回调
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.WRITE_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        //对入参的保存，用于第二次发送用；
        mWriteDataListener = writeDataListener;
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
        mNoficeCallbackData = new NoficeCallbackData();
        mWriteCallbackData.setFunctionCode(functionCode);
        resCode = FunCode2RespCode.funCode2RespCode(functionCode);
        mNoficeCallbackData.setRespondCode(resCode);
        deviceMirror.bindChannel(bleCallback, bluetoothGattChannel);
        //设置为正在写入
        LockAPI lockAPI = LockAPI.getInstance();
        lockAPI.setWriting(true);
        deviceMirror.writeData(writerData);
        tryAgainWrite();
    }

    private void tryAgainWrite() {
        if (mTryAgainNum < mTryAgainCount) {
            mTryAgainNum++;
            mTryAgainHandler.removeCallbacksAndMessages(null);
            mTryAgainHandler.postDelayed(mTryAgainRunnable, mTryAgainTime);
        } else {
            mTryAgainHandler.postDelayed(mSecondWriteRunnable, mTryAgainTime);
        }
    }


    /**
     * 根据功能码写入数据
     *
     * @param functionCode 功能码：是作为监听回调的一个依据，判断目前是写入的那个功能码（哪一步）
     *                     。例如功能码：0x02——funcationCode为2。
     * @param writerData
     */
    public void writeFunctionCode2(byte functionCode, byte[] writerData, final WriteDataListener writeDataListener) {
        if (LockApiBleUtil.getInstance().getGatt() == null) return;
        if (LockApiBleUtil.getInstance().getConnectedBoxDevice() == null) return;
        //简单用对象保存写入数据
        this.writeData = writerData;
        mFunCode = functionCode;
        connectedDevice = LockApiBleUtil.getInstance().getConnectedBoxDevice();
        //写入数据的回调
        mWriteCallbackData = new WriteCallbackData();
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.WRITE_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        //对入参的保存，用于第二次发送用；
        mWriteDataListener = writeDataListener;
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
        mNoficeCallbackData = new NoficeCallbackData();
        mWriteCallbackData.setFunctionCode(functionCode);
        resCode = FunCode2RespCode.funCode2RespCode(functionCode);
        mNoficeCallbackData.setRespondCode(resCode);
        deviceMirror.bindChannel(bleCallback, bluetoothGattChannel);
        //设置为正在写入
        LockAPI lockAPI = LockAPI.getInstance();
        lockAPI.setWriting(true);
        write(writerData);
        tryAgainWrite();
//        this.mTryAgainCount = tryAgainCount;
//        if (tryAgainCount != 0) {            //不是第二次写入就，开始倒计时
//            LockApiBleUtil.getInstance().sendWriteSecondHandler(writeDataListener);
//        }else {
//            LockApiBleUtil.getInstance().sendNotifyTimeoutHandler();
//        }
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

    private NoficeDataListener mNoficeDataListener;

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
        mNoficeCallbackData.setRespondCode(resCode);
        mNoficeDataListener = listener;
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
        LogUtil.i("服务：", serviceUUID.toString());
        LogUtil.i("GATT：", characteristicUUID.toString());
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
     * 取消注册的Notify
     */
    public void unregisterNotify() {
        if (LockApiBleUtil.getInstance().getDeviceMirror() != null) {
            LockApiBleUtil.getInstance().getDeviceMirror().unregisterNotify(false);
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
                //这里的isWriteStart是代表写入成功，接着发起3.5秒的计时，如果没有Notify就会执行Handler
//                LogUtil.i(TAG, "剩余重发次数：" + mTryAgainCount);
//                if (mTryAgainCount != 0) {
//                    LogUtil.e(TAG, "的第二次写入计时,此刻为写入成功后的，重新计时（意义为：如果在这段时间没有数据返回，证明蓝牙链路异常）");
//                    LockApiBleUtil.getInstance().sendWriteSecondHandler(mWriteDataListener);
//                }else {
//                    LockApiBleUtil.getInstance().sendNotifyTimeoutHandler();
//                }
                LogUtil.e(TAG, "写入成功");
                mWriteCallbackData.setData(data);
                mWriteDataListener.onWirteSuccess(mWriteCallbackData);
            } else if (bluetoothGattInfo.getPropertyType() == PropertyType.PROPERTY_READ) {
                mReadListener.onReadListener(data);
            }
        }

        @Override
        public void onFailure(BleException exception) {
//            LockApiBleUtil.getInstance().clearIsWriteAndNotifyStart();
            if (mReadListener != null) {
                mReadListener.onReadListener(null);
            }
            if (mWriteCallbackData != null) {
                mWriteCallbackData.setData(null);
                if (mWriteDataListener != null) {
                    mWriteDataListener.onWriteFail(mWriteCallbackData);
                }
            }
            if (exception == null) {
                return;
            }
        }
    };

    private IBleCallback receiveCallback = new IBleCallback() {
        @Override
        public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
            LogUtil.i(TAG, "监听通知成功：长度—" + data.length + "；数据16进制—" + HexUtil.encodeHexStr(data));
            byte[] callBlck;
            DealDataUtil.DealtSituation situation = DealDataUtil.dealtDealData(data);
            mNoficeCallbackData.setFinish(situation.isFinish());
            if (situation.isFinish()) {
                callBlck = DealDataUtil.callbackDataMap.get(DealDataUtil.ressonpCode);
                if (!situation.isFail()) {
                    LogUtil.i(TAG, "监听通知处理好的数据：长度—" + callBlck.length + "；数据16进制—" + HexUtil.encodeHexStr(callBlck));
                    mNoficeCallbackData.setData(callBlck);
                } else {
                    mNoficeCallbackData.setData(null);
                    if (mNoficeDataListener == null) return;
                    mNoficeDataListener.onNoficeFail(mNoficeCallbackData);
                    LogUtil.i(TAG, "通知Notify返回数据有误或掉包现象，或者数据处理有误");
                }
            }
            if (mNoficeDataListener == null) return;
            mNoficeDataListener.onNoficeSuccess(mNoficeCallbackData);
        }

        @Override
        public void onFailure(BleException exception) {
            LogUtil.i(TAG, "监听通知失败");
//            LockApiBleUtil.getInstance().clearIsWriteAndNotifyStart();
            mNoficeCallbackData.setData(null);
            if (mNoficeDataListener == null) return;
            mNoficeDataListener.onNoficeFail(mNoficeCallbackData);
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

    public void removeHandler() {
        if (mTryAgainHandler != null) {
            mTryAgainHandler.removeCallbacksAndMessages(null);
        }
    }

    public int getTryAgainCount() {
        return mTryAgainCount;
    }

    public void setTryAgainCount(int tryAgainCount) {
        mTryAgainCount = tryAgainCount;
    }

    public int getTryAgainNum() {
        return mTryAgainNum;
    }

    public void setTryAgainNum(int tryAgainNum) {
        mTryAgainNum = tryAgainNum;
    }


    public long getTryAgainTime() {
        return mTryAgainTime;
    }

    public void setTryAgainTime(long tryAgainTime) {
        this.mTryAgainTime = tryAgainTime;
    }
}
