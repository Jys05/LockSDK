package com.locksdk.bean;

/**
 * Created by Sujiayong on 2017/11/17.
 * 对设备写入数据时，回调的数据：功能码、数据
 */

public class WriteCallbackData {
    private byte mFunctionCode;
    private byte[] data;

    public WriteCallbackData() {
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
}
