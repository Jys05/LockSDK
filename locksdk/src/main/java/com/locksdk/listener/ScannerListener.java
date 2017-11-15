package com.locksdk.listener;

import com.locksdk.Result;
import com.vise.baseble.model.BluetoothLeDevice;

import java.util.List;

/**
 * Created by Sujiayong on 2017/11/14.
 */

public interface ScannerListener {

    void onScannering(String code , String mag);

    void onBoxFound(Result<List<String>> boxs_Name);

    void onScannerFail(String code , String mag);

}
