package com.locksdk.listener;

import com.locksdk.bean.WriteCallbackData;

/**
 * Created by Sujiayong on 2017/11/17.
 * 对设备写入数据监听
 */

public interface WriteDataListener {
    void onWirteSuccess(WriteCallbackData data);

    void onWriteFail(WriteCallbackData data);       //错误时，数据为null
}
