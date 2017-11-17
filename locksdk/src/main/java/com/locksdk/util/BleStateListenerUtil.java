package com.locksdk.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.locksdk.listener.BleStateListener;
import com.vise.baseble.ViseBle;

/**
 * Created by Sujiayong on 2017/11/17.
 * 蓝牙状态监听工具
 */

public class BleStateListenerUtil {

    private static BleStateListener mBleStateListener;     //蓝牙状态监听

    //监听蓝牙广播的过滤
    public static void setOnBleReceiver(final BleStateListener listener) {
        if (ViseBle.getInstance().getContext() == null) return;
        mBleStateListener = listener;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");

        Context context = ViseBle.getInstance().getContext();
        context.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    //蓝牙的广播接收器
    public static BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_ON: //蓝牙状态打开
                            mBleStateListener.onState_ON();
                        case BluetoothAdapter.STATE_OFF:
                            mBleStateListener.onState_OFF();
                            break;
                    }
                    break;
            }
        }
    };

    //关闭监听蓝牙广播接收器
    public static void closeBleReceiver() {
        if (ViseBle.getInstance().getContext() == null) return;
        ViseBle.getInstance().getContext().unregisterReceiver(mBroadcastReceiver);
    }

}
