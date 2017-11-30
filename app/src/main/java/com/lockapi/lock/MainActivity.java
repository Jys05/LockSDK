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

import com.library.base.util.recyclerview.BaseAdapterHelper;
import com.library.base.util.recyclerview.OnItemClickListener;
import com.library.base.util.recyclerview.QuickAdapter;
import com.locksdk.LockAPI;
import com.locksdk.LockFactory;
import com.locksdk.Result;
import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.ScannerListener;
import com.locksdk.baseble.ViseBle;
import com.locksdk.baseble.model.BluetoothLeDevice;

import java.util.List;

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
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    //获取款箱
    public void onScannerClick(View view) {
      mHandler2.sendEmptyMessage(0x00);
    }

    private Handler mHandler2 = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLockAPI = LockAPI.getInstance().init(MainActivity.this);
                    ScannerListener mScannerListener = new ScannerListener() {
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
//            Toast.makeText(MainActivity.this, "扫描出错", Toast.LENGTH_SHORT).show();
                            Log.i(TAG + "==扫描出错==>", msg);
                        }
                    };
                    mLockAPI.getBoxList(MainActivity.this, null, mScannerListener);
                }
            });
            return false;
        }
    });

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(View view, int i) {
            Toast.makeText(MainActivity.this, mQuickAdapter.getItem(i).getDevice().getName(), Toast.LENGTH_SHORT).show();
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
            mHandler.sendEmptyMessage(0x00);
        }

        @Override
        public void onSuccess(BluetoothLeDevice device, String uuid, String boxName) {
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
