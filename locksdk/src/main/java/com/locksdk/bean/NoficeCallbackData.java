package com.locksdk.bean;

/**
 * Created by Sujiayong on 2017/11/17.
 * 蓝牙通知Nofice回调
 */

public class NoficeCallbackData {
    private int mFunctionCode;
    private byte[] data;

    public NoficeCallbackData() {
    }

    public int getFunctionCode() {
        return mFunctionCode;
    }

    public void setFunctionCode(int functionCode) {
        mFunctionCode = functionCode;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
