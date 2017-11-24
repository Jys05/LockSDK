package com.locksdk.baseble.exception.handler;

import com.locksdk.baseble.exception.ConnectException;
import com.locksdk.baseble.exception.GattException;
import com.locksdk.baseble.exception.InitiatedException;
import com.locksdk.baseble.exception.OtherException;
import com.locksdk.baseble.exception.TimeoutException;

/**
 * @Description: 异常默认处理
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 16/8/14 10:35.
 */
public class DefaultBleExceptionHandler extends BleExceptionHandler {
    @Override
    protected void onConnectException(ConnectException e) {
//        ViseLog.e(e.getDescription());
    }

    @Override
    protected void onGattException(GattException e) {
//        ViseLog.e(e.getDescription());
    }

    @Override
    protected void onTimeoutException(TimeoutException e) {
//        ViseLog.e(e.getDescription());
    }

    @Override
    protected void onInitiatedException(InitiatedException e) {
//        ViseLog.e(e.getDescription());
    }

    @Override
    protected void onOtherException(OtherException e) {
//        ViseLog.e(e.getDescription());
    }
}