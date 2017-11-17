package com.locksdk.listener;

/**
 * Created by Sujiayong on 2017/11/17.
 * 权限申请监听
 */

public interface ApplyPermissionListener {

    void onAllow();

    void onRefuse();

    void onNoAsk();
}
