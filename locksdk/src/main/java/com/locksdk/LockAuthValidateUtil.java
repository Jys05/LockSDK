package com.locksdk;

import android.bluetooth.BluetoothGattService;

import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.core.DeviceMirrorPool;

import java.util.UUID;

/**
 * Created by Sujiayong on 2017/11/15.
 * 对款箱操作：激活（身份验证） ，获取随机数 ，开锁，查询锁具状态 ， 查询日志
 * <p>都应在连接款箱成功后操作</>
 */

public class LockAuthValidateUtil {

    private DeviceMirrorPool mDeviceMirrorPool;

    public void writeFunctionCode() {
        if (LockApiBleUtil.getInstance().getGatt() == null) return;
        BluetoothGattService bluetoothGattService = LockApiBleUtil.getInstance().getBluetoothGattService();
        UUID characteristicUUID = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.WRITE_UUID)).getUuid();
        UUID mServiceUUID = bluetoothGattService.getUuid();
        DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(mBluetoothLeDevice);
    }
}
