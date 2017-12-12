package com.locksdk.listener;

import com.locksdk.lockApi.Result;

/**
 * Created by Sujiayong on 2017/11/21.
 * 开锁监听
 */

public interface OpenLockListener {

    void openLockCallback(Result<String> open);
}
