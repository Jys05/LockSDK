package com.locksdk;

/**
 * Created by Sujiayong on 2017/11/14.
 */

public class Constant {

    public static final String SERVICE_UUID = "8bdffca0-cf83-458a-89da-1c0714c2deb3";

    public static final String KX_STATE_UUID = "8bdffca1-cf83-458a-89da-1c0714c2deb3";       //功能：款箱状态

    public static final String READ_UUID = "8bdffca2-cf83-458a-89da-1c0714c2deb3";       //功能：款箱向手机发送数据

    public static final String WRITE_UUID = "8bdffca3-cf83-458a-89da-1c0714c2deb3";       //功能：手机向款箱发送数据

    public static final String READ_MAC_UUID = "8bdffca4-cf83-458a-89da-1c0714c2deb3";       //功能：获取手机的Mac码

    public static final String OPEN_LOCATION_PERSIMISSON = "请打开定位权限，保证款箱搜索正常";

    public static final class CODE {
        public static final String CODE_SUCCESS = "0000";           //操作成功：扫描开始、扫描到设备、连接成功

        public static final String CODE_TIME_OUT = "0001";          //超时：如扫描超时、连接超时

        public static final String CODE_CONNECT_FAIL = "0002";          //连接错误

        public static final String CODE_SCANNER_FAIL = "0003";          //扫描错误

        public static final String CODE_GET_LOCK_ID_FAIL = "0004";          //获取款箱锁具ID错误


    }

    public static final class MSG {
        public static final String MSG_SCANNERING = "扫描进行中";

        public static final String MSG_SCANNERED = "扫描结果";

        public static final String MSG_SCANNER_FAIL = "扫描失败";

        public static final String MSG_CONNECT_TIME_OUT = "连接超时";

        public static final String MSG_CONNECT_FAIL = "连接失败";

        public static final String MSG_CONNECT_DEVICE_NULL = "连接设备不存在";

        public static final String MSG_GET_LOCK_ID_FAIL = "获取款箱锁具ID错误";


        public static final String MSG_GET_LOCK_ID_SUCCESS = "获取款箱锁具ID成功";
    }
}
