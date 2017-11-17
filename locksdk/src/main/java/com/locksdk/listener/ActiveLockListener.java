package com.locksdk.listener;

/**
 * Created by Sujiayong on 2017/11/17.
 * 激活款箱监听
 */

public interface ActiveLockListener {
    void onActiveLockSuccess();

    void onActiviteLockFail(String code, String msg);
}
