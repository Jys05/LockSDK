package com.locksdk.bean;

/**
 * Created by Sujiayong on 2017/11/15.
 * 获取随机数：返回的数据
 * P：如果有部分数据为空，则可能激活流程有误
 */

public class RandomAttr {
    private String boxName;//款箱名称
    private String random;//随机数
    private String closeCode;//闭锁码
    private String dpCommKeyVer;//动态密码传输密钥版本
    private String dpKeyVer;//动态密码密钥版本

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public String getCloseCode() {
        return closeCode;
    }

    public void setCloseCode(String closeCode) {
        this.closeCode = closeCode;
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
}
