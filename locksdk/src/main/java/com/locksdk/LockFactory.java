package com.locksdk;

import android.content.Context;

/**
 * Created by Sujiayong on 2017/11/14.
 */

public class LockFactory {
     private static final String TAG = "LockFactory";

    public static void getInstance(Context ctx) {
        LockAPI.getInstance().init(ctx);
    }

}
