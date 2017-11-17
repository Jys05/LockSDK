package com.locksdk;

import android.app.Activity;
import android.content.Context;

/**
 * Created by Sujiayong on 2017/11/14.
 */

public class LockFactory {

    private static final String TAG = "LockFactory";
    private static LockFactory instance;
    private static boolean isFrist = true;

    public LockFactory() {
    }

    public static LockAPI getInstance(Context ctx) {
        if(instance == null){
            synchronized (LockFactory.class){
                if(instance==null){
                    instance = new LockFactory();
                }
            }
        }
        if(isFrist){
            isFrist = false;
            return LockAPI.getInstance().init(ctx);
        }else {
            return LockAPI.getInstance();
        }

    }

}
