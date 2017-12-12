package com.locksdk.listener;

import com.locksdk.lockApi.Result;

/**
 * Created by Sujiayong on 2017/11/23.
 */

public interface GetLockIdListener {
    void onGetLockIDListener(Result< String> resultLockID);
}
