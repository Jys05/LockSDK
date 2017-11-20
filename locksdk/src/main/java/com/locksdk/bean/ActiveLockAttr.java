package com.locksdk.bean;

/**
 * Created by Sujiayong on 2017/11/20.
 * 激活数据
 */

public class ActiveLockAttr {
    private String trTime;          //时间戳
    private String lockId;          //锁具ID
    private String dpKey;           //动态密码密钥
    private String dpCommKey;           //动态密码传输密钥
    private String dpCommKeyVer;            //动态密码传输密钥版本
    private String dpKeyVer;           //动态密码密钥版本
    private String dpKeyChkCode;            //动态密码密钥校验值
    private String dpCommChkCode;           //动态密码传输密钥校验值
    private String boxName;         //款箱名称

    public String getTrTime() {
        return trTime;
    }

    public void setTrTime(String trTime) {
        this.trTime = trTime;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }

    public String getDpKey() {
        return dpKey;
    }

    public void setDpKey(String dpKey) {
        this.dpKey = dpKey;
    }

    public String getDpCommKey() {
        return dpCommKey;
    }

    public void setDpCommKey(String dpCommKey) {
        this.dpCommKey = dpCommKey;
    }

    public String getDpCommKeyVer() {
        return dpCommKeyVer;
    }

    public void setDpCommKeyVer(String dpCommKeyVer) {
        this.dpCommKeyVer = dpCommKeyVer;
    }

    public String getDpKeyVer() {
        return dpKeyVer;
    }

    public void setDpKeyVer(String dpKeyVer) {
        this.dpKeyVer = dpKeyVer;
    }

    public String getDpKeyChkCode() {
        return dpKeyChkCode;
    }

    public void setDpKeyChkCode(String dpKeyChkCode) {
        this.dpKeyChkCode = dpKeyChkCode;
    }

    public String getDpCommChkCode() {
        return dpCommChkCode;
    }

    public void setDpCommChkCode(String dpCommChkCode) {
        this.dpCommChkCode = dpCommChkCode;
    }

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }
}
