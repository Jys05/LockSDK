package com.locksdk;

/**
 * Created by Sujiayong on 2017/11/14.
 */

public class Constant {

    public static String SERVICE_UUID = "8bdffca0-cf83-458a-89da-1c0714c2deb3";

//    public static String KX_STATE_UUID = "8bdffca1-cf83-458a-89da-1c0714c2deb3";       //功能：

    public static String NOTIFY_UUID = "8bdffca2-cf83-458a-89da-1c0714c2deb3";       //功能：款箱向手机发送数据

    public static String WRITE_UUID = "8bdffca3-cf83-458a-89da-1c0714c2deb3";       //功能：手机向款箱发送数据

    public static String READ_LOCK_ID_UUID = "00002a23-0000-1000-8000-00805f9b34fb";       //功能：锁具ID


    public static final String OPEN_LOCATION_PERSIMISSON = "请打开定位权限，保证款箱搜索正常";

    public static final class CODE {
        public static final String CODE_SUCCESS = "0000";           //操作成功：扫描开始、扫描到设备、连接成功

        public static final String CODE_TIME_OUT = "0001";          //超时：如扫描超时、连接超时

        public static final String CODE_CONNECT_FAIL = "0002";          //连接错误

        public static final String CODE_SCANNER_FAIL = "0003";          //扫描错误

        public static final String CODE_SCANNER_FAIL2 = "0004";          //由于在5.0API以上需要定位权限

        public static final String CODE_GET_LOCK_ID_FAIL = "0005";          //获取款箱锁具ID错误

        public static final String CODEACTIVE_FAIL = "0006";          //激活过程有误

        public static final String GET_RANDOM_FAIL = "0007";          //获取随机数有误

        public static final String OPEN_LOCK_FAIL = "0008";          //开锁有误
    }

    public static final class MSG {
        public static final String MSG_SCANNERING = "扫描进行中";

        public static final String MSG_SCANNERED = "扫描结果";

        public static final String MSG_SCANNER_FAIL = "扫描失败";

        public static final String MSG_LOCATION_PERSISION = "定位权限未开启";

        public static final String MSG_CONNECT_TIME_OUT = "连接超时";

        public static final String MSG_CONNECT_FAIL = "连接失败";

        public static final String MSG_CONNECT_FAIL2 = "时间过长未操作，款箱自动断开";

        public static final String MSG_DISCONNECT = "连接断开";

        public static final String MSG_CONNECT_DEVICE_NULL = "连接设备不存在";

        public static final String MSG_GET_LOCK_ID_FAIL = "获取款箱锁具ID错误";

        public static final String MSG_GET_LOCK_ID_SUCCESS = "获取款箱锁具ID成功";

        public static final String MSG_GET_RANDOM_SUCCESS = "获取随机数成功";

        public static final String MSG_WRITE_FAIL = "数据写入失败";

        public static final String MSG_ACTIVE_SUCCESS = "激活锁具成功";

        public static final String MSG_DPKEY_FAIL = "对秘钥处理错误";

        public static final String MSG_NOFICE_FAIL = "监听数据错误";

        public static final String MSG_ENCODE_FAIL = "数据加密有误";

        public static final String MSG_OPEN_LOCk_FAIL = "开锁时数据加密失败";

        public static final String MSG_OPEN_LOCk_FAIL2 = "开锁时用户名传入为空";


    }
}
