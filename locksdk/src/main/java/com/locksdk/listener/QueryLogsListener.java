package com.locksdk.listener;

import com.locksdk.Result;
import com.locksdk.bean.LockLog;

import java.util.List;

/**
 * Created by Sujiayong on 2017/11/21.
 * 查询日志监听
 */

public interface QueryLogsListener  {

    void queryLogsCallback(Result<List<LockLog>> result);
    void queryLogsCallback(Result<String> result);
}
