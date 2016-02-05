package gupuru.streetpassble.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gupuru.streetpassble.callback.AdvertiseBle;
import gupuru.streetpassble.callback.ScanBle;
import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.StreetPassSettings;
import gupuru.streetpassble.reciver.StreetPassServiceReceiver;
import gupuru.streetpassble.server.BLEGattServer;
import gupuru.streetpassble.server.BLEServer;
import gupuru.streetpassble.util.StreetPassServiceUtil;

public class StreetPassService extends Service implements BLEGattServer.OnBLEGattServerListener
        , StreetPassServiceReceiver.OnStreetPassServiceReceiverListener, BLEServer.OnBLEServerListener {

    private Context context;
    private ScanBle scanBle;
    private AdvertiseBle advertiseBle;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothAdapter bluetoothAdapter;
    private StreetPassSettings streetPassSettings;
    private StreetPassServiceReceiver streetPassServiceReceiver;

    private BluetoothGattServer gattServer;
    private BLEServer bleServer;
    private BLEGattServer bleGattServer;
    private StreetPassServiceUtil streetPassServiceUtil;
    private BluetoothGatt bluetoothGatt;

    public StreetPassService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        streetPassServiceUtil = new StreetPassServiceUtil();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        streetPassServiceReceiver = new StreetPassServiceReceiver();
        streetPassServiceReceiver.setOnStreetPassServiceReceiverListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_CONNECT_DEVICE);
        intentFilter.addAction(Constants.ACTION_SEND_DATA_TO_DEVICE);
        intentFilter.addAction(Constants.ACTION_OPEN_GATT);
        intentFilter.addAction(Constants.ACTION_CLOSE_GATT);
        intentFilter.addAction(Constants.ACTION_START_STOP_SCAN);
        intentFilter.addAction(Constants.ACTION_DISCONNECT_DEVICE);
        registerReceiver(streetPassServiceReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            //StreetPassSettings取得
            streetPassSettings = intent.getParcelableExtra(Constants.STREET_PASS_SETTINGS);
            if (intent.getBooleanExtra(Constants.CAN_CONNECT, false)) {
                bleServer = new BLEServer(streetPassSettings.getServiceUuid()
                        , streetPassSettings.getCharacteristicUuid());
                bleServer.setOnBLEServerListener(this);
                openGattServer();
            }
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
        //scan停止
        stopScan();
        if (bluetoothLeAdvertiser != null && advertiseBle != null) {
            bluetoothLeAdvertiser.stopAdvertising(advertiseBle);
            bluetoothLeAdvertiser = null;
        }
        try {
            unregisterReceiver(streetPassServiceReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        closeGattServer();
        streetPassSettings = null;
        streetPassServiceUtil = null;
        bleServer = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void closeGattServer() {
        if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
        }
        bleGattServer = null;
    }

    private void openGattServer() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleGattServer = new BLEGattServer();
        bleGattServer.setOnBLEGattServerListener(this);
        gattServer = manager.openGattServer(context, bleGattServer);
        bleGattServer.setBluetoothGattServer(gattServer);
        //Serviceを登録
        BluetoothGattService service = new BluetoothGattService(
                UUID.fromString(streetPassSettings.getServiceUuid()),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic mCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(streetPassSettings.getCharacteristicUuid()),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY |
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(mCharacteristic);
        gattServer.addService(service);
    }

    /**
     * BLE scan開始
     */
    private void scan() {
        if (streetPassSettings.getServiceUuid() != null && !streetPassSettings.getServiceUuid().equals("")) {
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(UUID.fromString(streetPassSettings.getServiceUuid())))
                    .build();
            filters.add(filter);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(streetPassSettings.getScanMode())
                    .build();

            scanBle = new ScanBle(context);

            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }

            bluetoothLeScanner.startScan(filters, settings, scanBle);
        }
    }

    /**
     * BLE scan停止
     */
    private void stopScan(){
        if (scanBle != null && bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanBle);
            bluetoothLeScanner = null;
            scanBle = null;
        }
    }

    private void advertising() {
        if (streetPassSettings.getServiceUuid() != null && !streetPassSettings.getServiceUuid().equals("")) {
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
            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(streetPassSettings.getServiceUuid()));
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

    //region StreetPassServiceReceiver callback

    @Override
    public void onConnectDeviceData(String deviceAddress) {
        if (deviceAddress != null && !deviceAddress.equals("")) {
            if (bleServer != null) {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                bluetoothGatt = device.connectGatt(context, false, bleServer);
                bluetoothGatt.connect();
            }
        }
    }

    @Override
    public void onSendData(String data) {
        if (bleServer != null) {
            bleServer.writeData(data);
        }
    }

    @Override
    public void onClose() {
        closeGattServer();
    }

    @Override
    public void onIsScanStart(boolean flg) {
        if (flg) {
            //scan開始
            if(scanBle == null && bluetoothLeScanner == null) {
                scan();
            }
        } else {
            //scan停止
            stopScan();
        }
    }

    @Override
    public void onDisconnectDevice() {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    //endregion

    //region BLEGattServer callback

    @Override
    public void onServiceAdded(boolean result) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_GATT_SERVICE_ADDED);
        intent.putExtra(Constants.SERVICE_ADDED, result);
        context.sendBroadcast(intent);
    }

    @Override
    public void onCharacteristicWriteRequest(String message) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_GATT_SERVER_WRITE_REQUEST);
        intent.putExtra(Constants.WRITE_REQUEST, message);
        context.sendBroadcast(intent);
    }

    @Override
    public void onConnectionStateChange(boolean isConnect, BluetoothDevice device) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_GATT_SERVER_STATE_CHANGE);
        intent.putExtra(Constants.CONNECTION_DATA, streetPassServiceUtil.getDeviceData(device));
        intent.putExtra(Constants.IS_CONNECTION, isConnect);
        context.sendBroadcast(intent);
    }

    //endregion

    //region BLEServer callback

    @Override
    public void onBLEServerRead(String data) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_BLE_SERVER_READ);
        intent.putExtra(Constants.BLE_SERVER_READ, data);
        context.sendBroadcast(intent);
    }

    @Override
    public void onBLEServerWrite(String data) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_BLE_SERVER_WRITE);
        intent.putExtra(Constants.BLE_SERVER_WRITE, data);
        context.sendBroadcast(intent);
    }

    @Override
    public void onConnected(boolean result) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_BLE_SERVER_CONNECTED);
        intent.putExtra(Constants.BLE_SERVER_CONNECTED, result);
        context.sendBroadcast(intent);
    }

    @Override
    public void onBLEServerError(Error error) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_BLE_SERVER_ERROR);
        intent.putExtra(Constants.BLE_SERVER_ERROR, error);
        context.sendBroadcast(intent);
    }

    //endregion

}
