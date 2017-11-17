package com.locksdk.listener;

/**
 * Created by Sujiayong on 2017/11/17.
 * 蓝牙状态监听
 */

public interface BleStateListener {

    void onState_ON();

    void onState_OFF();
}
