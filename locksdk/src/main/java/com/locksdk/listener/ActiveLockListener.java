package com.locksdk.listener;

import com.locksdk.Result;

/**
 * Created by Sujiayong on 2017/11/17.
 * 激活款箱监听
 */

public interface ActiveLockListener {
    void activeLockCallback(Result<String> result);
}
