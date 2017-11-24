package com.locksdk.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.locksdk.LockAPI;
import com.locksdk.listener.ApplyPermissionListener;
import com.locksdk.baseble.ViseBle;

/**
 * Created by Sujiayong on 2017/11/17.
 * 申请定位权限工具
 */

public class ApplyLocationPermissionUtil {

    //由于在安卓版本为5.0以上，扫描需要定位权限
    public static void applyLocationPermission(final ApplyPermissionListener listener) {
        if (LockAPI.getInstance().getContext() == null) {
            Log.e("++++++>", "ViseBle.getInstance().getContext()");
            return;
        }
        Context context = LockAPI.getInstance().getContext();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!(context instanceof Activity)) {  Log.e("+++2+++>", "ViseBle.getInstance().getContext()");return;}
//            PermissionManager.instance().with((Activity) context).request(new OnPermissionCallback() {
//                @Override
//                public void onRequestAllow(String permissionName) {
//                    if (permissionName.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                        listener.onAllow();
//                    }
//                }
//
//                @Override
//                public void onRequestRefuse(String permissionName) {
//                    listener.onRefuse();
//                }
//
//                @Override
//                public void onRequestNoAsk(String permissionName) {
//                    listener.onNoAsk();
//                }
//            }, Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            listener.onAllow();
        }
    }
}
