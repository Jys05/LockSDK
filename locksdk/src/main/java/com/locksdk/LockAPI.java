package com.locksdk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

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
import com.locksdk.util.LogsDataUtil;
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
        mContext = context.getApplicationContext();
        LockApiBleUtil.getInstance().init(context);
        return this;
    }

    public static final int PREMISSION_REQUEST_CODE = 0x01;

    //获取款箱列表
    public void getBoxList(Activity activiy, String uuid, final ScannerListener listener) {
        if (!TextUtils.isEmpty(uuid)) {
            Constant.SERVICE_UUID = uuid;
        }
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
        return LockApiBleUtil.getInstance().getScannerBoxNames();
    }

    //连接
    public void openConnection(BluetoothLeDevice bluetoothLeDevice, long timeout, ConnectListener listener) {
        LockApiBleUtil.getInstance().openConnection(bluetoothLeDevice, timeout, listener);
    }

    //关闭连接
    public void closeConnection() {
        LockApiBleUtil.getInstance().closeConnection();
    }

    //获取锁具ID
    public void getLockIdByBoxName(GetLockIdListener lockIdListener) {
        LockApiBleUtil.getInstance().getLockIdByBoxName(lockIdListener);
    }

    //激活
    public void activeLock(Map<String, String> param, ActiveLockListener lockListener) {
        mActiveLockListener = lockListener;
        ActiveLockUtil.activeLock(param, lockListener);
    }

    //注册锁具状态
    public LockAPI registerLockStatusListener(LockStatusListener lockStatusListener) {
        this.mLockStatusListener = lockStatusListener;
        return this;
    }

    //开锁
    public void openLock(Map<String, String> param, OpenLockListener lockListener) {
        mOpenLockListener = lockListener;
        OpenLockUtil.opnenLock(param, lockListener);
    }

    //获取随机数（开箱触发）
    public void getRandom(String boxName, GetRandomListener listener) {
        mGetRandomListener = listener;
        GetRandomUtil.getRandom(boxName, listener);
    }


    //查询锁状态
    public void queryLockStatus(String lockId) {
        QueryLockStatusUtil.queryLockStatus(lockId, mLockStatusListener);
    }

    //查询日志
    public void queryLogs(Map<String, String> param, QueryLogsListener logsListener) {
        mQueryLogsListener = logsListener;
        QueryLogsUtil.queryLogs(param, logsListener);
    }

    //注册蓝牙通知监听
    public void resigeterNotify() {
        WriteAndNoficeUtil.getInstantce().noficeFunctionCode(mNoficeDataListener);     //设置通知监听
    }

    private NoficeDataListener mNoficeDataListener = new NoficeDataListener() {
        @Override
        public void onNoficeSuccess(NoficeCallbackData callbackData) {
            if (callbackData.isFinish()) {
                dealtNotifyCallBackData(callbackData);
            }
        }

        @Override
        public void onNoficeFail(NoficeCallbackData callbackData) {

        }
    };

    private void dealtNotifyCallBackData(NoficeCallbackData callbackData) {
        //TODO : 2017/11/23 没有加监听是否为空判断
        byte[] data;
        byte[] btBoxName1;
        byte[] btBoxName;
        switch (callbackData.getFunctionCode()) {
            case (byte) 0x90:
                Result<String> activiteLockResult = new Result<>();
                data = callbackData.getData();
                btBoxName1 = new byte[16];
                System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
                btBoxName = DealtByteUtil.dataClear0(btBoxName1);
                activiteLockResult.setCode("0000");
                activiteLockResult.setMsg("激活成功");
                activiteLockResult.setData(new String(btBoxName));
                mActiveLockListener.activeLockCallback(activiteLockResult);
                break;
            case (byte) 0x91:
                Result<RandomAttr> getRandomResult = new Result<>();
                RandomAttr randomAttr = new RandomAttr();
                getRandomResult.setCode("0000");
                getRandomResult.setMsg("获取成功");
                data = callbackData.getData();
                Log.e(TAG, "长度" + data.length);
                btBoxName1 = new byte[16];
                System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
                btBoxName = DealtByteUtil.dataClear0(btBoxName1);
                randomAttr.setBoxName(new String(btBoxName));

                byte[] btRandom = new byte[8];
                System.arraycopy(data, 16, btRandom, 0, btRandom.length);
                randomAttr.setRandom(new String(btRandom));

                byte[] btCloseCode = new byte[10];
                System.arraycopy(data, 24, btCloseCode, 0, btCloseCode.length);
                Log.e(TAG, HexUtil.encodeHexStr(btCloseCode));
                randomAttr.setCloseCode(new String(btCloseCode));

                byte[] btDpCommKeyVer = new byte[36];
                System.arraycopy(data, 34, btDpCommKeyVer, 0, btDpCommKeyVer.length);
                Log.e(TAG, HexUtil.encodeHexStr(btCloseCode));
                randomAttr.setDpCommKeyVer(new String(btDpCommKeyVer));

                byte[] btDpKeyVer = new byte[36];
                System.arraycopy(data, 70, btDpKeyVer, 0, btDpKeyVer.length);
                Log.e(TAG, HexUtil.encodeHexStr(btCloseCode));
                randomAttr.setDpKeyVer(new String(btDpKeyVer));

                getRandomResult.setData(randomAttr);
                mGetRandomListener.getRandomCallback(getRandomResult);
                break;
            case (byte) 0x92:
                Result<String> openLockResult = new Result<>();
                data = callbackData.getData();
                Log.e(TAG, "开锁：" + HexUtil.encodeHexStr(data));
                btBoxName1 = new byte[16];
                System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
                btBoxName = DealtByteUtil.dataClear0(btBoxName1);
                String strBoxName = new String(btBoxName);
                Log.e(TAG, "开锁款箱名：" + btBoxName + "====" + HexUtil.encodeHexStr(btBoxName1));
                byte[] btCode = new byte[2];
                System.arraycopy(data, 16, btCode, 0, btCode.length);
                Log.e(TAG, "开锁信息码：" + HexUtil.encodeHexStr(btCode));
                byte[] callbackInfo = new byte[16];
                System.arraycopy(data, 18, callbackInfo, 0, callbackInfo.length);
                byte[] callbackInfo2 = DealtByteUtil.dataClear0(callbackInfo);      //去零
                Log.e(TAG, "开锁信息：" + new String(callbackInfo2) + "====" + HexUtil.encodeHexStr(callbackInfo));
                //4b583030310000000000000000000000 0300 766572696679206661696c6564000000

                //4b583030310000000000000000000000 0000 73756363657373000000000000000000
                if (HexUtil.encodeHexStr(btCode).equals("0000")) {
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
                Result<List<LockLog>> queryLogsCallbackResult = new Result<>();
                queryLogsCallbackResult.setCode("0000");
                queryLogsCallbackResult.setMsg("查询日志成功");
                Log.e(TAG, HexUtil.encodeHexStr(callbackData.getData()));
                queryLogsCallbackResult.setData(LogsDataUtil.dealLogsData(callbackData.getData()));
                mQueryLogsListener.queryLogsCallback(queryLogsCallbackResult);
                break;
            case (byte) 0x94:
                byte[] data2 = callbackData.getData();
                Log.e(TAG, "长度" + data2.length);
                byte[] btBoxName3 = new byte[16];
                System.arraycopy(data2, 0, btBoxName3, 0, btBoxName3.length);
                byte[] btBoxName4 = DealtByteUtil.dataClear0(btBoxName3);
                String boxName = new String(btBoxName4);

                byte[] btBoxStatus = new byte[2];
                System.arraycopy(data2, data2.length - 2, btBoxStatus, 0, btBoxStatus.length);

                mLockStatusListener.onChange(boxName, LockApiBleUtil.getInstance().getLockIDStr(), LockStatusUtil.getBoxStatus(btBoxStatus[0], btBoxStatus[1]));
                break;
        }

    }
}
