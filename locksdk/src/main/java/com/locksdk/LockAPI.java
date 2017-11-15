package com.locksdk;

import android.content.Context;

import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.ScannerListener;

/**
 * Created by Sujiayong on 2017/11/14.
 * 该部分定义APP操作锁具所需要的API。
 * 获取方法：LockAPI  api=LockFactory.getInstance(Context ctx);
 */

public class LockAPI {

    private static final String TAG = "LockAPI";
    private static LockAPI instance;

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


    public void init(Context context) {
        if (context == null) {
            return;
        }
        LockApiBleUtil.getInstance().init(context);
    }


    public void getBoxList(final ScannerListener listener) {
        LockApiBleUtil.getInstance().startScanner(listener);
    }


    public void openConnection(String boxNames, long timeout, ConnectListener listener) {
        LockApiBleUtil.getInstance().openConnection(boxNames, timeout, listener);
    }


    public void closeConnection(String uuid) {
        LockApiBleUtil.getInstance().closeConnection(uuid);
    }


    public void getLockIdByBoxName(String boxName) {
        LockApiBleUtil.getInstance().getLockIdByBoxName(boxName);
    }

}
