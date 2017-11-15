package com.locksdk;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.ScannerListener;
import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IConnectCallback;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.UuidFilterScanCallback;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.exception.TimeoutException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private String mBoxName = null;
    private String mBoxMac = null;           //连接后的box的Mac地址——即是box的Mac地址，也是锁具ID
    private List<String> mScanenrBoxNames;
    private Result<List<String>> mScanneredResult;               //扫描返回结果
    private ScannerListener mScannerListener;           //扫描监听
    private BluetoothLeDevice mConnectedBox;            //成功连接后
    private BluetoothGatt mGatt;
    private BluetoothGattService mBluetoothGattService;

    private LockApiBleUtil() {
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
        ViseBle.getInstance().init(context.getApplicationContext());
    }


    /************************* 蓝牙扫描（	获取款箱列表）、停止扫描 **************************/

    /**
     * 获取款箱列表（开始扫描）
     *
     * @return 款箱名称列表
     */
    public void startScanner(final ScannerListener listener) {
        if (listener == null) return;
        if (ViseBle.getInstance().getContext() == null) return;
        if (!isBleEnable(ViseBle.getInstance().getContext())) {

        }
        clearScannerResult();
        mScannerListener = listener;
        mScanneredResult = new Result<>();
        //开始扫描
        mScannerListener.onScannering(Constant.CODE.CODE_SUCCESS, Constant.MSG.MSG_SCANNERING);
        ViseBle.getInstance().startScan(mUuidFilterScanCallback);
    }

    //清理扫描到的结果（防止下次扫描重复）
    private void clearScannerResult() {
        if (mLockIdMap != null) {
            mLockIdMap.clear();
        } else {
            mLockIdMap = new HashMap<>();
        }
        if (mScanenrBoxNames != null) {
            mScanenrBoxNames.clear();
        } else {
            mScanenrBoxNames = new ArrayList<>();
        }
    }

    /**
     * 停止扫描
     */
    public void stopScanner() {
        ViseBle.getInstance().stopScan(mUuidFilterScanCallback);
    }


    private UuidFilterScanCallback mUuidFilterScanCallback = new UuidFilterScanCallback(new IScanCallback() {
        @Override
        public void onDeviceFound(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
            List<BluetoothLeDevice> bluetoothLeDevices = bluetoothLeDeviceStore.getDeviceList();
            for (BluetoothLeDevice bluetoothLeDevice : bluetoothLeDevices) {
                if (!mLockIdMap.containsValue(bluetoothLeDevice.getAddress())) {
                    mLockIdMap.put(bluetoothLeDevice.getName(), bluetoothLeDevice.getAddress());
                }
                if (!mScanenrBoxNames.contains(bluetoothLeDevice.getName())) {
                    mScanenrBoxNames.add(bluetoothLeDevice.getName());
                }
            }
            mScanneredResult.setMsg(Constant.MSG.MSG_SCANNERED);
            mScanneredResult.setCode(Constant.CODE.CODE_SUCCESS);
            mScanneredResult.setData(mScanenrBoxNames);
            mScannerListener.onBoxFound(mScanneredResult);
        }

        //扫描结束后调用。
        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
            //TODO : 2017/11/15 由于目前在init方法中扫面时间设置为-1，所以是永久扫描，以下语句不执行
            if (bluetoothLeDeviceStore.getDeviceList().size() > 0) {
                List<BluetoothLeDevice> bluetoothLeDevices = bluetoothLeDeviceStore.getDeviceList();
                mScanenrBoxNames.clear();
                for (BluetoothLeDevice bluetoothLeDevice : bluetoothLeDevices) {
                    mScanenrBoxNames.add(bluetoothLeDevice.getName());
                }
                mScanneredResult.setMsg(Constant.MSG.MSG_SCANNERED);
                mScanneredResult.setCode(Constant.CODE.CODE_SUCCESS);
                mScanneredResult.setData(mScanenrBoxNames);
                mScannerListener.onBoxFound(mScanneredResult);
            } else {
                mScannerListener.onScannerFail(Constant.CODE.CODE_TIME_OUT, Constant.MSG.MSG_SCANNER_TIME_OUT);
            }
        }

        @Override
        public void onScanTimeout() {
            //TODO : 2017/11/15 由于目前在init方法中扫面时间设置为-1，所以是永久扫描，以下语句不执行
            mScannerListener.onScannerFail(Constant.CODE.CODE_TIME_OUT, Constant.MSG.MSG_SCANNER_TIME_OUT);
        }
    }).setUuid(UUID.fromString(Constant.SERVICE_UUID));


    /**************************** 蓝牙连接、关闭连接 *************************************/

    /**
     * 开始连接
     * 先关闭蓝牙，后再连接（通过广播监听蓝牙状态的变化）
     *
     * @param timeOut  单位毫秒
     * @param listener
     */
    public void openConnection(final String boxName, long timeOut, final ConnectListener listener) {
        if (listener != null) {
            //蓝牙相关配置修改
            if (timeOut > 0) {
                ViseBle.config().setConnectTimeout((int) timeOut);//连接超时时间
            }
            mConnectListener = listener;
            mBoxName = boxName;
            //停止扫描
            stopScanner();
            //注册蓝牙广播监听
            Context context = ViseBle.getInstance().getContext();
            context.registerReceiver(mBroadcastReceiver, makeFilters());
            //关闭蓝牙
            ViseBle.getInstance().getBluetoothAdapter().disable();
        }
    }


    /**
     * 关闭蓝牙款箱连接
     *
     * @param uuid
     */
    //FIXME: 由于项目开发设备连接是用boxName或者BluetoothLeDevice的对象，所以文档有问题
    public void closeConnection(String uuid) {
        //断开连接，将连接成功的box的Mac地址，赋值为空
        mBoxMac = null;
        if (mConnectedBox != null) {
            ViseBle.getInstance().disconnect(mConnectedBox);
            mConnectedBox = null;
        } else {
            ViseBle.getInstance().disconnect();
        }

    }

    //监听蓝牙广播的过滤
    private IntentFilter makeFilters() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        return intentFilter;
    }

    //蓝牙的广播接收器
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_ON: //蓝牙状态打开
                            if (!TextUtils.isEmpty(mBoxName)) {
                                //调用“连接中”接口
                                mConnectListener.onWaiting(Constant.SERVICE_UUID);
                                //蓝牙打开状态时，进行连接操作
                                ViseBle.getInstance().connectByName(mBoxName, mIConnectCallback);
                            } else {
                                mConnectListener.onFail(Constant.SERVICE_UUID
                                        , Constant.MSG.MSG_CONNECT_DEVICE_NULL);
                            }
                        case BluetoothAdapter.STATE_OFF:
                            //蓝牙状态为关闭时，启动蓝牙
                            ViseBle.getInstance().getBluetoothAdapter().enable();
                            break;
                    }
                    break;
            }
        }
    };

    private IConnectCallback mIConnectCallback = new IConnectCallback() {
        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            if (mConnectListener != null) {
                //关闭蓝牙广播监听
                Context context = ViseBle.getInstance().getContext();
                context.unregisterReceiver(mBroadcastReceiver);
                //再次获取连接的款箱名、锁具ID(款箱Mac)
                mBoxName = deviceMirror.getBluetoothLeDevice().getName();
                mBoxMac = deviceMirror.getBluetoothLeDevice().getAddress();
                mConnectedBox = deviceMirror.getBluetoothLeDevice();
                mGatt = deviceMirror.getBluetoothGatt();
                mBluetoothGattService = mGatt.getService(UUID.fromString(Constant.SERVICE_UUID));
                //调用连接成功接口
                mConnectListener.onSuccess(Constant.SERVICE_UUID, deviceMirror.getBluetoothLeDevice().getName());
            }
        }

        @Override
        public void onConnectFailure(BleException exception) {
            if (exception instanceof TimeoutException) {
                mConnectListener.onFail(Constant.CODE.CODE_TIME_OUT, Constant.MSG.MSG_CONNECT_TIME_OUT);
            } else {
                if (mConnectListener != null) {
                    mConnectListener.onFail(Constant.CODE.CODE_CONNECT_FAIL, Constant.MSG.MSG_CONNECT_FAIL);
                }
            }
            Log.i(TAG, exception.getDescription());
        }

        @Override
        public void onDisconnect(boolean isActive) {
            mConnectListener.onClose(Constant.SERVICE_UUID, mBoxName);
        }
    };

    /********************************** 获取款箱锁具ID ********************************/
    public Result<String> getLockIdByBoxName(String boxName) {
        Result<String> lockIdResult = new Result<>();
        if (!TextUtils.isEmpty(boxName)) {
            if (mLockIdMap != null) {
                String lockId = mLockIdMap.get(boxName);
                lockIdResult.setCode(Constant.CODE.CODE_SUCCESS);
                lockIdResult.setMsg(Constant.MSG.MSG_GET_LOCK_ID_SUCCESS);
                lockIdResult.setData(lockId);
                return lockIdResult;
            } else {
                lockIdResult.setCode(Constant.CODE.CODE_GET_LOCK_ID_FAIL);
                lockIdResult.setMsg(Constant.MSG.MSG_GET_LOCK_ID_FAIL);
                lockIdResult.setData(null);
                return lockIdResult;
            }
        } else {
            lockIdResult.setCode(Constant.CODE.CODE_GET_LOCK_ID_FAIL);
            lockIdResult.setMsg(Constant.MSG.MSG_GET_LOCK_ID_FAIL);
            lockIdResult.setData(null);
            return lockIdResult;
        }
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
}
