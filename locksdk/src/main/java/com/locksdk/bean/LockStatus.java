package com.locksdk.bean;

import com.locksdk.util.LockStatusUtil;

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

    public void setLockStatus(char lockStatus) {
        this.lockStatus = Integer.valueOf(String.valueOf(lockStatus));
    }

    public void setCloseTimoutAlarm(char closeTimoutAlarm) {
        this.closeTimoutAlarm = Integer.valueOf(String.valueOf(closeTimoutAlarm));
    }

    public void setVibrateAlarm(char vibrateAlarm) {
        this.vibrateAlarm = Integer.valueOf(String.valueOf(vibrateAlarm));
    }

    public void setLockedAlarm(char lockedAlarm) {
        this.lockedAlarm = Integer.valueOf(String.valueOf(lockedAlarm));
    }

    public void setBoxStatus(char boxStatus) {
        this.boxStatus = Integer.valueOf(String.valueOf(boxStatus));
    }

    public void setSwitchError(char switchError) {
        this.switchError = Integer.valueOf(String.valueOf(switchError));
    }

    public void setAlarmSwitchStatus(char alarmSwitchStatus) {
        this.alarmSwitchStatus = Integer.valueOf(String.valueOf(alarmSwitchStatus));
    }

    public void setShelfStatus(char shelfStatus) {
        this.shelfStatus = Integer.valueOf(String.valueOf(shelfStatus));
    }

    public void setBatteryLevel(String batteryLevelStr) {
        this.batteryLevel = Integer.valueOf(LockStatusUtil.toD(batteryLevelStr, 2));
    }
}
