package com.locksdk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.RandomAttr;
import com.locksdk.listener.ActiveLockListener;
import com.locksdk.listener.ApplyPermissionListener;
import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.GetLockIdListener;
import com.locksdk.listener.GetRandomListener;
import com.locksdk.listener.LockStatusListener;
import com.locksdk.listener.NoficeDataListener;
import com.locksdk.listener.OpenLockListener;
import com.locksdk.listener.QueryLogsListener;
import com.locksdk.listener.ScannerListener;
import com.locksdk.util.DealtByteUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.ViseBle;
import com.locksdk.baseble.model.BluetoothLeDevice;
import com.locksdk.baseble.utils.HexUtil;

import java.util.List;
import java.util.Map;

import static com.locksdk.util.ApplyLocationPermissionUtil.applyLocationPermission;

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

    //获取款箱列表
    public void getBoxList(Activity activiy, final ScannerListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.e("======>", ContextCompat.checkSelfPermission(activiy, Manifest.permission.ACCESS_COARSE_LOCATION) + "");
            if (ContextCompat.checkSelfPermission(activiy, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
                activiy.requestPermissions(permissions, 0x01);
            } else {
                LockApiBleUtil.getInstance().startScanner(listener);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            LockApiBleUtil.getInstance().startScanner(listener);
        }

    }

    //获取扫描到的款箱名字，（P:在getBoxList的扫描成功的接口调用，否则可以为null，或size为0）
    public List<String> getScannerBoxNames() {
        return LockApiBleUtil.getInstance().getScannerBoxNames();
    }

    //连接
    public void openConnection(BluetoothLeDevice bluetoothLeDevice, long timeout, ConnectListener listener) {
        LockApiBleUtil.getInstance().openConnection(bluetoothLeDevice, timeout, listener);
    }

    //关闭连接
    public void closeConnection(String uuid) {
        LockApiBleUtil.getInstance().closeConnection();
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

    //获取锁具ID
    public void getLockIdByBoxName(String boxName, GetLockIdListener lockIdListener) {
        LockApiBleUtil.getInstance().getLockIdByBoxName(boxName, lockIdListener);
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
                Log.e((callbackData.getFunctionCode()) + "====>", callbackData.getData().length + "====" + HexUtil.encodeHexStr(callbackData.getData()));
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

                //TODO : 2017/11/23 数值固定
                byte[] btCloseCode = new byte[10];
                System.arraycopy(data, 24, btCloseCode, 0, btCloseCode.length);
                Log.e(TAG, HexUtil.encodeHexStr(btCloseCode));
                randomAttr.setCloseCode(new String(btCloseCode));
//                randomAttr.setCloseCode("12345");

                byte[] btDpCommKeyVer = new byte[36];
                System.arraycopy(data, 34, btDpCommKeyVer, 0, btDpCommKeyVer.length);
                Log.e(TAG, HexUtil.encodeHexStr(btCloseCode));
                randomAttr.setDpCommKeyVer(new String(btDpCommKeyVer));
//                randomAttr.setDpCommKeyVer("123123215432");

                byte[] btDpKeyVer = new byte[36];
                System.arraycopy(data, 70, btDpKeyVer, 0, btDpKeyVer.length);
                Log.e(TAG, HexUtil.encodeHexStr(btCloseCode));
                randomAttr.setDpKeyVer(new String(btDpKeyVer));
//                randomAttr.setDpKeyVer("sadasds");

                getRandomResult.setData(randomAttr);
                mGetRandomListener.getRandomCallback(getRandomResult);
                break;
            case (byte) 0x92:
                Result<String> openLockResult = new Result<>();
                data = callbackData.getData();
                btBoxName1 = new byte[16];
                System.arraycopy(data, 0, btBoxName1, 0, btBoxName1.length);
                btBoxName = DealtByteUtil.dataClear0(btBoxName1);

                byte[] btCode = new byte[2];
                System.arraycopy(data, 16, btCode, 0, btCode.length);

                byte[] callbackInfo = new byte[16];
                System.arraycopy(data, 18, callbackInfo, 0, callbackInfo.length);
                String msg = " ";
                if (HexUtil.encodeHexStr(btCode).equals("0000")) {
                     msg = "款箱名：" + (new String(btBoxName)) + "返回结果：" + HexUtil.encodeHexStr(btCode)
                            + "信息：" +"开锁成功";
                } else {
                     msg = "款箱名：" + (new String(btBoxName)) + "返回结果：" + HexUtil.encodeHexStr(btCode)
                            + "信息：" + (new String(callbackInfo));
                }

                openLockResult.setCode("0000");
                openLockResult.setMsg("开锁成功");
                openLockResult.setData(msg);
                mOpenLockListener.openLockCallback(openLockResult);
                break;
            case (byte) 0x93:
                Result<String> queryLogsCallbackResult = new Result<>();
                queryLogsCallbackResult.setCode("0000");
                queryLogsCallbackResult.setMsg("查询日志成功");
                queryLogsCallbackResult.setData(HexUtil.encodeHexStr(callbackData.getData()));
                mQueryLogsListener.queryLogsCallback(queryLogsCallbackResult);
                break;
            case (byte) 0x94:
                byte[] data2 = callbackData.getData();
                Log.e(TAG, "长度" + data2.length);
                byte[] btBoxName3 = new byte[16];
                System.arraycopy(data2, 1, btBoxName3, 0, btBoxName3.length);
                byte[] btBoxName4 = DealtByteUtil.dataClear0(btBoxName3);
                String boxName = new String(btBoxName4);
                //TODO : 2017/11/23 锁具ID这里处理可能有问题
                mLockStatusListener.onChange(boxName, LockApiBleUtil.getInstance().getConnectedBoxDevice().getAddress(), null);
                break;
        }

    }
}
