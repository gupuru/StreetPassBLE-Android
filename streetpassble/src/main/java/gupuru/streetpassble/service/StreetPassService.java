package gupuru.streetpassble.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gupuru.streetpassble.callback.AdvertiseBle;
import gupuru.streetpassble.callback.BLEServer;
import gupuru.streetpassble.callback.ScanBle;
import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.StreetPassSettings;

public class StreetPassService extends Service {

    private Context context;
    private ScanBle scanBle;
    private AdvertiseBle advertiseBle;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private ConnectDeviceReceiver connectDeviceReceiver = null;
    private IntentFilter connectDeviceFilter = null;
    private SendDataToDeviceReceiver sendDataToDeviceReceiver = null;
    private IntentFilter sendDataToDeviceFilter = null;
    private StreetPassSettings streetPassSettings;

    public StreetPassService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        connectDeviceReceiver = new ConnectDeviceReceiver();
        connectDeviceFilter = new IntentFilter(
                Constants.ACTION_CONNECT_DEVICE);
        sendDataToDeviceReceiver = new SendDataToDeviceReceiver();
        sendDataToDeviceFilter = new IntentFilter(
                Constants.ACTION_SEND_DATA_TO_DEVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            //StreetPassSettings取得
            streetPassSettings = intent.getParcelableExtra(Constants.STREET_PASS_SETTINGS);

            registerReceiver(connectDeviceReceiver, connectDeviceFilter);
            registerReceiver(sendDataToDeviceReceiver, sendDataToDeviceFilter);

            //BLEの送信に対応しているか
            if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                //対応->送受信
                scan();
                advertising();
            } else {
                //非対応->受信のみ
                scan();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (scanBle != null && bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanBle);
            bluetoothLeScanner = null;
        }
        if (bluetoothLeAdvertiser != null && advertiseBle != null) {
            bluetoothLeAdvertiser.stopAdvertising(advertiseBle);
            bluetoothLeAdvertiser = null;
        }
        unregisterReceiver(connectDeviceReceiver);
        unregisterReceiver(sendDataToDeviceReceiver);
        streetPassSettings = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void scan() {
        if (streetPassSettings.getUuid() != null && !streetPassSettings.getUuid().equals("")) {
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(UUID.fromString(streetPassSettings.getUuid())))
                    .build();
            filters.add(filter);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(streetPassSettings.getScanMode())
                    .build();

            scanBle = new ScanBle(context);

            bluetoothLeScanner.startScan(filters, settings, scanBle);
        }
    }

    private void advertising() {
        if (streetPassSettings.getUuid() != null && !streetPassSettings.getUuid().equals("")) {
            // 設定
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(streetPassSettings.getAdvertiseMode())
                    .setTxPowerLevel(streetPassSettings.getTxPowerLevel())
                    .setTimeout(streetPassSettings.getTimeOut())
                    .setConnectable(streetPassSettings.isAdvertiseConnectable())
                    .build();

            String serviceData = streetPassSettings.getData();
            if (serviceData == null) {
                serviceData = "";
            } else {
                if (serviceData.getBytes().length > 20) {
                    serviceData = serviceData.substring(0, 6);
                }
            }

            // アドバタイジングデータ
            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(streetPassSettings.getUuid()));
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addServiceUuid(pUuid)
                    .addServiceData(pUuid, serviceData.getBytes())
                    .setIncludeDeviceName(streetPassSettings.isAdvertiseIncludeDeviceName())
                    .setIncludeTxPowerLevel(streetPassSettings.isAdvertiseIncludeTxPowerLevel())
                    .build();

            advertiseBle = new AdvertiseBle(context);

            bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseBle);
        }
    }

    private BLEServer bleServer;

    private class ConnectDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(intent.getStringExtra(Constants.DEVICE_ADDRESS))) {
                String deviceAddress = intent.getStringExtra(Constants.DEVICE_ADDRESS);
                String characteristicUuid = intent.getStringExtra(Constants.CHARACTERISTIC_UUID);
                if (deviceAddress != null && !deviceAddress.equals("")
                        && characteristicUuid != null && !characteristicUuid.equals("")) {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    bleServer = new BLEServer(context, bluetoothGatt);
                    bluetoothGatt = device.connectGatt(getApplicationContext(), false, bleServer);
                    bluetoothGatt.connect();
                }
            }
        }
    }

    private class SendDataToDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(intent.getStringExtra(Constants.DATA))) {
                String data = intent.getStringExtra(Constants.DATA);
                bleServer.sendData(data);
            }
        }
    }

}
