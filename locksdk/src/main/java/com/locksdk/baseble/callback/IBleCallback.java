package com.locksdk.baseble.callback;


import com.locksdk.baseble.core.BluetoothGattChannel;
import com.locksdk.baseble.exception.BleException;
import com.locksdk.baseble.model.BluetoothLeDevice;

/**
 * @Description: 操作数据回调
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/10/17 19:42
 */
public interface IBleCallback {
    void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice);

    void onFailure(BleException exception);
}
