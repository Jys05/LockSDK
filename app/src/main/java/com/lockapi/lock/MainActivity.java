package com.lockapi.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.library.base.util.DateUtil;
import com.library.base.util.recyclerview.BaseAdapterHelper;
import com.library.base.util.recyclerview.OnItemClickListener;
import com.library.base.util.recyclerview.QuickAdapter;
import com.locksdk.Constant;
import com.locksdk.DealDataUtil;
import com.locksdk.LockAPI;
import com.locksdk.LockFactory;
import com.locksdk.Result;
import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.ScannerListener;
import com.locksdk.util.ASCIICodeUtil;
import com.locksdk.util.BCDCodeUtil;
import com.locksdk.util.DealtByteUtil;
import com.locksdk.util.LockSDKHexUtil;
import com.locksdk.util.Util;
import com.vise.baseble.ViseBle;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.HexUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.recyclerview)
    RecyclerView mRecyclerview;

    private QuickAdapter<BluetoothLeDevice> mQuickAdapter;
    private LockAPI mLockAPI;
    private static final String TAG = "MainActivity";
    private String mBoxName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
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
        mLockAPI = LockFactory.getInstance(this);
    }

    public void onScannerClick(View view) {
        mLockAPI.getBoxList(mScannerListener);
    }

    private ScannerListener mScannerListener = new ScannerListener() {
        @Override
        public void onStartScanner(String code, String mag) {
            Toast.makeText(MainActivity.this, "扫描开始", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBoxFoundScanning(Result<List<BluetoothLeDevice>> boxs_Name) {
            List<BluetoothLeDevice> devices = boxs_Name.getData();
            mQuickAdapter.replaceAll(devices);
        }

        @Override
        public void onScannerFail(String code, String msg) {
//            Toast.makeText(MainActivity.this, "扫描出错", Toast.LENGTH_SHORT).show();
            Log.i(TAG + "==扫描出错==>", msg);
        }
    };

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(View view, int i) {
            Toast.makeText(MainActivity.this, mQuickAdapter.getItem(i).getDevice().getName(), Toast.LENGTH_SHORT).show();
            mBoxName = mQuickAdapter.getItem(i).getDevice().getName();
            BluetoothLeDevice bluetoothLeDevice = mQuickAdapter.getItem(i);
            if (!ViseBle.getInstance().isConnect(bluetoothLeDevice)) {
                Log.e("======>", "没有连接，装备连接" + bluetoothLeDevice.getName());
                mLockAPI.openConnection(bluetoothLeDevice, 5000, mConnectListener);
            } else {
                Log.e("======>", "已连接，准备断开连接");
                mLockAPI.closeConnection(null);
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
            mHandler.sendEmptyMessage(0x00);
        }

        @Override
        public void onSuccess(BluetoothLeDevice device, String boxName) {
            mMsg = "连接成功";
            mHandler.sendEmptyMessage(0x00);
            Log.i(TAG, "连接成功");
            Intent intent = new Intent(MainActivity.this, SecondActiviy.class);
            Bundle bundle = new Bundle();
            bundle.putString("boxName", mBoxName);
            bundle.putString("boxMac", device.getDevice().getAddress());
            intent.putExtras(bundle);
            startActivity(intent);
        }

        @Override
        public void onFail(String uuid, String msg) {
            mMsg = "连接错误";
            mHandler.sendEmptyMessage(0x00);
            Log.i(TAG, "连接错误:" + msg);
        }

        @Override
        public void onTimeout(String uuid, long timeout) {
            mMsg = "连接超时";
            mHandler.sendEmptyMessage(0x00);
            Log.i(TAG, "连接超时");
        }

        @Override
        public void onClose(String uuid, String boxName) {
//            Toast.makeText(MainActivity.this, "断开成功", Toast.LENGTH_SHORT).show();
            mMsg = "断开成功";
            mHandler.sendEmptyMessage(0x00);
            Log.i(TAG, "onClose");
        }
    };

    private String mMsg;
    private Message mMessage = new Message();
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            Toast.makeText(MainActivity.this, mMsg + "", Toast.LENGTH_SHORT).show();
            return false;
        }
    });
}
