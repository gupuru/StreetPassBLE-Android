package gupuru.streetpass.activity;

import android.Manifest;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import gupuru.streetpass.R;
import gupuru.streetpass.adapter.BleRecyclerAdapter;
import gupuru.streetpass.bean.BleData;
import gupuru.streetpass.constans.Constants;
import gupuru.streetpass.utils.DividerItemDecoration;
import gupuru.streetpassble.DeviceConnection;
import gupuru.streetpassble.StreetPassBle;
import gupuru.streetpassble.parcelable.AdvertiseSuccessParcelable;
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.ErrorParcelable;
import gupuru.streetpassble.parcelable.StreetPassSettings;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        StreetPassBle.OnStreetPassListener, DeviceConnection.OnDeviceConnectionListener {

    private StreetPassBle streetPassBle;
    private DeviceConnection deviceConnection;
    private TextView statusTextView;
    private ArrayList<BleData> bleDataArrayList;
    private BleRecyclerAdapter bleRecyclerAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //マシュマロ判別
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //マシュマロ
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //未許可
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }

        Button startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(this);
        Button stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setOnClickListener(this);
        Button sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(this);
        Button readBtn = (Button) findViewById(R.id.read);
        readBtn.setOnClickListener(this);
        statusTextView = (TextView) findViewById(R.id.status);

        streetPassBle = new StreetPassBle(MainActivity.this);
        streetPassBle.setOnStreetPassListener(this);
        deviceConnection = new DeviceConnection(MainActivity.this);
        deviceConnection.setOnStreetPassListener(this);

        if (streetPassBle.isBle()) {
            if (streetPassBle.isAdvertise()) {
                statusTextView.setText("送受信可能");
            } else {
                statusTextView.setText("受信のみ可能");
            }
        } else {
            startBtn.setVisibility(View.GONE);
            startBtn.setVisibility(View.GONE);
        }

        //RecyclerView初期化
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        //区切り線をつける
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, null));
        bleDataArrayList = new ArrayList<>();
        //Adapter初期化
        bleRecyclerAdapter = new BleRecyclerAdapter(MainActivity.this, bleDataArrayList, deviceConnection);
        //アダプターにセット
        recyclerView.setAdapter(bleRecyclerAdapter);
        //更新を通知
        bleRecyclerAdapter.notifyDataSetChanged();

    }

    @Override
    public void streetPassResult(DeviceData deviceData) {
        statusTextView.setText("受信しています。");

        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN);
        final Date date = new Date(System.currentTimeMillis());

        BleData bleData = new BleData("ふええええ", deviceData.getDeviceAddress(), deviceData.getServiceData(), df.format(date));
        bleDataArrayList.add(bleData);
        bleRecyclerAdapter.setbleDataArrayList(bleDataArrayList);
        bleRecyclerAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(bleDataArrayList.size() - 1);
    }

    @Override
    public void advertiseSuccess(AdvertiseSuccessParcelable advertiseSuccessParcelable) {
        statusTextView.setText("送信しています。");
    }

    @Override
    public void error(ErrorParcelable errorParcelable) {
        statusTextView.setText(errorParcelable.getErrorMessage());
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.start:
                statusTextView.setText("開始します。");
                StreetPassSettings streetPassSettings = new StreetPassSettings();
                streetPassSettings.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
                streetPassSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
                streetPassSettings.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
                streetPassSettings.setAdvertiseConnectable(true);
                streetPassSettings.setAdvertiseIncludeDeviceName(false);
                streetPassSettings.setAdvertiseIncludeTxPowerLevel(false);
                streetPassSettings.setServiceUuid(Constants.SERVICE_UUID);
                streetPassSettings.setData("あいうえお_yp");
                deviceConnection.open();
                streetPassBle.start(streetPassSettings, true);
                break;
            case R.id.stop:
                streetPassBle.stop();
                if (deviceConnection != null) {
                    deviceConnection.close();
                }
                bleRecyclerAdapter.clear();
                bleRecyclerAdapter.notifyDataSetChanged();
                statusTextView.setText("停止しました。");
                break;
            case R.id.send:
                break;
            case R.id.read:
                break;
            default:
                break;
        }
    }

    @Override
    public void deviceConnectInfo(DeviceData deviceData, boolean isConnect) {
        Log.d("ここ", "接続した端末" + deviceData.getDeviceAddress());
    }

    @Override
    public void deviceConnectSendInfo(DeviceData deviceData) {

    }

    @Override
    public void deviceConnectSendReceiveData(String message) {

    }

    @Override
    public void deviceConnectOpenServer(boolean result) {
        Log.d("ここ", "server開いたか" + result);
    }

    @Override
    public void deviceConnectError(ErrorParcelable errorParcelable) {
        Log.d("ここ", "エラー" + errorParcelable.getErrorMessage());
    }

}
