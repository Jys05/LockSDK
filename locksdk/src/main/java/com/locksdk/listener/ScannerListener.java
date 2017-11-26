package com.locksdk.listener;

import com.locksdk.Result;
import com.locksdk.baseble.model.BluetoothLeDevice;

import java.util.List;

/**
 * Created by Sujiayong on 2017/11/14.
 * 扫描监听
 */

public interface ScannerListener {

    void onStartScanner(String code, String msg);

    void onBoxFoundScanning(Result<List<BluetoothLeDevice>> scannerDevideResult , Result<List<String>> boxNamesResult);

    void onScannerFail(String code, String msg);

}
