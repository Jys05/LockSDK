package com.locksdk.bean;

/**
 * Created by Sujiayong on 2017/11/16.
 * 日志对象
 */

public class LockLog {
    private String optType;//操作类型
    private String optTime;//操作时间
    private String userId;//用户id

    public String getOptType() {
        return optType;
    }

    public void setOptType(String optType) {
        this.optType = optType;
    }

    public String getOptTime() {
        return optTime;
    }

    public void setOptTime(String optTime) {
        this.optTime = optTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
