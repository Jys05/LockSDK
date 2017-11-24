package com.locksdk.baseble.callback.scan;

import android.os.ParcelUuid;

import com.locksdk.baseble.model.BluetoothLeDevice;
import com.locksdk.baseble.model.BluetoothLeDeviceStore;

import java.util.UUID;

/**
 * @Description: 根据指定uuid过滤设备
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 17/9/12 23:20.
 */
public class UuidFilterScanCallback extends ScanCallback {
    private UUID uuid;//设备uuid

    public UuidFilterScanCallback(IScanCallback scanCallback) {
        super(scanCallback);
    }

    public UuidFilterScanCallback setUuid(String uuid) {
        this.uuid = UUID.fromString(uuid);
        bluetoothLeDeviceStore.clear();
        return this;
    }

    public UuidFilterScanCallback setUuid(UUID uuid) {
        this.uuid = uuid;
        bluetoothLeDeviceStore.clear();
        return this;
    }

    @Override
    public BluetoothLeDeviceStore onFilter(BluetoothLeDevice bluetoothLeDevice) {
        if (bluetoothLeDevice != null && bluetoothLeDevice.getDevice() != null
                && bluetoothLeDevice.getDevice().getUuids() != null
                && bluetoothLeDevice.getDevice().getUuids().length > 0) {
            for (ParcelUuid parcelUuid : bluetoothLeDevice.getDevice().getUuids()) {
                if (uuid != null && uuid == parcelUuid.getUuid()) {
                    bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
                }
            }
        }
        return bluetoothLeDeviceStore;
    }
}
