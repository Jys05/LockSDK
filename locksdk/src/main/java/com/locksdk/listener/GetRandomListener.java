package com.locksdk.listener;

import com.locksdk.Result;
import com.locksdk.bean.RandomAttr;

/**
 * Created by Sujiayong on 2017/11/21.
 * 获取随机数监听
 */

public interface GetRandomListener {
    void getRandomCallback(Result<RandomAttr> randomAttrResult);
}
