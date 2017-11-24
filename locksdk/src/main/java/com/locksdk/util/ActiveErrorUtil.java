package com.locksdk.util;


import com.locksdk.baseble.utils.HexUtil;



/**
 * Created by Sujiayong on 2017/10/26.
 * 与款箱数据交互时，返回回来的错误数据
 */

public class ActiveErrorUtil {

    private static final String TAG = "ActiveErrorUtil";

    public static String getErrorMsg(byte[] dataByte) {
        if (dataByte.length == 3) {
            String errorMsg = null;        //板子返回来错误码，对应的错误信息
            String dataStr = HexUtil.encodeHexStr(dataByte);
            int dataInt = Integer.valueOf(dataStr);
            switch (dataInt) {
                case 0:
                    errorMsg = "成功";
                case 1:
                    errorMsg = "接受请求正在执行";
                    break;
                case 2:
                    errorMsg = "数据错误";
                    break;
                case 3:
                    errorMsg = "无权限";

                    break;
                case 4:
                    errorMsg = "不支持的功能码";
                    break;
                case 5:
                    errorMsg = "读取的数据不存在";
                    break;
            }
            return errorMsg;
        } else {
            return "返回数据异常";
        }
    }
}
