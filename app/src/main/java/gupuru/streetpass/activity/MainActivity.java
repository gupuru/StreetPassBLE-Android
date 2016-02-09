package gupuru.streetpass.activity;

import android.Manifest;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import gupuru.streetpassble.ConnectDevice;
import gupuru.streetpassble.StreetPassBle;
import gupuru.streetpassble.parcelable.AdvertiseSuccess;
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.StreetPassSettings;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        StreetPassBle.OnStreetPassListener, ConnectDevice.OnConnectDeviceListener {

    private StreetPassBle streetPassBle;
    private ConnectDevice connectDevice;
    private TextView statusTextView;
    private TextView connectStatusTextView;
    private ArrayList<BleData> bleDataArrayList;
    private BleRecyclerAdapter bleRecyclerAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ツールバー
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(getResources().getString(R.string.app_name));
            toolbar.setTitleTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
        }

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
        statusTextView = (TextView) findViewById(R.id.status);
        connectStatusTextView = (TextView) findViewById(R.id.connect_status);

        streetPassBle = new StreetPassBle(MainActivity.this);
        streetPassBle.setOnStreetPassListener(this);
        connectDevice = new ConnectDevice(MainActivity.this);
        connectDevice.setOnConnectDeviceListener(this);
        connectDevice.start();

        //BLE対応端末か
        if (streetPassBle.isBle()) {
            if (streetPassBle.isAdvertise()) {
                //送信可能
                statusTextView.setText(getString(R.string.send_receive_status_message));
            } else {
                //受信のみ
                statusTextView.setText(getString(R.string.receive_status_message));
            }
        } else {
            //非対応
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
        bleRecyclerAdapter = new BleRecyclerAdapter(MainActivity.this, connectDevice, bleDataArrayList);
        //アダプターにセット
        recyclerView.setAdapter(bleRecyclerAdapter);
        //更新を通知
        bleRecyclerAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.contact:
                showAlertDialog(getString(R.string.contact_title), getString(R.string.contact_message));
                return true;
            case R.id.version:
                if (streetPassBle != null) {
                    showAlertDialog(getString(R.string.versioin_title), streetPassBle.getLibraryVersion());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * アラートダイヤログを表示
     * @param title
     * @param message
     */
    private void showAlertDialog(String title, String message){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.alert_positive_button), null)
                .show();
    }

    @Override
    public void onDataReceived(DeviceData deviceData) {
        if (deviceData != null) {
            statusTextView.setText(getString(R.string.receiving_status_message));

            final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN);
            final Date date = new Date(System.currentTimeMillis());

            String deviceName = getString(R.string.no_name);
            if (deviceData.getDeviceName() != null && !deviceData.getDeviceName().equals("")) {
                deviceName = deviceData.getDeviceName();
            }

            BleData bleData = new BleData(deviceName, deviceData.getDeviceAddress()
                    , deviceData.getServiceData(), df.format(date));
            bleDataArrayList.add(bleData);
            bleRecyclerAdapter.setbleDataArrayList(bleDataArrayList);
            bleRecyclerAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(bleDataArrayList.size() - 1);
        }
    }

    @Override
    public void onAdvertiseResult(AdvertiseSuccess advertiseSuccess) {
        statusTextView.setText(getString(R.string.sending_status_message));
    }

    @Override
    public void onError(Error error) {
        statusTextView.setText(error.getErrorMessage());
    }

    private boolean isC = false;
    @Override
    public void onConnectedResult(boolean isConnected) {
        isC = isConnected;
        if (isConnected) {
            connectStatusTextView.setText(getString(R.string.connected_device_message));
        } else {
            Intent intent = new Intent();
            intent.setAction("disconnect");
            sendBroadcast(intent);
        }
    }

    @Override
    public void onConnectedDeviceData(DeviceData deviceData) {
        if (!isC) {
            connectDevice.connectDevice(deviceData.getDeviceAddress());
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            startActivity(intent);
        }
    }

    @Override
    public void canConnect(boolean result) {
        bleRecyclerAdapter.setIsOpenServer(result);
        if (result){
            connectStatusTextView.setText(getString(R.string.open_server_status_message));
        } else {
            connectStatusTextView.setText(getString(R.string.close_server_status_message));
        }
    }

    @Override
    public void onConnectedError(Error error) {
        statusTextView.setText(error.getErrorMessage());
    }

      @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                statusTextView.setText(getString(R.string.starting_status_message));
                StreetPassSettings streetPassSettings = new StreetPassSettings();
                streetPassSettings.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
                streetPassSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
                streetPassSettings.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
                streetPassSettings.setAdvertiseConnectable(true);
                streetPassSettings.setAdvertiseIncludeDeviceName(true);
                streetPassSettings.setAdvertiseIncludeTxPowerLevel(true);
                streetPassSettings.setServiceUuid(Constants.SERVICE_UUID);
                streetPassSettings.setCharacteristicUuid(Constants.CHARACTERISTIC_UUID);
                streetPassSettings.setDefaultResponseData("first");
                streetPassSettings.setData(getString(R.string.test_message));
                streetPassBle.start(streetPassSettings, true);
                break;
            case R.id.stop:
                streetPassBle.stop();
                connectDevice.stop();
                bleRecyclerAdapter.clear();
                bleRecyclerAdapter.notifyDataSetChanged();
                statusTextView.setText(getString(R.string.stop_status_message));
                connectStatusTextView.setText(getString(R.string.close_server_status_message));
                break;
            default:
                break;
        }
    }

}
