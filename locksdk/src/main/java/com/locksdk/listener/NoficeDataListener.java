package com.locksdk.listener;

import com.locksdk.bean.NoficeCallbackData;
import com.locksdk.bean.WriteCallbackData;

/**
 * Created by Sujiayong on 2017/11/17.
 * 对设备通知Nofice数据监听
 */

public interface NoficeDataListener {
    void onNoficeSuccess(NoficeCallbackData data);

    void onNoficeFail(NoficeCallbackData data);
}
