package com.locksdk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.locksdk.listener.ApplyPermissionListener;
import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.ScannerListener;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.xsnow.permission.OnPermissionCallback;
import com.vise.xsnow.permission.PermissionManager;

import java.util.List;

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


    public LockAPI init(Context context) {
        if (context == null) {
            return null;
        }
        mContext = context.getApplicationContext();
        LockApiBleUtil.getInstance().init(context);
        return this;
    }


    //获取款箱列表
    public void getBoxList(final ScannerListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            applyLocationPermission(new ApplyPermissionListener() {
                @Override
                public void onAllow() {
                    LockApiBleUtil.getInstance().startScanner(listener);
                }

                @Override
                public void onRefuse() {
                    Toast.makeText(mContext, Constant.OPEN_LOCATION_PERSIMISSON, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNoAsk() {
                    Toast.makeText(mContext, Constant.OPEN_LOCATION_PERSIMISSON, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            LockApiBleUtil.getInstance().startScanner(listener);
        }

    }

    //获取扫描到的款箱名字，（P:在getBoxList的扫描成功的接口调用，否则可以为null，或size为0）
    public List<String> getScannerBoxNames() {
        return LockApiBleUtil.getInstance().getScannerBoxNames();
    }

    public void openConnection(BluetoothLeDevice bluetoothLeDevice, long timeout, ConnectListener listener) {
        LockApiBleUtil.getInstance().openConnection(bluetoothLeDevice, timeout, listener);
    }


    public void closeConnection(String uuid) {
        LockApiBleUtil.getInstance().closeConnection();
    }

    public Result<String> activeLock(){
        return LockAuthValidateUtil.activeLock();
    }


    public Result<String> getLockIdByBoxName(String boxName) {
        return LockApiBleUtil.getInstance().getLockIdByBoxName(boxName);
    }
}
