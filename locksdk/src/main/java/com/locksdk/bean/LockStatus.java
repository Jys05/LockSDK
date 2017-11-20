package com.locksdk.bean;

/**
 * Created by Sujiayong on 2017/11/15.
 * 锁具状态
 */

public class LockStatus {

    private int lockStatus = 0;//锁状态
    private int closeTimoutAlarm = 0;//超时未关报警
    private int vibrateAlarm = 0;//震动报警
    private int lockedAlarm = 0;//锁定报警
    private int boxStatus = 0;//款箱状态
    private int switchError = 0;//锁开关错误
    private int alarmSwitchStatus = 1;//款箱报警提醒开关状态
    private int shelfStatus = 1;//上下架状态
    private int batteryLevel = 3;//电池电量

    public String getLockStatus() {
        if (lockStatus == 0) {
            return "关闭";
        } else {
            return "开启";
        }
    }

    public void setLockStatus(int lockStatus) {
        this.lockStatus = lockStatus;
    }

    public String getCloseTimoutAlarm() {
        if (closeTimoutAlarm == 0) {
            return "无报警";
        } else {
            return "有报警";
        }
    }

    public void setCloseTimoutAlarm(int closeTimoutAlarm) {
        this.closeTimoutAlarm = closeTimoutAlarm;
    }

    public String getVibrateAlarm() {
        if (vibrateAlarm == 0) {
            return "无报警";
        } else {
            return "有报警";
        }
    }

    public void setVibrateAlarm(int vibrateAlarm) {
        this.vibrateAlarm = vibrateAlarm;
    }

    public String getLockedAlarm() {
        if (lockedAlarm == 0) {
            return "无报警";
        } else {
            return "有报警";
        }
    }

    public void setLockedAlarm(int lockedAlarm) {
        this.lockedAlarm = lockedAlarm;
    }

    public String getBoxStatus() {
        if (boxStatus == 0) {
            return "关";
        } else {
            return "开";
        }
    }

    public void setBoxStatus(int boxStatus) {
        this.boxStatus = boxStatus;
    }

    public String getSwitchError() {
        if (switchError == 0) {
            return "无错误";
        } else {
            return "有错误";
        }
    }

    public void setSwitchError(int switchError) {
        this.switchError = switchError;
    }

    public String getAlarmSwitchStatus() {
        if (alarmSwitchStatus == 0) {
            return "关";
        } else {
            return "开";
        }

    }

    public void setAlarmSwitchStatus(int alarmSwitchStatus) {
        this.alarmSwitchStatus = alarmSwitchStatus;
    }

    public String getShelfStatus() {
        if (shelfStatus == 0) {
            return "未上架";
        } else {
            return "已上架";
        }
    }

    public void setShelfStatus(int shelfStatus) {
        this.shelfStatus = shelfStatus;
    }

    public String getBatteryLevel() {
        if (batteryLevel == 0) {
            return "最低";
        } else if (batteryLevel == 1) {
            return "中等";
        } else if (batteryLevel == 2) {
            return "中等";
        } else if (batteryLevel == 3) {
            return "最高";
        } else {
            return "最高";
        }
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
