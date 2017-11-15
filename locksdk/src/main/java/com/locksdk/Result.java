package com.locksdk;

/**
 * Created by Sujiayong on 2017/11/14.
 * 返回结果Result对象
 *
 * <p>正确返回"0000",其他值为错误</p>
 * <p>返回结果为"0000"时，返回消息为"成功",其他返回错误描述</p>
 */

public class Result<T> {
    private String code;        //返回结果代码
    private String msg;         //返回消息
    private T data;        //返回值

    public Result() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
