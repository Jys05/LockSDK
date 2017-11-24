package com.lockapi.lock;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.library.base.util.ToastUtil;
import com.locksdk.Constant;
import com.locksdk.LockAPI;
import com.locksdk.LockApiBleUtil;
import com.locksdk.LockFactory;
import com.locksdk.Result;
import com.locksdk.bean.LockStatus;
import com.locksdk.bean.RandomAttr;
import com.locksdk.listener.ActiveLockListener;
import com.locksdk.listener.GetLockIdListener;
import com.locksdk.listener.GetRandomListener;
import com.locksdk.listener.LockStatusListener;
import com.locksdk.listener.OpenLockListener;
import com.locksdk.listener.QueryLogsListener;
import com.locksdk.util.DateUtil;
import com.locksdk.util.WriteAndNoficeUtil;
import com.locksdk.baseble.utils.HexUtil;

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
        mLockAPI.getLockIdByBoxName(mBoxName, new GetLockIdListener() {
            @Override
            public void onGetLockIDListener(String lockId) {
                mMsg = "锁具ID：" + lockId;
                mHandler.sendEmptyMessage(0x00);
            }
        });

    }

    //激活
    public void onActiviteLockClick(View view) {
        Map<String, String> param = new HashMap<>();
        param.put("trTime", DateUtil.format(DateUtil.yyyyMMddHHmmss_not, System.currentTimeMillis()));
        param.put("lockId",LockApiBleUtil.getInstance().getLockIDStr());
        param.put("dpKey", "0000");
        param.put("dpCommKey", "0000");
        param.put("dpCommKeyVer", "0000");
        param.put("dpKeyVer", "0000");
        param.put("dpKeyChkCode", "0000");
        param.put("dpCommChkCode", "0000");
        if(TextUtils.isEmpty(mBoxName)){
            param.put("boxName", "KX001");
        }else {
            param.put("boxName", mBoxName);
        }
        mLockAPI.activeLock(param, new ActiveLockListener() {
            @Override
            public void activeLockCallback(Result<String> result) {
                mMsg = "激活成功："
                        + "\n激活数据：" + result.getData();
                mHandler.sendEmptyMessage(0x00);
            }
        });
    }

    //获取随机
    public void onGetRandomClick(View view) {
        mLockAPI.getRandom("KX001", new GetRandomListener() {
            @Override
            public void getRandomCallback(Result<RandomAttr> randomAttrResult) {
                mMsg = "款箱名：" + randomAttrResult.getData().getBoxName()
                        + "\n随机数：" + randomAttrResult.getData().getRandom()
                        + "\n闭锁码：" + randomAttrResult.getData().getCloseCode()
                        + "\n动态密码传输密钥版本" + randomAttrResult.getData().getDpCommKeyVer()
                        + "\n动态密码密钥版本" + randomAttrResult.getData().getDpKeyVer();
                mHandler.sendEmptyMessage(0x00);

            }
        });
    }

    //查询状态
    public void onQueryLockStatusClick(View view) {
        mLockAPI.registerLockStatusListener(new LockStatusListener() {
            @Override
            public void onChange(String boxName, String lockId, LockStatus newStatus) {
                mMsg = "款箱名：" + boxName
                        + "\n锁具ID：" + lockId
                        + "\n锁具状态：" + "81";
                mHandler.sendEmptyMessage(0x00);
            }
        }).queryLockStatus(mBoxAddress.replace(":", ""));
    }

    //查询日志
    public void onQueryLogsClick(View view) {
        Map<String, String> param = new HashMap<>();
        param.put("lockId",LockApiBleUtil.getInstance().getLockIDStr());
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
        param.put("trTime", DateUtil.format(DateUtil.yyyyMMddHHmmss_not , System.currentTimeMillis()));
        if(TextUtils.isEmpty(mBoxName)){
            param.put("boxName", "KX001");
        }else {
            param.put("boxName", mBoxName);
        }
        param.put("userId", "userId");
        param.put("dynamicPwd", "123456");
        mLockAPI.openLock(param, new OpenLockListener() {
            @Override
            public void openLockCallback(Result<String> open) {
//TODO : 2017/11/23
                if(open.getCode().equals("0001")){
                    mMsg = open.getMsg();
                    mHandler.sendEmptyMessage(0x00);
                }else {
                    mMsg = open.getData();
                    mHandler.sendEmptyMessage(0x00);
                }
            }
        });
    }


    private void initView() {
        tv_Result = (TextView) findViewById(R.id.tv_Result);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
//            Toast.makeText(SecondActiviy.this, "获取成功", Toast.LENGTH_SHORT).show();
            tv_Result.setText(mMsg + " ");
            return false;
        }
    });
}
