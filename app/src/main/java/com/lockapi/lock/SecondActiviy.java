package com.lockapi.lock;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.library.base.util.ToastUtil;
import com.locksdk.Constant;
import com.locksdk.LockAPI;
import com.locksdk.LockFactory;
import com.locksdk.Result;
import com.locksdk.bean.LockStatus;
import com.locksdk.bean.RandomAttr;
import com.locksdk.listener.ActiveLockListener;
import com.locksdk.listener.GetRandomListener;
import com.locksdk.listener.LockStatusListener;
import com.locksdk.listener.OpenLockListener;
import com.locksdk.listener.QueryLogsListener;
import com.locksdk.util.DateUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sujiayong on 2017/11/21.
 */

public class SecondActiviy extends AppCompatActivity {

    private LockAPI mLockAPI;
    private static final String TAG = "SecondActiviy";
    private String mBoxName;
    private String mBoxAddress;
    private TextView tv_Result;
    private String mMsg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initView();
        final Bundle bundle = getIntent().getExtras();
        Log.i(TAG, bundle.getString("boxName", "KX001"));
        Log.i(TAG, bundle.getString("boxMac", "   "));
        mBoxName = bundle.getString("boxName");
        mBoxAddress = bundle.getString("boxMac");
        mLockAPI = LockFactory.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        mLockAPI.closeConnection(Constant.SERVICE_UUID);
        super.onDestroy();
    }

    //关闭连接
    public void onCloseConnectClick(View view) {
        mLockAPI.closeConnection(Constant.SERVICE_UUID);
        finish();
    }

    //获取锁具ID
    public void onGetLockIdByBoxNameClick(View view) {
//        if (!TextUtils.isEmpty(mBoxName)) {
        Result<String> result = mLockAPI.getLockIdByBoxName(mBoxName);
        Toast.makeText(this, result.getData(), Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, mBoxAddress, Toast.LENGTH_SHORT).show();
//        }
    }

    //激活
    public void onActiviteLockClick(View view) {
        Map<String, String> param = new HashMap<>();
        param.put("trTime", DateUtil.format(DateUtil.yyyyMMddHHmmss_not, System.currentTimeMillis()));
        param.put("lockId", mBoxAddress.replace(":", ""));
        param.put("dpKey", "0000");
        param.put("dpCommKey", "0000");
        param.put("dpCommKeyVer", "0000");
        param.put("dpKeyVer", "0000");
        param.put("dpKeyChkCode", "0000");
        param.put("dpCommChkCode", "0000");
        param.put("boxName", "0000");
        mLockAPI.activeLock(param, new ActiveLockListener() {
            @Override
            public void activeLockCallback(Result<String> result) {
                mHandler.sendEmptyMessage(0x00);
                mMsg = "激活成功："
                        + "\n激活数据：" + result.getData();
            }
        });
    }

    //获取随机
    public void onGetRandomClick(View view) {
        mLockAPI.getRandom("KX001", new GetRandomListener() {
            @Override
            public void getRandomCallback(Result<RandomAttr> randomAttrResult) {
                mHandler.sendEmptyMessage(0x00);
                mMsg = "款箱名：" + randomAttrResult.getData().getBoxName()
                        + "\n随机数：" + randomAttrResult.getData().getRandom()
                        + "\n闭锁码：" + randomAttrResult.getData().getCloseCode()
                        + "\n动态密码传输密钥版本" + randomAttrResult.getData().getDpCommKeyVer()
                        + "\n动态密码密钥版本" + randomAttrResult.getData().getDpKeyVer();

            }
        });
    }

    //查询状态
    public void onQueryLockStatusClick(View view) {
        mLockAPI.registerLockStatusListener(new LockStatusListener() {
            @Override
            public void onChange(String boxName, String lockId, LockStatus newStatus) {
                mHandler.sendEmptyMessage(0x00);
                mMsg = "款箱名：" + boxName
                        + "\n锁具ID：" + lockId
                        + "\n锁具状态：" + " ";
            }
        }).queryLockStatus("sabd");
    }

    //查询日志
    public void onQueryLogsClick(View view) {
        Map<String, String> param = new HashMap<>();
        param.put("lockId", mBoxAddress);
        param.put("startSeq", "00");
        param.put("endSeq", "01");
        mLockAPI.queryLogs(param, new QueryLogsListener() {
            @Override
            public void queryLogsCallback(Result<String> result) {
                mHandler.sendEmptyMessage(0x00);
                mMsg = "查询日志成功："
                        + "\n日志数据：" + result.getData();
            }
        });
    }

    //开锁
    public void onOpenLockClick(View view) {
        Map<String, String> param = new HashMap<>();
        param.put("trTime", "trTime");
        param.put("boxName", "boxName");
        param.put("userId", "userId");
        param.put("dynamicPwd", "dynamicPwd");
        mLockAPI.openLock(null, new OpenLockListener() {
            @Override
            public void openLockCallback(Result<String> open) {
//TODO : 2017/11/23
            }
        });
    }

    private void initView() {
        tv_Result = (TextView) findViewById(R.id.tv_Result);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            Toast.makeText(SecondActiviy.this, "获取成功", Toast.LENGTH_SHORT).show();
            tv_Result.setText(mMsg + " ");
            return false;
        }
    });
}
