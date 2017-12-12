package com.locksdk.bean;

/**
 * Created by Sujiayong on 2017/11/17.
 * 蓝牙通知Nofice回调
 */

public class NoficeCallbackData {
    private byte mRespondCode;          //掉包或数据有误的时候用到此“响应码”，做判断是哪个接口写入，将数据返回哪个接口
    private byte[] data;                //数据用包好响应码，如果数据正常的话，优先用data中的响应码，做接口判断
    private boolean isFinish;

    public NoficeCallbackData() {
    }

    public byte getRespondCode() {
        return mRespondCode;
    }

    public void setRespondCode(byte respondCode) {
        mRespondCode = respondCode;
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
