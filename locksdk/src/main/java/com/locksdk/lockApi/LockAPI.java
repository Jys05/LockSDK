package com.locksdk.lockApi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.locksdk.bean.LockLog;
import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.RandomAttr;
import com.locksdk.listener.ActiveLockListener;
import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.GetLockIdListener;
import com.locksdk.listener.GetRandomListener;
import com.locksdk.listener.LockStatusListener;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.OpenLockListener;
import com.locksdk.listener.QueryLogsListener;
import com.locksdk.listener.ScannerListener;
import com.locksdk.util.DealtByteUtil;
import com.locksdk.util.LockStatusUtil;
import com.locksdk.util.LogUtil;
import com.locksdk.util.LogsDataUtil;
import com.locksdk.util.LogsDataUtil.*;
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.model.BluetoothLeDevice;
import com.locksdk.baseble.utils.HexUtil;

import java.util.List;
import java.util.Map;


/**
 * Created by Sujiayong on 2017/11/14.
 * 该部分定义APP操作锁具所需要的API。
 * 获取方法：LockAPI  api=LockFactory.getInstance(Context ctx);
 */

public class LockAPI {

    private static final String TAG = "LockAPI";
    private static LockAPI instance;


    private Context mContext;
    private LockStatusListener mLockStatusListener;
    private GetRandomListener mGetRandomListener;
    private QueryLogsListener mQueryLogsListener;
    private OpenLockListener mOpenLockListener;
    private ActiveLockListener mActiveLockListener;
    private boolean isWriting = false;      //为了设备自动休眠添加的，判断是否写入
    private int mDeviceSleepTime = 7000;   //设备休眠时间

    private LockAPI() {

    }

    public static LockAPI getInstance() {
        if (instance == null) {
            synchronized (LockAPI.class) {
                if (instance == null) {
                    instance = new LockAPI();
                }
            }
        }
        return instance;
    }

    public Context getContext() {
        return mContext;
    }

    public LockAPI init(Context context) {
        if (context == null) {
            return null;
        }
        LogUtil.setEnable(false);
        mContext = context.getApplicationContext();
        LockApiBleUtil.getInstance().init(context);
        return this;
    }

    public static final int PREMISSION_REQUEST_CODE = 0x01;


    public boolean isWriting() {
        return isWriting;
    }

    public void setWriting(boolean writing) {
        isWriting = writing;
    }


    public int getDeviceSleepTime() {
        return mDeviceSleepTime;
    }

    public LockAPI setSleepModel(boolean isAllowSleep) {
        LogUtil.i(TAG, "关闭以及开启“防止休眠模式”默认是不休眠模式，isAllowSleep为true是允许设备（款箱）休眠");
        LockApiBleUtil.getInstance().setSleepModel(isAllowSleep);
        return this;
    }

    public LockAPI setLogsUtil(boolean isLogEnable) {
        LogUtil.i(TAG, "关闭以及开启Log打印，默认关闭");
        LogUtil.setEnable(isLogEnable);
        return this;
    }


    //获取款箱列表
    public void getBoxList(Activity activiy, String uuid, final ScannerListener listener) {
        LogUtil.i(TAG, "获取款箱列表");
        if (!TextUtils.isEmpty(uuid)) {
            Constant.SERVICE_UUID = uuid;
        }
        removeCallbacksAndMessages();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(activiy, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
                activiy.requestPermissions(permissions, PREMISSION_REQUEST_CODE);
            } else {
                LockApiBleUtil.getInstance().startScanner(listener);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            LockApiBleUtil.getInstance().startScanner(listener);
        }
    }

    //获取款箱列表
    public void getBoxList(String uuid, final ScannerListener listener) {
        LogUtil.i(TAG, "获取款箱列表");
        if (!TextUtils.isEmpty(uuid)) {
            Constant.SERVICE_UUID = uuid;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                listener.onScannerFail(Constant.CODE.CODE_SCANNER_FAIL2, Constant.MSG.MSG_LOCATION_PERSISION);
            } else {
                LockApiBleUtil.getInstance().startScanner(listener);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            LockApiBleUtil.getInstance().startScanner(listener);
        }
    }

    //获取扫描到的款箱名字，（P:在getBoxList的扫描成功的接口调用，否则可以为null，或size为0）
    public Result<List<String>> getScannerBoxNames() {
        LogUtil.i(TAG, "获取扫描到的款箱名字");
        return LockApiBleUtil.getInstance().getScannerBoxNames();
    }

    //连接
    public void openConnection(BluetoothLeDevice bluetoothLeDevice, long timeout, ConnectListener listener) {
        LogUtil.i(TAG, "连接");
        LockApiBleUtil.getInstance().openConnection(bluetoothLeDevice, timeout, listener);
    }

    //关闭连接
    public boolean closeConnection() {
        LogUtil.i(TAG, "关闭连接");
        return LockApiBleUtil.getInstance().closeConnection();
    }

    //获取锁具ID
    public void getLockIdByBoxName(GetLockIdListener lockIdListener) {
        LogUtil.i(TAG, "获取锁具ID");
        LockApiBleUtil.getInstance().getLockIdByBoxName(lockIdListener);
    }

    private String deviceBusyCode = Constant.CODE.DEVICE_BUSY;
    private String deviceBusyMsg = Constant.MSG.MSG_DEVICE_BUSY;
    private int mTryAgainCount = 1;         //相当于用于保存设置后的TryAgainCount值，然后在每一个接口赋值给LockApiBleUtil中的

    public void setTryAgainCount(int tryAgainCount) {
        this.mTryAgainCount = tryAgainCount;
        LockApiBleUtil.getInstance().setTryAgainCount(tryAgainCount);
    }

    //激活
    public void activeLock(Map<String, String> param, ActiveLockListener activeLockListener) {
        LogUtil.i(TAG, (WriteAndNoficeUtil.getInstantce().getWriteData() == null) ? "空" : "不为空");
        LogUtil.i(TAG, "isWriting:" + isWriting);
        if (WriteAndNoficeUtil.getInstantce().getWriteData() == null) {

            LogUtil.i(TAG, "激活");
            removeCallbacksAndMessages();
            LogUtil.i(TAG, "设置多少时间后重发和设置重发次数");
            LockApiBleUtil.getInstance().setTryAgainCount(mTryAgainCount);
            LockApiBleUtil.getInstance().setWriteSecondTime(6500);
            mActiveLockListener = activeLockListener;
            ActiveLockUtil.activeLock(param, activeLockListener);
        } else {
            if (activeLockListener != null) {
                Result<String> activiteResult = new Result<>();
                activiteResult.setCode(deviceBusyCode);
                activiteResult.setMsg(deviceBusyMsg);
                activiteResult.setData(null);
                activeLockListener.activeLockCallback(activiteResult);
            }
        }
    }

    //注册锁具状态
    public LockAPI registerLockStatusListener(LockStatusListener lockStatusListener) {
        LogUtil.i(TAG, "注册锁具状态");
        removeCallbacksAndMessages();
        this.mLockStatusListener = lockStatusListener;
        return this;
    }

    //开锁
    public void openLock(Map<String, String> param, OpenLockListener openLockListener) {
        LogUtil.i(TAG, (WriteAndNoficeUtil.getInstantce().getWriteData() == null) ? "空" : "不为空");
        LogUtil.i(TAG, "isWriting:" + isWriting);
        if (WriteAndNoficeUtil.getInstantce().getWriteData() == null) {
            LogUtil.i(TAG, "开锁");
            removeCallbacksAndMessages();
            mOpenLockListener = openLockListener;
            LogUtil.i(TAG, "设置多少时间后重发和设置重发次数");
            LockApiBleUtil.getInstance().setTryAgainCount(mTryAgainCount);
            LockApiBleUtil.getInstance().setWriteSecondTime(3500);
            OpenLockUtil.opnenLock(param, openLockListener);
        } else {
            if (openLockListener != null) {
                Result<String> openLockResult = new Result<>();
                openLockResult.setCode(deviceBusyCode);
                openLockResult.setMsg(deviceBusyMsg);
                openLockResult.setData(null);
                openLockListener.openLockCallback(openLockResult);
            }
        }
    }

    //获取随机数（开箱触发）
    public void getRandom(String boxName, GetRandomListener getRandomListener) {
        LogUtil.i(TAG, (WriteAndNoficeUtil.getInstantce().getWriteData() == null) ? "空" : "不为空");
        LogUtil.i(TAG, "isWriting:" + isWriting);
        if (WriteAndNoficeUtil.getInstantce().getWriteData() == null) {
            LogUtil.i(TAG, "获取随机数（开箱触发）");
            removeCallbacksAndMessages();
            LogUtil.i(TAG, "设置多少时间后重发和设置重发次数");
            LockApiBleUtil.getInstance().setTryAgainCount(mTryAgainCount);
            LockApiBleUtil.getInstance().setWriteSecondTime(3500);
            mGetRandomListener = getRandomListener;
            GetRandomUtil.getRandom(boxName, getRandomListener);
        } else {
            if (getRandomListener != null) {
                Result<RandomAttr> randomAttrResult = new Result<>();
                randomAttrResult.setCode(deviceBusyCode);
                randomAttrResult.setMsg(deviceBusyMsg);
                randomAttrResult.setData(null);
                getRandomListener.getRandomCallback(randomAttrResult);
            }
        }
    }


    //查询锁状态
    public void queryLockStatus(String lockId) {
        LogUtil.i(TAG, (WriteAndNoficeUtil.getInstantce().getWriteData() == null) ? "空" : "不为空");
        LogUtil.i(TAG, "isWriting:" + isWriting);
        if (WriteAndNoficeUtil.getInstantce().getWriteData() == null) {
            LogUtil.i(TAG, "查询锁状态");
            removeCallbacksAndMessages();
            LogUtil.i(TAG, "设置多少时间后重发和设置重发次数");
            LockApiBleUtil.getInstance().setTryAgainCount(mTryAgainCount);
            LockApiBleUtil.getInstance().setWriteSecondTime(3500);
            QueryLockStatusUtil.queryLockStatus(lockId, mLockStatusListener);
        } else {
            if (mLockStatusListener != null) {
                mLockStatusListener.onChange(null, LockApiBleUtil.getInstance().getLockIDStr(), null, deviceBusyMsg);
            }
        }
    }

    //查询日志
    public void queryLogs(Map<String, String> param, QueryLogsListener logsListener) {
        LogUtil.i(TAG, (WriteAndNoficeUtil.getInstantce().getWriteData() == null) ? "空" : "不为空");
        LogUtil.i(TAG, "isWriting:" + isWriting);
        if (WriteAndNoficeUtil.getInstantce().getWriteData() == null) {
            LogUtil.i(TAG, "查询日志");
            removeCallbacksAndMessages();
            LogUtil.i(TAG, "设置多少时间后重发和设置重发次数");
            LockApiBleUtil.getInstance().setTryAgainCount(mTryAgainCount);
            LockApiBleUtil.getInstance().setWriteSecondTime(3500);
            mQueryLogsListener = logsListener;
            QueryLogsUtil.queryLogs(param, logsListener);
        } else {
            if (logsListener != null) {
                Result<List<LockLog>> logsResult = new Result<>();
                logsResult.setCode(deviceBusyCode);
                logsResult.setMsg(deviceBusyMsg);
                logsResult.setData(null);
                logsListener.queryLogsCallback(logsResult);
            }
        }
    }

    //注册蓝牙通知监听
    public void resigeterNotify() {
        LogUtil.i(TAG, "注册蓝牙通知监听");
        WriteAndNoficeUtil.getInstantce().noficeFunctionCode(mNoficeDataListener);     //设置通知监听
    }

    //清理LockApiBleUtil中防止设备休眠的Handler
    public void removeCallbacksAndMessages() {
        LogUtil.i(TAG, "清理LockApiBleUtil中防止设备休眠的Handler");
        //清理Handler
        LockApiBleUtil.getInstance().clearHandler();
        LockApiBleUtil.getInstance().clearmWriteNotifyHandler();
    }

    private NoficeDataListener mNoficeDataListener = new NoficeDataListener() {
        @Override
        public void onNoficeSuccess(NoficeCallbackData callbackData) {
            if (callbackData.isFinish()) {
                LogUtil.i(TAG, "关闭二次重发机制，以及允许其他接口写入");
                LockApiBleUtil.getInstance().clearIsWriteAndNotifyStart();
                if (callbackData.getData() != null) {
                    LogUtil.i(TAG, callbackData.getData().length + "---" + HexUtil.encodeHexStr(callbackData.getData()));
                    dealtNotifyCallBackDataForSuccess(callbackData);
                } else {
                    dealtNotifyCallBackDataForFail(callbackData);
                }
            }
        }

        @Override
        public void onNoficeFail(NoficeCallbackData callbackData) {
            LogUtil.i(TAG + "onNoficeFail", Constant.MSG.MSG_NOTIFY_FAIL);
            dealtNotifyCallBackDataForFail(callbackData);
        }
    };

    private void dealtNotifyCallBackDataForSuccess(NoficeCallbackData callbackData) {
        //TODO : 2017/11/23 没有加监听是否为空判断
        byte[] data = new byte[(callbackData.getData().length - 1)];
        byte[] btBoxName1;
        byte[] btBoxName;
        LogUtil.i(TAG, "返回拼接好的数据长度：" + callbackData.getData().length + "=====" + HexUtil.encodeHexStr(callbackData.getData()));
        byte responseCode = callbackData.getData()[0];
        System.arraycopy(callbackData.getData(), 1, data, 0, data.length);
        LogUtil.i(TAG, "真正报文长度：" + data.length + "=====" + HexUtil.encodeHexStr(data));
        switch (responseCode) {
            case (byte) 0x90:
                Result<String> activiteLockResult = new Result<>();
//                data = callbackData.getData();
                btBoxName1 = new byte[16];
                System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
                btBoxName = DealtByteUtil.dataClear0(btBoxName1);
                activiteLockResult.setCode(Constant.CODE.CODE_SUCCESS);
                activiteLockResult.setMsg(Constant.MSG.MSG_ACTIVE_SUCCESS);
                activiteLockResult.setData(new String(btBoxName));
                mActiveLockListener.activeLockCallback(activiteLockResult);
                break;
            case (byte) 0x91:
                Result<RandomAttr> getRandomResult = new Result<>();
                RandomAttr randomAttr = new RandomAttr();
//                data = callbackData.getData();
                LogUtil.i(TAG, "长度" + data.length);
                btBoxName1 = new byte[16];
                System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
                btBoxName = DealtByteUtil.dataClear0(btBoxName1);
                randomAttr.setBoxName(new String(btBoxName));

                byte[] btRandom = new byte[8];
                System.arraycopy(data, 16, btRandom, 0, btRandom.length);
                randomAttr.setRandom(new String(btRandom));

                byte[] btCloseCode = new byte[10];
                System.arraycopy(data, 24, btCloseCode, 0, btCloseCode.length);
                LogUtil.i(TAG, HexUtil.encodeHexStr(btCloseCode));
                randomAttr.setCloseCode(new String(btCloseCode));

                byte[] btDpCommKeyVer = new byte[36];
                System.arraycopy(data, 34, btDpCommKeyVer, 0, btDpCommKeyVer.length);
                LogUtil.i(TAG, HexUtil.encodeHexStr(btCloseCode));
                randomAttr.setDpCommKeyVer(new String(btDpCommKeyVer));

                byte[] btDpKeyVer = new byte[36];
                System.arraycopy(data, 70, btDpKeyVer, 0, btDpKeyVer.length);
                LogUtil.i(TAG, HexUtil.encodeHexStr(btCloseCode));
                randomAttr.setDpKeyVer(new String(btDpKeyVer));

                getRandomResult.setCode(Constant.CODE.CODE_SUCCESS);
                getRandomResult.setMsg(Constant.MSG.MSG_GET_RANDOM_SUCCESS);
                getRandomResult.setData(randomAttr);
                mGetRandomListener.getRandomCallback(getRandomResult);
                break;
            case (byte) 0x92:
                Result<String> openLockResult = new Result<>();
//                data = callbackData.getData();
                LogUtil.i(TAG, "开锁：" + HexUtil.encodeHexStr(data));
                btBoxName1 = new byte[16];
                System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
                btBoxName = DealtByteUtil.dataClear0(btBoxName1);
                String strBoxName = new String(btBoxName);
                LogUtil.i(TAG, "开锁款箱名：" + btBoxName + "====" + HexUtil.encodeHexStr(btBoxName1));
                byte[] btCode = new byte[2];
                System.arraycopy(data, 16, btCode, 0, btCode.length);
                LogUtil.i(TAG, "开锁信息码：" + HexUtil.encodeHexStr(btCode));
                byte[] callbackInfo = new byte[16];
                System.arraycopy(data, 18, callbackInfo, 0, callbackInfo.length);
                byte[] callbackInfo2 = DealtByteUtil.dataClear0(callbackInfo);      //去零
                LogUtil.i(TAG, "开锁信息：" + new String(callbackInfo2) + "====" + HexUtil.encodeHexStr(callbackInfo));
                if (HexUtil.encodeHexStr(btCode).equals(Constant.CODE.CODE_SUCCESS)) {
                    openLockResult.setCode(HexUtil.encodeHexStr(btCode));
                    openLockResult.setMsg(new String(callbackInfo2));
                    openLockResult.setData(strBoxName);
                } else {
                    openLockResult.setCode(HexUtil.encodeHexStr(btCode));
                    openLockResult.setMsg(new String(callbackInfo2));
                    openLockResult.setData(strBoxName);
                }
                mOpenLockListener.openLockCallback(openLockResult);
                break;
            case (byte) 0x93:
                LogUtil.i(TAG, "查询日志数据：" + HexUtil.encodeHexStr(data));
                Result<List<LockLog>> queryLogsCallbackResult = new Result<>();
                DeatedLogsData deatedLogsData = LogsDataUtil.dealLogsData(data);
                if (deatedLogsData.isSuccess()) {
                    queryLogsCallbackResult.setCode(Constant.CODE.CODE_SUCCESS);
                    queryLogsCallbackResult.setMsg(Constant.MSG.MSG_QUERY_LOGS_SUCCESS);
                    queryLogsCallbackResult.setData(deatedLogsData.getLockLogList());
                } else {
                    queryLogsCallbackResult.setCode(Constant.CODE.QUERY_LOGS_FAIL);
                    queryLogsCallbackResult.setMsg(Constant.MSG.MSG_QUERY_LOGS_FAIL + "，错误码：" + deatedLogsData.getCallBackResult());
                    queryLogsCallbackResult.setData(null);
                }
                mQueryLogsListener.queryLogsCallback(queryLogsCallbackResult);
                break;
            case (byte) 0x94:
//                byte[] data2 = callbackData.getData();
                LogUtil.i(TAG, "长度" + data.length);
                byte[] btBoxName3 = new byte[16];
                System.arraycopy(data, 0, btBoxName3, 0, btBoxName3.length);
                byte[] btBoxName4 = DealtByteUtil.dataClear0(btBoxName3);
                String boxName = new String(btBoxName4);

                byte[] btBoxStatus = new byte[2];
                System.arraycopy(data, data.length - 2, btBoxStatus, 0, btBoxStatus.length);
                if (mLockStatusListener != null) {
                    mLockStatusListener.onChange(boxName, LockApiBleUtil.getInstance().getLockIDStr(), LockStatusUtil.getBoxStatus(btBoxStatus[0], btBoxStatus[1]), null);
                }
                break;
        }
        LogUtil.i(TAG, (WriteAndNoficeUtil.getInstantce().getWriteData() == null) ? "Success1的空" : "Success1的不为空");
        LogUtil.i(TAG, "Success1的isWriting:" + isWriting);
        //清空写入数据，代表开始可以允许其他接口写入数据
        WriteAndNoficeUtil.getInstantce().setWriteData(null);
        //心跳计时重新开始
        setWriting(false);
        LogUtil.i(TAG, (WriteAndNoficeUtil.getInstantce().getWriteData() == null) ? "Success2的空" : "Success2的不为空");
        LogUtil.i(TAG, "Success2的isWriting:" + isWriting);

        LockApiBleUtil.getInstance().setDeviceSleepTime(mDeviceSleepTime);
        LockApiBleUtil.getInstance().mAllowSleepHandler.sendEmptyMessage(0x00);
    }

    private void dealtNotifyCallBackDataForFail(NoficeCallbackData callbackData) {
        //TODO : 2017/11/23 没有加监听是否为空判断
        if (callbackData.getRespondCode() == 0) return;
        byte responseCode = callbackData.getRespondCode();
        switch (responseCode) {
            case (byte) 0x90:
                Result<String> activiteLockResult = new Result<>();
                activiteLockResult.setCode(Constant.CODE.CODE_ACTIVE_FAIL);
                activiteLockResult.setMsg(Constant.MSG.MSG_NOTIFY_FAIL);
                activiteLockResult.setData(null);
                mActiveLockListener.activeLockCallback(activiteLockResult);
                break;
            case (byte) 0x91:
                Result<RandomAttr> getRandomResult = new Result<>();
                getRandomResult.setCode(Constant.CODE.GET_RANDOM_FAIL);
                getRandomResult.setMsg(Constant.MSG.MSG_NOTIFY_FAIL);
                getRandomResult.setData(null);
                mGetRandomListener.getRandomCallback(getRandomResult);
                break;
            case (byte) 0x92:
                Result<String> openLockResult = new Result<>();
                openLockResult.setCode(Constant.CODE.OPEN_LOCK_FAIL);
                openLockResult.setMsg(Constant.MSG.MSG_NOTIFY_FAIL);
                openLockResult.setData(null);
                mOpenLockListener.openLockCallback(openLockResult);
                break;
            case (byte) 0x93:
                Result<List<LockLog>> queryLogsCallbackResult = new Result<>();
                queryLogsCallbackResult.setCode(Constant.CODE.QUERY_LOGS_FAIL);
                queryLogsCallbackResult.setMsg(Constant.MSG.MSG_NOTIFY_FAIL);
                queryLogsCallbackResult.setData(null);
                mQueryLogsListener.queryLogsCallback(queryLogsCallbackResult);
                break;
            case (byte) 0x94:
                if (mLockStatusListener != null) {
                    mLockStatusListener.onChange(null, LockApiBleUtil.getInstance().getLockIDStr(), null, Constant.MSG.MSG_NOTIFY_FAIL);
                }
                break;
        }
        //清空写入数据，代表开始可以允许其他接口写入数据
        WriteAndNoficeUtil.getInstantce().setWriteData(null);
        setWriting(false);
        //心跳计时重新开始
        LockApiBleUtil.getInstance().setDeviceSleepTime(mDeviceSleepTime);
        LockApiBleUtil.getInstance().mAllowSleepHandler.sendEmptyMessageDelayed(0x00, 100);
    }
}
