package com.lockapi.lock;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.library.base.frame.FrameActivity;
import com.library.base.util.LoadingUtil;
import com.library.base.util.LogUtil;
import com.library.base.util.ToastUtil;
import com.library.base.util.recyclerview.BaseAdapterHelper;
import com.library.base.util.recyclerview.OnItemClickListener;
import com.library.base.util.recyclerview.QuickAdapter;
import com.locksdk.LockAPI;
import com.locksdk.LockApiBleUtil;
import com.locksdk.Result;
import com.locksdk.baseble.ViseBle;
import com.locksdk.baseble.model.BluetoothLeDevice;
import com.locksdk.baseble.utils.HexUtil;
import com.locksdk.bean.LockLog;
import com.locksdk.bean.LockStatus;
import com.locksdk.bean.RandomAttr;
import com.locksdk.listener.ActiveLockListener;
import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.GetLockIdListener;
import com.locksdk.listener.GetRandomListener;
import com.locksdk.listener.LockStatusListener;
import com.locksdk.listener.OpenLockListener;
import com.locksdk.listener.QueryLogsListener;
import com.locksdk.listener.ScannerListener;
import com.locksdk.util.DateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.InjectView;


public class MainActivity extends FrameActivity {

    @InjectView(R.id.recyclerview)
    RecyclerView mRecyclerview;
    @InjectView(R.id.et_inputBoxName)
    EditText mEtInputBoxName;
    @InjectView(R.id.et_queryLogStart)
    EditText mEtQueryLogStart;
    @InjectView(R.id.et_queryLogEnd)
    EditText mEtQueryLogEnd;
    @InjectView(R.id.tv_Result)
    TextView tv_Result;
    @InjectView(R.id.sv_connected)
    ScrollView mSvConnected;        //连接成功后界面
    @InjectView(R.id.ll_scanner)
    LinearLayout mLlScanner;        //扫描界面
    @InjectView(R.id.et_password)
    EditText mEtPassword;

    private QuickAdapter<BluetoothLeDevice> mQuickAdapter;
    private LockAPI mLockAPI;
    private static final String TAG = "MainActivity";
    private String mBoxName;
    private ScannerListener mScannerListener;

    @Override
    protected int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initComponent() {
        super.initComponent();
        mLockAPI = LockAPI.getInstance().init(MainActivity.this);
        initRecyclerView();
    }

    @Override
    protected void onDestroy() {
        LockAPI.getInstance().removeCallbacksAndMessages();
        super.onDestroy();
    }

    private void initRecyclerView() {
        mQuickAdapter = new QuickAdapter<BluetoothLeDevice>(this, R.layout.item_device) {
            @Override
            protected void convert(BaseAdapterHelper baseAdapterHelper, BluetoothLeDevice bluetoothLeDevice) {
                baseAdapterHelper.setText(R.id.tv_deviceName, bluetoothLeDevice.getDevice().getAddress()
                        + "--" + bluetoothLeDevice.getDevice().getName());
            }
        };
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerview.setAdapter(mQuickAdapter);
        mQuickAdapter.setOnItemClickListener(mOnItemClickListener);

    }


    //获取款箱
    public void onScannerClick(View view) {
        mQuickAdapter.clear();
        mScannerListener = new ScannerListener() {
            @Override
            public void onStartScanner(String code, String mag) {
                Toast.makeText(MainActivity.this, "扫描开始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBoxFoundScanning(Result<List<BluetoothLeDevice>> scannerDevideResult, Result<List<String>> boxNamesResult) {
                List<BluetoothLeDevice> devices = scannerDevideResult.getData();
                mQuickAdapter.replaceAll(devices);
            }


            @Override
            public void onScannerFail(String code, String msg) {
                Log.i(TAG + "==扫描出错==>", msg);
                Toast.makeText(MainActivity.this, "扫描出错", Toast.LENGTH_SHORT).show();

            }
        };
        mLockAPI.getBoxList(MainActivity.this, null, mScannerListener);
    }


    //关闭连接
    public void onCloseConnectClick(View view) {
        mLockAPI.closeConnection();
        mSvConnected.setVisibility(View.GONE);
        mLlScanner.setVisibility(View.VISIBLE);
        mQuickAdapter.clear();
        mLockAPI.getBoxList(MainActivity.this, null, mScannerListener);
    }

    //获取锁具ID
    public void onGetLockIdByBoxNameClick(View view) {
        LoadingUtil.showTipText("写入数据中");
        mLockAPI.getLockIdByBoxName(new GetLockIdListener() {
            @Override
            public void onGetLockIDListener(String lockId) {
                mMsg = "锁具ID：" + lockId;
                mHandler.sendEmptyMessage(0x02);
            }
        });

    }


    //激活/初始化
    public void onActiviteLockClick(View view) {
        Map<String, String> param = new HashMap<>();
        param.put("trTime", DateUtil.format(DateUtil.yyyyMMddHHmmss_not, System.currentTimeMillis()));
        param.put("lockId", LockApiBleUtil.getInstance().getLockIDStr());
        param.put("dpKey", "0000");
        param.put("dpCommKey", "0000");
        param.put("dpCommKeyVer", "0000");
        param.put("dpKeyVer", "0000");
        param.put("dpKeyChkCode", "0000");
        param.put("dpCommChkCode", "0000");
        final String boxName = mEtInputBoxName.getText().toString().trim();
        if (TextUtils.isEmpty(boxName)) {
            ToastUtil.show("请输入初始化款箱名字");
            return;
        } else {
            param.put("boxName", boxName);
        }
        LoadingUtil.showTipText("写入数据中");
        mLockAPI.activeLock(param, new ActiveLockListener() {
            @Override
            public void activeLockCallback(Result<String> result) {
                mBoxName = boxName;
                mMsg = "激活成功："
                        + "\n激活数据：" + result.getData();
                mHandler.sendEmptyMessage(0x02);
            }
        });
    }

    //获取随机
    public void onGetRandomClick(View view) {
        if (TextUtils.isEmpty(mBoxName)) {
            ToastUtil.show("款箱名为空，未初始化");
            return;
        }
        LoadingUtil.showTipText("写入数据中");
        mLockAPI.getRandom(mBoxName, new GetRandomListener() {
            @Override
            public void getRandomCallback(Result<RandomAttr> randomAttrResult) {
                mMsg = "款箱名：" + randomAttrResult.getData().getBoxName()
                        + "\n随机数：" + randomAttrResult.getData().getRandom()
                        + "\n闭锁码：" + randomAttrResult.getData().getCloseCode()
                        + "\n动态密码传输密钥版本" + randomAttrResult.getData().getDpCommKeyVer()
                        + "\n动态密码密钥版本" + randomAttrResult.getData().getDpKeyVer();
                mHandler.sendEmptyMessage(0x02);

            }
        });
    }

    //查询状态
    public void onQueryLockStatusClick(View view) {
        if (TextUtils.isEmpty(mBoxName)) {
            ToastUtil.show("款箱名为空，未初始化");
            return;
        }
        LoadingUtil.showTipText("写入数据中");
        mLockAPI.registerLockStatusListener(new LockStatusListener() {
            @Override
            public void onChange(String boxName, String lockId, LockStatus newStatus) {
                mMsg = "款箱名：" + boxName
                        + "\n锁具ID：" + lockId
                        + "\n锁状态：" + newStatus.getLockStatus()
                        + "\n超时未关报警：" + newStatus.getCloseTimoutAlarm()
                        + "\n震动报警：" + newStatus.getVibrateAlarm()
                        + "\n锁定报警：" + newStatus.getLockedAlarm()
                        + "\n款箱状态：" + newStatus.getBoxStatus()
                        + "\n锁开关错误：" + newStatus.getSwitchError()
                        + "\n款箱报警提醒开关状态：" + newStatus.getAlarmSwitchStatus()
                        + "\n上下架状态：" + newStatus.getShelfStatus()
                        + "\n电池电量：" + newStatus.getBatteryLevel()
                ;
                mHandler.sendEmptyMessage(0x02);
            }

        }).queryLockStatus(LockApiBleUtil.getInstance().getLockIDStr());
    }

    //查询日志
    public void onQueryLogsClick(View view) {
        if (TextUtils.isEmpty(mBoxName)) {
            ToastUtil.show("款箱名为空，未初始化");
            return;
        }
        String strStart = mEtQueryLogStart.getText().toString().trim();
        String strEnd = mEtQueryLogEnd.getText().toString().trim();
        if (!checkNum(strStart)) return;
        if (!checkNum(strEnd)) return;
        LoadingUtil.showTipText("写入数据中");
        Map<String, String> param = new HashMap<>();
        param.put("lockId", LockApiBleUtil.getInstance().getLockIDStr());
        param.put("startSeq", strStart);
        param.put("endSeq", strEnd);
        Log.e("=====>" , strStart+"=-=="+strEnd);
        mLockAPI.queryLogs(param, new QueryLogsListener() {
            @Override
            public void queryLogsCallback(Result<List<LockLog>> result) {
                List<LockLog> lockLogs = result.getData();
                if (lockLogs == null) {
                    mMsg = "获取日志失败";
                } else {
                    String log = "\n日志条数：" + lockLogs.size() + "\n";
                    for (int i = 0; i < lockLogs.size(); i++) {
                        log = log + "操作类型：" + lockLogs.get(i).getOptType() + "\n"
                                + "操作时间：" + lockLogs.get(i).getOptTime() + "\n"
                                + "操作用户ID：" + lockLogs.get(i).getUserId() + "\n";
                    }
                    mMsg = "查询日志成功："
                            + "\n日志数据：" + log;
                }
                mHandler.sendEmptyMessage(0x02);
            }
        });
    }

    //开锁
    public void onOpenLockClick(View view) {
        if (TextUtils.isEmpty(mBoxName)) {
            ToastUtil.show("款箱名为空，未初始化");
            return;
        }
        String strPassword = mEtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(strPassword)) {
            ToastUtil.show("请输入密码");
            return;
        }
        LoadingUtil.showTipText("写入数据中");
        Map<String, String> param = new HashMap<>();
        param.put("trTime", DateUtil.format(DateUtil.yyyyMMddHHmmss_not, System.currentTimeMillis()));
        param.put("boxName", mBoxName);
        param.put("userId", "userId");
        param.put("dynamicPwd", strPassword);
        mLockAPI.openLock(param, new OpenLockListener() {
            @Override
            public void openLockCallback(Result<String> open) {
//TODO : 2017/11/23
                if (open.getCode().equals("0000")) {
                    mMsg = "开锁成功：" + open.getData() +
                            "开锁信息：" + open.getMsg();
                    mHandler.sendEmptyMessage(0x02);
                } else {
                    mMsg = "开锁失败：" + open.getData() +
                            "开锁信息：" + open.getMsg();
                    mHandler.sendEmptyMessage(0x02);
                }
            }
        });
    }


    private boolean checkNum(String num) {
        String rg = "^[1-9]\\d*|0";
        Pattern pattern = Pattern.compile(rg);
        Matcher matcher = pattern.matcher(num);
        return matcher.matches();
    }


    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(View view, int i) {
//            Toast.makeText(MainActivity.this, mQuickAdapter.getItem(i).getDevice().getName(), Toast.LENGTH_SHORT).show();
            mBoxName = mQuickAdapter.getItem(i).getDevice().getName();
            BluetoothLeDevice bluetoothLeDevice = mQuickAdapter.getItem(i);
            if (!ViseBle.getInstance().isConnect(bluetoothLeDevice)) {
                Log.e("======>", "没有连接，装备连接" + bluetoothLeDevice.getName());
                mLockAPI.openConnection(bluetoothLeDevice, 10000, mConnectListener);
            } else {
                Log.e("======>", "已连接，准备断开连接");
                mLockAPI.closeConnection();
            }
        }

        @Override
        public void onItemLongClick(View view, int i) {

        }
    };

    private ConnectListener mConnectListener = new ConnectListener() {
        @Override
        public void onWaiting(String uuid) {
            mMsg = "开始连接";
            LoadingUtil.showTipText("开始连接");
            mHandler.sendEmptyMessage(0x00);
        }

        @Override
        public void onSuccess(BluetoothLeDevice device, String uuid, String boxName) {
            mMsg = "连接成功";
            LoadingUtil.hidden();
            mHandler.sendEmptyMessage(0x00);
            Log.i(TAG, "连接成功");
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLlScanner.setVisibility(View.GONE);
                    mSvConnected.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onFail(String uuid, String msg) {
            mMsg = msg;
            LoadingUtil.hidden();
//            mHandler.sendEmptyMessage(0x00);
            mHandler.sendEmptyMessage(0x03);
            Log.i(TAG, "连接错误:" + msg);
        }

        @Override
        public void onTimeout(String uuid, long timeout) {
            mMsg = "连接超时";
            LoadingUtil.hidden();
            mHandler.sendEmptyMessage(0x00);
            Log.i(TAG, "连接超时");
        }

        @Override
        public void onClose(String uuid, String boxName) {
            mMsg = "断开成功";
            mLockAPI.closeConnection();
            mHandler.sendEmptyMessage(0x00);
            Log.i(TAG, "onClose");
        }
    };

    private String mMsg;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == 0x00) {
                Toast.makeText(MainActivity.this, mMsg + "", Toast.LENGTH_SHORT).show();
            } else if (message.what == 0x03) {
                Toast.makeText(MainActivity.this, mMsg + "，重新扫描 ", Toast.LENGTH_SHORT).show();
                mSvConnected.setVisibility(View.GONE);
                mLlScanner.setVisibility(View.VISIBLE);
                mQuickAdapter.clear();
                mLockAPI.getBoxList(MainActivity.this, null, mScannerListener);
            } else {
                LoadingUtil.hidden();
                tv_Result.setText(mMsg + " ");
            }
            return false;
        }
    });

    @Override
    public void onBackPressed() {
        if (mSvConnected.getVisibility() == View.VISIBLE) {
            mSvConnected.setVisibility(View.GONE);
            mLlScanner.setVisibility(View.VISIBLE);
            mLockAPI.closeConnection();
            mQuickAdapter.clear();
            mLockAPI.getBoxList(MainActivity.this, null, mScannerListener);
        } else {
            super.onBackPressed();
        }
    }
}
