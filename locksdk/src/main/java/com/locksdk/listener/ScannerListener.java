package com.locksdk.listener;

import com.locksdk.Result;
import com.locksdk.baseble.model.BluetoothLeDevice;

import java.util.List;

/**
 * Created by Sujiayong on 2017/11/14.
 * 扫描监听
 */

public interface ScannerListener {

    void onStartScanner(String code , String mag);

    void onBoxFoundScanning(Result<List<BluetoothLeDevice>> boxs_Name);

    void onScannerFail(String code , String msg);

}
