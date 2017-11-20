package com.locksdk.listener;


import com.locksdk.bean.LockStatus;

/**
 * Created by Sujiayong on 2017/11/15.
 */

public interface LockStatusListener {
    void onChange(String boxName, String lockId, LockStatus newStatus);
}
