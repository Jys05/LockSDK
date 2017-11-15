package com.locksdk.listener;

/**
 * Created by Sujiayong on 2017/11/14.
 * 连接监听器接口
 */

public interface ConnectListener {

    void onWaiting(String uuid);//正在连接过程中

    void onSuccess(String uuid, String boxName);//连接成功

    void onFail(String uuid, String msg);//连接失败

    void onTimeout(String uuid, long timeout);//连接超时

    void onClose(String uuid, String boxName);//连接关闭

}
