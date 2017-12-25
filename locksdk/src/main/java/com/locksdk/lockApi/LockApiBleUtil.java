package com.locksdk.lockApi;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.locksdk.baseble.common.BleConfig;
import com.locksdk.baseble.exception.ConnectException;
import com.locksdk.bean.WriteCallbackData;
import com.locksdk.listener.BleStateListener;
import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.GetLockIdListener;
import com.locksdk.listener.ReadListener;
import com.locksdk.listener.ScannerListener;
import com.locksdk.listener.WriteDataListener;
import com.locksdk.util.LogUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.ViseBle;
import com.locksdk.baseble.callback.IConnectCallback;
import com.locksdk.baseble.core.DeviceMirror;
import com.locksdk.baseble.exception.BleException;
import com.locksdk.baseble.exception.TimeoutException;
import com.locksdk.baseble.model.BluetoothLeDevice;
import com.locksdk.baseble.model.BluetoothLeDeviceStore;
import com.locksdk.baseble.utils.HexUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.locksdk.util.BleStateListenerUtil.setOnBleReceiver;

/**
 * Created by Sujiayong on 2017/11/14.
 * 蓝牙工具：扫描，连接，断开连接，获取锁具ID
 */

public class LockApiBleUtil {

    private static final String TAG = "LockApiBleUtil";

    //用于请求打开蓝牙的CODE，用于onActivityResult判断
    public static final int BLE_REQUEST_CODE = 0x12;
    private static LockApiBleUtil instance;
    private long connectTimeout = 10000;
    private Map<String, String> mLockIdMap;     //对应关系是：key为boxName（款箱名字） ，value为LockId(锁具Id)
    private ConnectListener mConnectListener;
    private String mBoxName = null;                 //准备连接或已经连接成功的box名字
    private String mBoxMac = null;           //连接后的box的Mac地址——即是box的Mac地址，也是锁具ID
    private List<String> mScanenrBoxNames;
    private Result<List<BluetoothLeDevice>> mScanneredResult;               //扫描返回结果
    private BluetoothLeDeviceStore mBluetoothLeDeviceStore;         //搜索的结果
    private ScannerListener mScannerListener;           //扫描监听
    private BluetoothGatt mGatt;
    private BluetoothGattService mBluetoothGattService;
    private BluetoothLeDevice mConnectBoxDevice;        //准备连接的box设备
    private BluetoothLeDevice mConnectedBoxDevice;            //成功连接后的box设备

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback mScanCallback;     //安卓版本在5.0以上的扫描回调
    private BluetoothAdapter.LeScanCallback mLeScanCallback;//安卓版本在4.4以上的扫描回调
    private boolean IsScannering = false;
    private DeviceMirror mDeviceMirror;

    //TOdo 之后需要整理
    public void clearLockId() {
        mLockID = null;
        mLockIDStr = null;
    }

    public void setLockID(byte[] lockID) {
        mLockID = lockID;
    }

    public String getLockIDStr() {
        return mLockIDStr;
    }

    public void setLockIDStr(String lockIDStr) {
        mLockIDStr = lockIDStr;
    }

    private byte[] mLockID;


    private String mLockIDStr = "";

    public DeviceMirror getDeviceMirror() {
        return mDeviceMirror;
    }


    public byte[] getLockID() {
        return mLockID;
    }

    private LockApiBleUtil() {
        mLockIdMap = new HashMap<>();
        mScanenrBoxNames = new ArrayList<>();
        mLockIdMap = new HashMap<>();
        mBluetoothLeDeviceStore = new BluetoothLeDeviceStore();
    }

    public static LockApiBleUtil getInstance() {
        if (instance == null) {
            synchronized (LockApiBleUtil.class) {
                if (instance == null) {
                    instance = new LockApiBleUtil();
                }
            }
        }

        return instance;
    }

    public void init(Context context) {
        if (context == null) {
            return;
        }
        //蓝牙相关配置修改
        ViseBle.config()
                .setScanTimeout(-1)//扫描超时时间，这里设置为永久扫描
                .setConnectTimeout((int) connectTimeout)//连接超时时间
                .setOperateTimeout(5 * 1000)//设置数据操作超时时间
                .setConnectRetryCount(3)//设置连接失败重试次数
                .setConnectRetryInterval(1000)//设置连接失败重试间隔时间
                .setOperateRetryCount(3)//设置数据操作失败重试次数
                .setOperateRetryInterval(1000)//设置数据操作失败重试间隔时间
                .setMaxConnectCount(1);//设置最大连接设备数量
        //蓝牙信息初始化，全局唯一，必须在应用初始化时调用
        ViseBle.getInstance().init(context);
    }


    /************************* 蓝牙扫描（	获取款箱列表）、停止扫描 **************************/

    /**
     * 获取款箱列表（开始扫描）
     *
     * @return 款箱名称列表
     */
    public void startScanner(final ScannerListener listener) {
        if (listener == null) {
            LogUtil.i(TAG, Constant.MSG.MSG_SCANNER_FAIL_NULL);
            listener.onScannerFail(Constant.CODE.CODE_SCANNER_FAIL, Constant.MSG.MSG_SCANNER_FAIL_NULL);
            return;
        }
        mScannerListener = listener;
        if (ViseBle.getInstance().getContext() == null) {
            LogUtil.i(TAG, Constant.MSG.MSG_LOCK_API_INIT_FAIL);
            listener.onScannerFail(Constant.CODE.CODE_SCANNER_FAIL, Constant.MSG.MSG_LOCK_API_INIT_FAIL);
            return;
        }
        if (!isBleEnable(ViseBle.getInstance().getContext())) {
            setOnBleReceiver(mBleStateListenerFoeScanner);
            //开启蓝牙
            ViseBle.getInstance().getBluetoothAdapter().enable();
            return;
        }
        clearScannerResult();
        stopScanner();
        //开始扫描
        mScannerListener.onStartScanner(Constant.CODE.CODE_SUCCESS, Constant.MSG.MSG_SCANNERING);
        startScannerSetting();
    }


    //清理扫描到的结果（防止下次扫描重复）
    private void clearScannerResult() {
        mScanneredResult = null;
        mScanneredResult = new Result<>();
        mLockIdMap.clear();
        mLockIdMap = new HashMap<>();
        mScanenrBoxNames.clear();
        mBluetoothLeDeviceStore.clear();
    }

    //按照不同的安卓版本:进行扫描设置，以及开始扫描
    private void startScannerSetting() {
        IsScannering = true;
        isConnecting = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sannerSettingForLollipop();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sannerSettingForJELLY_BEAN();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sannerSettingForLollipop() {
        if (mScanCallback == null) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice bluetoothDevice = result.getDevice();
                    if (bluetoothDevice != null) {
                        mBluetoothLeDeviceStore.addDevice(new BluetoothLeDevice(bluetoothDevice, result.getRssi()
                                , result.getScanRecord().getBytes(), result.getTimestampNanos()));
                        if (!mLockIdMap.containsValue(bluetoothDevice.getAddress())) {
                            LogUtil.i(TAG, bluetoothDevice.getName() + "——" + bluetoothDevice.getAddress());
                            mLockIdMap.put(bluetoothDevice.getName(), bluetoothDevice.getAddress());
                        }
                        if (!mScanenrBoxNames.contains(bluetoothDevice.getName())) {
                            mScanenrBoxNames.add(bluetoothDevice.getName());
                        }
                        mScanneredResult.setMsg(Constant.MSG.MSG_SCANNERED);
                        mScanneredResult.setCode(Constant.CODE.CODE_SUCCESS);
                        mScanneredResult.setData(mBluetoothLeDeviceStore.getDeviceList());
                        scannerBoxNamesResult.setCode(Constant.CODE.CODE_SUCCESS);
                        scannerBoxNamesResult.setMsg(Constant.MSG.MSG_SCANNERED);
                        scannerBoxNamesResult.setData(mScanenrBoxNames);
                        mScannerListener.onBoxFoundScanning(mScanneredResult, scannerBoxNamesResult);
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    mScannerListener.onScannerFail(Constant.CODE.CODE_SCANNER_FAIL
                            , Constant.MSG.MSG_SCANNER_FAIL);
                }
            };
        }           //由于查看源码之后，得知有Builder，所以猜想不是用new ScanFilter，而是用Builder
        ScanFilter.Builder builder = new ScanFilter.Builder();
        ScanFilter scanFilter = builder.setServiceUuid(new ParcelUuid(UUID.fromString(Constant.SERVICE_UUID))).build();
        List<ScanFilter> filterList = new ArrayList<>();            //添加过滤条件
        filterList.add(scanFilter);
        ScanSettings.Builder scanSettingsBuild = new ScanSettings.Builder();
        ScanSettings scanSettings = scanSettingsBuild.setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        ViseBle.getInstance().getBluetoothAdapter().getBluetoothLeScanner().startScan(filterList, scanSettings, mScanCallback);
    }

    private void sannerSettingForJELLY_BEAN() {
        final String uuidFilterStr = getUuidFilter();
        if (mLeScanCallback == null) {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device != null) {
                        String scanRecordStr = HexUtil.encodeHexStr(scanRecord);
                        if (scanRecordStr.contains(uuidFilterStr)) {
                            mBluetoothLeDeviceStore.addDevice(new BluetoothLeDevice(device, rssi
                                    , scanRecord, System.currentTimeMillis()));
                            if (!mLockIdMap.containsValue(device.getAddress())) {
                                mLockIdMap.put(device.getName(), device.getAddress());
                            }
                            if (!mScanenrBoxNames.contains(device.getName())) {
                                mScanenrBoxNames.add(device.getName());
                            }
                            mScanneredResult.setMsg(Constant.MSG.MSG_SCANNERED);
                            mScanneredResult.setCode(Constant.CODE.CODE_SUCCESS);
                            mScanneredResult.setData(mBluetoothLeDeviceStore.getDeviceList());
                            scannerBoxNamesResult.setCode(Constant.CODE.CODE_SUCCESS);
                            scannerBoxNamesResult.setMsg(Constant.MSG.MSG_SCANNERED);
                            scannerBoxNamesResult.setData(mScanenrBoxNames);
                            mScannerListener.onBoxFoundScanning(mScanneredResult, scannerBoxNamesResult);
                        }
                    }
                }
            };
        }
        ViseBle.getInstance().getBluetoothAdapter().startLeScan(mLeScanCallback);
    }

    /**
     * 4.4版本中的处理过滤UUID
     *
     * @return
     */
    private String getUuidFilter() {
        String uuidStr = Constant.SERVICE_UUID.replace("-", "");
        LogUtil.e(TAG + "0", uuidStr);
        byte[] uuidByt = HexUtil.decodeHex(uuidStr.toCharArray());
        byte[] uuidByt2 = new byte[uuidByt.length];
        LogUtil.e(TAG + "1", HexUtil.encodeHexStr(uuidByt, true));
        for (int i = 0; i < uuidByt2.length; i++) {
            uuidByt2[i] = uuidByt[(uuidByt2.length - 1) - i];
        }
        String uuidFilterStr = HexUtil.encodeHexStr(uuidByt2, true);
        return uuidFilterStr;
    }


    /**
     * 停止扫描
     */
    public void stopScanner() {
        if (IsScannering) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mScanCallback != null) {
                    ViseBle.getInstance().getBluetoothAdapter().getBluetoothLeScanner().stopScan(mScanCallback);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (mLeScanCallback != null) {
                    ViseBle.getInstance().stopLeScan(mLeScanCallback);
                }
            }
            IsScannering = false;
        }
    }

    private BleStateListener mBleStateListenerFoeScanner = new BleStateListener() {
        @Override
        public void onState_ON() {
            clearScannerResult();
            //开始扫描
            mScannerListener.onStartScanner(Constant.CODE.CODE_SUCCESS, Constant.MSG.MSG_SCANNERING);
            startScannerSetting();

        }

        @Override
        public void onState_OFF() {

        }
    };

    private Result<List<String>> scannerBoxNamesResult = new Result<>();

    //获取扫描到的款箱名字，（P:在getBoxList的扫描成功的接口调用，否则可以为null，或size为0）
    public Result<List<String>> getScannerBoxNames() {
        scannerBoxNamesResult.setCode(Constant.CODE.CODE_SUCCESS);
        scannerBoxNamesResult.setMsg(Constant.MSG.MSG_SCANNERED);
        scannerBoxNamesResult.setData(mScanenrBoxNames);
        return scannerBoxNamesResult;
    }

    /**************************** 蓝牙连接、关闭连接 *************************************/

    private static boolean isConnecting = false;
    private static boolean isConnectSuccess = false;
    public int mDeviceSleepTime = LockAPI.getInstance().getDeviceSleepTime();          //在连接成功之后也赋值了
    private boolean isAllowSleep = false;           //false为允许休眠，true为不允许休眠
    public Handler mAllowSleepHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (isAllowSleep) {
                clearHandler();
            } else {
                LockAPI lockAPI = LockAPI.getInstance();
                if (lockAPI.isWriting()) {      //正在写入
                    mDeviceSleepTime = lockAPI.getDeviceSleepTime();
                } else {
                    mDeviceSleepTime = mDeviceSleepTime - 100;
                    LogUtil.i(TAG, "休眠倒计时：" + mDeviceSleepTime);
                    if (mDeviceSleepTime == 0) {
                        if (isConnectSuccess) {
                            LogUtil.i(TAG, mDeviceSleepTime + "防止休眠给板子发指令");
                            mDeviceSleepTime = lockAPI.getDeviceSleepTime();
//                        lockAPI.queryLockStatus(mLockIDStr);
                            WriteAndNoficeUtil.getInstantce().writeForDeviceSleep(new WriteDataListener() {
                                @Override
                                public void onWirteSuccess(WriteCallbackData data) {
                                    mAllowSleepHandler.removeCallbacksAndMessages(null);
                                    mDeviceSleepTime = LockAPI.getInstance().getDeviceSleepTime();
                                }

                                @Override
                                public void onWriteFail(WriteCallbackData data) {
                                    mAllowSleepHandler.removeCallbacksAndMessages(null);
                                }

                                @Override
                                public void onWriteTimout() {

                                }
                            });
                        }
                    }
                    mAllowSleepHandler.sendEmptyMessageDelayed(0x00, 100);
                }
            }
            return false;
        }
    });

    public void setSleepModel(boolean isAllowSleep) {
        this.isAllowSleep = isAllowSleep;
    }


    public void setDeviceSleepTime(int deviceSleepTime) {
        mDeviceSleepTime = deviceSleepTime;
    }

    //清理Handler
    public void clearHandler() {
        if (mAllowSleepHandler != null) {
            mDeviceSleepTime = LockAPI.getInstance().getDeviceSleepTime();
            mAllowSleepHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * 开始连接
     *
     * @param timeOut  单位毫秒
     * @param listener
     */
    public void openConnection(final BluetoothLeDevice boxDevice, long timeOut, final ConnectListener listener) {
        if (listener != null) {
            //蓝牙相关配置修改
            if (timeOut > 0) {
                ViseBle.config().setConnectTimeout((int) timeOut);//连接超时时间
            }
            if (isConnecting) {
                LogUtil.i(TAG, "正在连接");
                listener.onWaiting(Constant.SERVICE_UUID);
                return;
            }
            mConnectListener = listener;
            mBoxName = boxDevice.getName();
            mConnectBoxDevice = boxDevice;
            //停止扫描
            stopScanner();
            //注册蓝牙广播监听
//            setOnBleReceiver(mBleStateListenerForConnect);
            //关闭蓝牙
            isConnecting = true;
            setConnect();
        } else {
            LogUtil.i(TAG, Constant.MSG.MSG_CONNECT_FAIL_NULL);
            listener.onFail(Constant.CODE.CODE_CONNECT_FAIL, Constant.MSG.MSG_CONNECT_FAIL_NULL);
        }
    }

//    private BleStateListener mBleStateListenerForConnect = new BleStateListener() {
//        @Override
//        public void onState_ON() {
//            isConnecting = true;
//            setConnect();
//            //关闭蓝牙广播监听
//            closeBleReceiver();
//        }
//
//        @Override
//        public void onState_OFF() {
//            //蓝牙状态为关闭时，启动蓝牙
//            ViseBle.getInstance().getBluetoothAdapter().enable();
//        }
//    };


    /**
     * 关闭蓝牙款箱连接
     */
    public boolean closeConnection() {
        //断开连接，将连接成功的box的Mac地址，赋值为空
        WriteAndNoficeUtil.getInstantce().unregisterNotify();
        mBoxMac = null;
        mBoxName = null;
        isConnecting = false;
        isConnectSuccess = false;
        WriteAndNoficeUtil.getInstantce().clearIsWriteAndNotifyStart();
        //清理获取的锁具ID
        LockApiBleUtil.getInstance().clearLockId();
        //清理Handler
        clearHandler();
        if (mConnectedBoxDevice != null) {
            ViseBle.getInstance().disconnect(mConnectedBoxDevice);
            mConnectedBoxDevice = null;
        } else {
            ViseBle.getInstance().disconnect();
        }
        return true;
    }

    private void setConnect() {
        isConnectSuccess = false;
        if (mConnectBoxDevice != null) {
            //调用“连接中”接口
            mConnectRetryCount = BleConfig.getInstance().getConnectRetryCount();
            Log.i(TAG, "失败从重连次数：" + mConnectRetryCount);
            mConnectListener.onWaiting(Constant.SERVICE_UUID);
            ViseBle.getInstance().connect(mConnectBoxDevice, mIConnectCallback);
        } else {
            mConnectListener.onFail(Constant.SERVICE_UUID
                    , Constant.MSG.MSG_CONNECT_DEVICE_NULL);
        }
    }

    private int mConnectRetryCount = BleConfig.getInstance().getConnectRetryCount();

    private IConnectCallback mIConnectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            if (mConnectListener != null) {
                if (isConnecting) {
                    isConnecting = false;
                    isConnectSuccess = true;
                    //再次获取连接的款箱名、锁具ID(款箱Mac)
                    mDeviceMirror = deviceMirror;
                    mConnectRetryCount = BleConfig.getInstance().getConnectRetryCount();
                    mBoxName = deviceMirror.getBluetoothLeDevice().getDevice().getName();
                    mBoxMac = deviceMirror.getBluetoothLeDevice().getDevice().getAddress();
                    mConnectedBoxDevice = deviceMirror.getBluetoothLeDevice();
                    mGatt = deviceMirror.getBluetoothGatt();
                    mBluetoothGattService = mGatt.getService(UUID.fromString(Constant.SERVICE_UUID));
                    LockAPI lockAPI = LockAPI.getInstance();
                    lockAPI.resigeterNotify();      //注册通知监听
                    //获取锁具ID
                    getLockIdByBoxName(null);
                    //调用连接成功接口
                    mConnectListener.onSuccess(mConnectedBoxDevice, Constant.SERVICE_UUID, mBoxName);
                    //防止设备休眠，时间计算12秒后发数据给板子
                    mDeviceSleepTime = lockAPI.getDeviceSleepTime();
                    mAllowSleepHandler.sendEmptyMessageDelayed(0x00, 100);
                } else {
                    LogUtil.e(TAG, "假连接");
                    closeConnection();
                }
            }
        }

        @Override
        public void onConnectFailure(BleException exception) {
            mConnectRetryCount--;
            if (mConnectRetryCount == 0) {
                mConnectRetryCount = BleConfig.getInstance().getConnectRetryCount();
                mConnectBoxDevice = null;
                isConnecting = false;
                //清理获取的锁具ID
                LockApiBleUtil.getInstance().clearLockId();
                if (exception instanceof TimeoutException) {
                    mConnectListener.onTimeout(Constant.SERVICE_UUID, BleConfig.getInstance().getConnectTimeout() * 3);
                    //清理Handler
                    clearHandler();
                } else {
                    if (mConnectListener != null) {
                        mConnectListener.onFail(Constant.SERVICE_UUID, Constant.MSG.MSG_CONNECT_FAIL);
                    }
                    //清理Handler
                    clearHandler();
                }

            }
            if (isConnectSuccess) {
                if (exception instanceof ConnectException) {
                    if (mConnectListener != null) {
                        //清理获取的锁具ID
                        LockApiBleUtil.getInstance().clearLockId();
                        mConnectListener.onFail(Constant.SERVICE_UUID, Constant.MSG.MSG_CONNECT_FAIL2);
                    }
                    //款箱自动断开，SDK内部调用断开连接
                    LockAPI.getInstance().closeConnection();
                }
                isConnectSuccess = false;

            }
            LogUtil.i(TAG, exception.getDescription() + "失败从重连次数：" + mConnectRetryCount);
        }

        @Override
        public void onDisconnect(boolean isActive) {
            mConnectBoxDevice = null;
            isConnecting = false;
            isConnectSuccess = false;
            //清理获取的锁具ID
            LockApiBleUtil.getInstance().clearLockId();
            //清理Handler
            clearHandler();
            mConnectListener.onClose(Constant.SERVICE_UUID, mBoxName);
        }
    };


    /********************************** 获取款箱锁具ID ********************************/
    public void getLockIdByBoxName(final GetLockIdListener lockIdListener) {
        final Result<String> resultLockId = new Result<>();
        WriteAndNoficeUtil.getInstantce().read(new ReadListener() {
            @Override
            public void onReadListener(byte[] data) {
                if (data != null) {
                    LogUtil.i(TAG + "-getLockIdByBoxName", data.length + "---" + HexUtil.encodeHexStr(data));
                    LockApiBleUtil.getInstance().setLockID(data);
                    LockApiBleUtil.getInstance().setLockIDStr(HexUtil.encodeHexStr(data));
                    if (lockIdListener != null) {
                        resultLockId.setCode(Constant.CODE.CODE_SUCCESS);
                        resultLockId.setMsg(Constant.MSG.MSG_GET_LOCK_ID_SUCCESS);
                        resultLockId.setData(HexUtil.encodeHexStr(data));
                        lockIdListener.onGetLockIDListener(resultLockId);
                    }
                } else {
                    if (lockIdListener != null) {
                        resultLockId.setCode(Constant.CODE.CODE_GET_LOCK_ID_FAIL);
                        resultLockId.setMsg(Constant.MSG.MSG_GET_LOCK_ID_FAIL);
                        resultLockId.setData(null);
                        lockIdListener.onGetLockIDListener(resultLockId);
                    }
                }
            }
        });
    }


    public static void enableBluetooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    public static boolean isSupportBle(Context context) {
        if (context == null || !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter() != null;
    }

    public static boolean isBleEnable(Context context) {
        if (!isSupportBle(context)) {
            return false;
        }
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter().isEnabled();
    }

    public BluetoothGatt getGatt() {
        return mGatt;
    }

    public BluetoothGattService getBluetoothGattService() {
        return mBluetoothGattService;
    }

    public BluetoothLeDevice getConnectedBoxDevice() {
        return mConnectedBoxDevice;
    }

}
