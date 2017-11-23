package com.locksdk;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.listener.BleStateListener;
import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.ScannerListener;
import com.locksdk.util.WriteAndNoficeUtil;
import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IConnectCallback;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.exception.TimeoutException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;
import com.vise.baseble.utils.BleUtil;
import com.vise.baseble.utils.HexUtil;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.locksdk.util.BleStateListenerUtil.closeBleReceiver;
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

    public DeviceMirror getDeviceMirror() {
        return mDeviceMirror;
    }

    private DeviceMirror mDeviceMirror;

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
                .setMaxConnectCount(3);//设置最大连接设备数量
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
            return;
        }
        mScannerListener = listener;
        if (ViseBle.getInstance().getContext() == null) {
            return;
        }
        if (!isBleEnable(ViseBle.getInstance().getContext())) {
            setOnBleReceiver(mBleStateListenerFoeScanner);
            //开启蓝牙
            ViseBle.getInstance().getBluetoothAdapter().enable();
            return;
        }
        clearScannerResult();
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
                            Log.e(TAG, bluetoothDevice.getName() + "——" + bluetoothDevice.getAddress());
                            mLockIdMap.put(bluetoothDevice.getName(), bluetoothDevice.getAddress());
                        }
                        if (!mScanenrBoxNames.contains(bluetoothDevice.getName())) {
                            mScanenrBoxNames.add(bluetoothDevice.getName());
                        }
                        mScanneredResult.setMsg(Constant.MSG.MSG_SCANNERED);
                        mScanneredResult.setCode(Constant.CODE.CODE_SUCCESS);
                        mScanneredResult.setData(mBluetoothLeDeviceStore.getDeviceList());
                        mScannerListener.onBoxFoundScanning(mScanneredResult);
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
        UUID[] uuids = new UUID[]{UUID.fromString(Constant.SERVICE_UUID)};
        if (mLeScanCallback == null) {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device != null) {
                        mBluetoothLeDeviceStore.addDevice(new BluetoothLeDevice(device, rssi
                                , scanRecord, 0000));
                        if (!mLockIdMap.containsValue(device.getAddress())) {
                            mLockIdMap.put(device.getName(), device.getAddress());
                        }
                        if (!mScanenrBoxNames.contains(device.getName())) {
                            mScanenrBoxNames.add(device.getName());
                        }
                        mScanneredResult.setMsg(Constant.MSG.MSG_SCANNERED);
                        mScanneredResult.setCode(Constant.CODE.CODE_SUCCESS);
                        mScanneredResult.setData(mBluetoothLeDeviceStore.getDeviceList());
                        mScannerListener.onBoxFoundScanning(mScanneredResult);
                    }
                }
            };
        }
        ViseBle.getInstance().getBluetoothAdapter().startLeScan(uuids, mLeScanCallback);
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

    //获取扫描到的款箱名字，（P:在getBoxList的扫描成功的接口调用，否则可以为null，或size为0）
    public List<String> getScannerBoxNames() {
        return mScanenrBoxNames;
    }

    /**************************** 蓝牙连接、关闭连接 *************************************/

    private static boolean isConnecting = false;

    /**
     * 开始连接
     * 先关闭蓝牙，后再连接（通过广播监听蓝牙状态的变化）
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
            if (isConnecting) return;
            mConnectListener = listener;
            mBoxName = boxDevice.getName();
            mConnectBoxDevice = boxDevice;
            //停止扫描
            stopScanner();
            //注册蓝牙广播监听
            setOnBleReceiver(mBleStateListenerForConnect);
            //关闭蓝牙
            if (ViseBle.getInstance().getBluetoothAdapter().isEnabled()) {
                ViseBle.getInstance().getBluetoothAdapter().disable();
            } else {
                ViseBle.getInstance().getBluetoothAdapter().enable();
            }

        }
    }

    private BleStateListener mBleStateListenerForConnect = new BleStateListener() {
        @Override
        public void onState_ON() {
            isConnecting = true;
            setConnect();
            //关闭蓝牙广播监听
            closeBleReceiver();
        }

        @Override
        public void onState_OFF() {
            //蓝牙状态为关闭时，启动蓝牙
            ViseBle.getInstance().getBluetoothAdapter().enable();
        }
    };


    /**
     * 关闭蓝牙款箱连接
     */
    public void closeConnection() {
        //断开连接，将连接成功的box的Mac地址，赋值为空
        mBoxMac = null;
        mBoxName = null;
        isConnecting = false;
        if (mConnectedBoxDevice != null) {
            ViseBle.getInstance().disconnect(mConnectedBoxDevice);
            mConnectedBoxDevice = null;
        } else {
            ViseBle.getInstance().disconnect();
        }

    }

    private void setConnect() {
        if (mConnectBoxDevice != null) {
            //调用“连接中”接口
            mConnectListener.onWaiting(Constant.SERVICE_UUID);
            ViseBle.getInstance().connect(mConnectBoxDevice, mIConnectCallback);
        } else {
            mConnectListener.onFail(Constant.SERVICE_UUID
                    , Constant.MSG.MSG_CONNECT_DEVICE_NULL);
        }
    }

    private IConnectCallback mIConnectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            if (mConnectListener != null) {
                isConnecting = false;
                //再次获取连接的款箱名、锁具ID(款箱Mac)
                mDeviceMirror = deviceMirror;
                mBoxName = deviceMirror.getBluetoothLeDevice().getDevice().getName();
                mBoxMac = deviceMirror.getBluetoothLeDevice().getDevice().getAddress();
                mConnectedBoxDevice = deviceMirror.getBluetoothLeDevice();
                mGatt = deviceMirror.getBluetoothGatt();
                mBluetoothGattService = mGatt.getService(UUID.fromString(Constant.SERVICE_UUID));
                LockAPI lockAPI = LockFactory.getInstance(LockAPI.getInstance().getContext());
                lockAPI.resigeterNotify();

                //调用连接成功接口
                mConnectListener.onSuccess(mConnectedBoxDevice, mBoxName);
            }
        }

        @Override
        public void onConnectFailure(BleException exception) {
            mConnectBoxDevice = null;
            isConnecting = false;
            if (exception instanceof TimeoutException) {
                mConnectListener.onTimeout(Constant.CODE.CODE_TIME_OUT, 5000);
            } else {
                if (mConnectListener != null) {
                    mConnectListener.onFail(Constant.CODE.CODE_CONNECT_FAIL, Constant.MSG.MSG_CONNECT_FAIL);
                }
            }
            Log.i(TAG, exception.getDescription());
        }

        @Override
        public void onDisconnect(boolean isActive) {
            mConnectBoxDevice = null;
            isConnecting = false;
            mConnectListener.onClose(Constant.SERVICE_UUID, mBoxName);
        }
    };


    /********************************** 获取款箱锁具ID ********************************/
    public Result<String> getLockIdByBoxName(String boxName) {
        Result<String> lockIdResult = new Result<>();
        //TODO : 2017/11/23 由于可能款箱名为空，所以去掉是否为空判断
//        if (!TextUtils.isEmpty(boxName)) {
        if (mLockIdMap != null) {
            Log.e("=====>", "1");
            String lockId = mLockIdMap.get(boxName).replace(":", "");
            lockIdResult.setCode(Constant.CODE.CODE_SUCCESS);
            lockIdResult.setMsg(Constant.MSG.MSG_GET_LOCK_ID_SUCCESS);
            lockIdResult.setData(lockId);
            return lockIdResult;
        } else {
            Log.e("=====>", "2");
            lockIdResult.setCode(Constant.CODE.CODE_GET_LOCK_ID_FAIL);
            lockIdResult.setMsg(Constant.MSG.MSG_GET_LOCK_ID_FAIL);
            lockIdResult.setData(null);
            return lockIdResult;
        }
//        } else {
//            Log.e("=====>", "3");
//            lockIdResult.setCode(Constant.CODE.CODE_GET_LOCK_ID_FAIL);
//            lockIdResult.setMsg(Constant.MSG.MSG_GET_LOCK_ID_FAIL);
//            lockIdResult.setData(null);
//            return lockIdResult;
//        }
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
