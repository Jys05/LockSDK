package com.locksdk.bean;

/**
 * Created by Sujiayong on 2017/11/17.
 * 蓝牙通知Nofice回调
 */

public class NoficeCallbackData {
    private byte mFunctionCode;
    private byte[] data;
    private boolean isFinish;

    public NoficeCallbackData() {
    }

    public byte getFunctionCode() {
        return mFunctionCode;
    }

    public void setFunctionCode(byte functionCode) {
        mFunctionCode = functionCode;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }
}
