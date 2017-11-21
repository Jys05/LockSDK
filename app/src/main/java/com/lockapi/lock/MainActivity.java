package com.lockapi.lock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.library.base.util.recyclerview.BaseAdapterHelper;
import com.library.base.util.recyclerview.OnItemClickListener;
import com.library.base.util.recyclerview.QuickAdapter;
import com.locksdk.ActiveLockUtil;
import com.locksdk.DealDataUtil;
import com.locksdk.LockAPI;
import com.locksdk.LockFactory;
import com.locksdk.Result;
import com.locksdk.listener.ConnectListener;
import com.locksdk.listener.ScannerListener;
import com.locksdk.util.LockSDKHexUtil;
import com.vise.baseble.ViseBle;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.utils.HexUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.recyclerview)
    RecyclerView mRecyclerview;

    private QuickAdapter<BluetoothLeDevice> mQuickAdapter;
    private LockAPI mLockAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        byte[] b = new byte[1];
//        b[0] = (byte) (0x80 + 1);
        b[0] = (byte) 222;
        Log.i("=====>", (0x80 + 1) + "——" + HexUtil.encodeHexStr(b));
//        String test = "12345678900987654321ABCDEFFEDCBA0123456789012345678901234567";
//        Log.i("======>1", LockSDKHexUtil.hexStringToByte(test, true).length + "");
//        List<byte[]> data = DealDataUtil.dealData(LockSDKHexUtil.hexStringToByte(test, true));
//        for (int i = 0; i < data.size(); i++) {
//            Log.i(i + "====size==>", data.get(i).length
//                    + "：" + HexUtil.encodeHexStr(data.get(i), false));
//        }
        Map<String, String> param = new HashMap<>();
        param.put("trTime", "A1A2A3A4A5A6A7A8A9A0B1B2B3B4");
        param.put("lockId", "A1A2A3A4A5A6A7A8A9A0B1B2B3B4B3B4B5B6B7B8B9B0C1C2C3C4");
        param.put("dpKey", "A1A2A3A4A5A6A7A8A9A0B1B2B3B4B5B6");
        param.put("dpCommKey", "A1A2A3A4A5A6A7A8A9A0B1B2B3B4B5B6");
        param.put("dpCommKeyVer", "A1A2A3A4A5A6A7A8A9A0B1B2B3B4B3B4B5B6B7B8B9B0C1C2C3C4C5C6C7C8C9C0D1D2D3D4D5D6");
        param.put("dpKeyVer", "A1A2A3A4A5A6A7A8A9A0B1B2B3B4B3B4B5B6B7B8B9B0C1C2C3C4C5C6C7C8C9C0D1D2D3D4D5D6");
        param.put("dpKeyChkCode", "A1A2A3A4A5A6A7A8A9A0B1B2B3B4B3B4B5B6B7B8B9B0C1C2C3C4C5C6C7C8C9C0D1D2D3D4");
        param.put("dpCommChkCode", "A1A2A3A4A5A6A7A8A9A0B1B2B3B4B3B4B5B6B7B8B9B0C1C2C3C4C5C6C7C8C9C0D1D2D3D4");
        param.put("boxName", "A1A2");
        ActiveLockUtil.activeLock(param);
        mQuickAdapter = new QuickAdapter<BluetoothLeDevice>(this, R.layout.item_device) {
            @Override
            protected void convert(BaseAdapterHelper baseAdapterHelper, BluetoothLeDevice bluetoothLeDevice) {
                baseAdapterHelper.setText(R.id.tv_deviceName, bluetoothLeDevice.getName());
            }
        };
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerview.setAdapter(mQuickAdapter);
        mQuickAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int i) {
                Log.e("======>", ((TextView) view).getText().toString());
                Toast.makeText(MainActivity.this, mLockAPI.getLockIdByBoxName(((TextView) view).getText().toString()).getData(), Toast.LENGTH_SHORT).show();
                BluetoothLeDevice bluetoothLeDevice = mQuickAdapter.getItem(i);
                if (!ViseBle.getInstance().isConnect(bluetoothLeDevice)) {
                    Log.e("======>", "没有连接，装备连接");
                    mLockAPI.openConnection(bluetoothLeDevice, 5000, mConnectListener);
                } else {
                    Log.e("======>", "已连接，准备断开连接");
                    mLockAPI.closeConnection(null);
                }
            }

            @Override
            public void onItemLongClick(View view, int i) {

            }
        });
        mLockAPI = LockFactory.getInstance(this);
        scannerDevice();
    }

    //获取款箱列表
    private void scannerDevice() {
        mLockAPI.getBoxList(new ScannerListener() {
            @Override
            public void onStartScanner(String code, String mag) {
                Log.e("======>", "扫描开始");
            }

            @Override
            public void onBoxFoundScanning(Result<List<BluetoothLeDevice>> boxs_Name) {
                List<BluetoothLeDevice> deviceLists = boxs_Name.getData();
                mQuickAdapter.replaceAll(deviceLists);
            }

            @Override
            public void onScannerFail(String code, String mag) {

            }
        });
    }


    private ConnectListener mConnectListener = new ConnectListener() {
        @Override
        public void onWaiting(String uuid) {
            Log.e("======>", "连接开始");
        }

        @Override
        public void onSuccess(BluetoothLeDevice device, String boxName) {
            Log.e("======>", "连接成功");
        }

        @Override
        public void onFail(String uuid, String msg) {

        }

        @Override
        public void onTimeout(String uuid, long timeout) {
            Log.e("======>", "onTimeout");
        }

        @Override
        public void onClose(String uuid, String boxName) {
            Log.e("======>", "断开连接");
        }
    };
}
