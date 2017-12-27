package com.locksdk.baseble.exception.handler;

import com.locksdk.baseble.exception.ConnectException;
import com.locksdk.baseble.exception.GattException;
import com.locksdk.baseble.exception.InitiatedException;
import com.locksdk.baseble.exception.OtherException;
import com.locksdk.baseble.exception.TimeoutException;
import com.locksdk.util.LogUtil;
//import com.locksdk.log.ViseLog;

/**
 * @Description: 异常默认处理
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 16/8/14 10:35.
 */
public class DefaultBleExceptionHandler extends BleExceptionHandler {
     private static final String TAG = "DefaultBleExceptionHandler";
    @Override
    protected void onConnectException(ConnectException e) {
        LogUtil.e(TAG , e.getDescription());
    }

    @Override
    protected void onGattException(GattException e) {
        LogUtil.e(TAG , e.getDescription());
    }

    @Override
    protected void onTimeoutException(TimeoutException e) {
        LogUtil.e(TAG , e.getDescription());
    }

    @Override
    protected void onInitiatedException(InitiatedException e) {
        LogUtil.e(TAG , e.getDescription());
    }

    @Override
    protected void onOtherException(OtherException e) {
        LogUtil.e(TAG , e.getDescription());
    }
}
