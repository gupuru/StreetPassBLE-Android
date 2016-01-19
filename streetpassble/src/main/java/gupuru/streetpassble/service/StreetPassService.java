package gupuru.streetpassble.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gupuru.streetpassble.callback.AdvertiseBle;
import gupuru.streetpassble.callback.ScanBle;
import gupuru.streetpassble.constants.Constants;

public class StreetPassService extends Service {

    private Context context;
    private ScanBle scanBle;
    private AdvertiseBle advertiseBle;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;
    private String characteristicUuid;
    private String serviceUuid;
    private ConnectDeviceReceiver connectDeviceReceiver = null;
    private IntentFilter connectDeviceFilter = null;
    private SendDataToDeviceReceiver sendDataToDeviceReceiver = null;
    private IntentFilter sendDataToDeviceFilter = null;


    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(HEART_RATE_MEASUREMENT);


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
            //uuid取得
            if (!TextUtils.isEmpty(intent.getStringExtra(Constants.SERVICE_UUID))) {
                serviceUuid = intent.getStringExtra(Constants.SERVICE_UUID);
            }
            //serviceData取得
            String data = "";
            if (!TextUtils.isEmpty(intent.getStringExtra(Constants.DATA))) {
                data = intent.getStringExtra(Constants.DATA);
            }
            //scanMode取得
            int scanMode = intent.getIntExtra(Constants.SCAN_MODE, ScanSettings.SCAN_MODE_LOW_POWER);
            //txPowerLevel取得
            int txPowerLevel = intent.getIntExtra(Constants.TX_POWER_LEVEL, AdvertiseSettings.ADVERTISE_TX_POWER_LOW);
            //advertiseMode取得
            int advertiseMode = intent.getIntExtra(Constants.ADVERTISE_MODE, AdvertiseSettings.ADVERTISE_MODE_BALANCED);

            registerReceiver(connectDeviceReceiver, connectDeviceFilter);
            registerReceiver(sendDataToDeviceReceiver, sendDataToDeviceFilter);

            //BLEの送信に対応しているか
            if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                //対応->送受信
                scan(serviceUuid, scanMode);
                advertising(serviceUuid, data, advertiseMode, txPowerLevel);
            } else {
                //非対応->受信のみ
                scan(serviceUuid, scanMode);
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
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void scan(String uuid, int scanMode) {
        if (uuid != null && !uuid.equals("")) {
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(UUID.fromString(uuid)))
                    .build();
            filters.add(filter);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(scanMode)
                    .build();

            scanBle = new ScanBle(context);

            bluetoothLeScanner.startScan(filters, settings, scanBle);
        }
    }

    private void advertising(String uuid, String data, int advertiseMode, int txPowerLevel) {
        if (uuid != null && !uuid.equals("")) {
            // 設定
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(advertiseMode)
                    .setTxPowerLevel(txPowerLevel)
                    .setTimeout(0)
                    .setConnectable(true)
                    .build();

            if (data == null) {
                data = "";
            } else {
                if (data.getBytes().length > 20) {
                    data = data.substring(0, 15);
                }
            }
            // アドバタイジングデータ
            ParcelUuid pUuid = new ParcelUuid(UUID.fromString(uuid));
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addServiceUuid(pUuid)
                    .addServiceData(pUuid, data.getBytes(Charset.forName("UTF-8")))
                    .setIncludeDeviceName(true)
                    .setIncludeTxPowerLevel(true)
                    .build();

            advertiseBle = new AdvertiseBle(context);

            bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseBle);
        }
    }

    private class ConnectDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(intent.getStringExtra(Constants.DEVICE_ADDRESS))) {
                String deviceAddress = intent.getStringExtra(Constants.DEVICE_ADDRESS);
                characteristicUuid = intent.getStringExtra(Constants.CHARACTERISTIC_UUID);
                if (deviceAddress != null && !deviceAddress.equals("")
                        && characteristicUuid != null && !characteristicUuid.equals("")) {
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    bluetoothGatt = device.connectGatt(getApplicationContext(), false, bluetoothGattCallback);
                    bluetoothLeScanner.stopScan(scanBle);
                    bluetoothLeScanner = null;
                }
            }
        }
    }

    private class SendDataToDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(intent.getStringExtra(Constants.DATA))) {
                String data = intent.getStringExtra(Constants.DATA);
                bluetoothGattCharacteristic.setValue(data);
                bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
            }
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 接続に成功したらサービスを検索
                bluetoothGatt.connect();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 接続が切れたらGATTを空
                if (bluetoothGatt != null) {
                    bluetoothGatt.close();
                    bluetoothGatt = null;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if ((service == null) || (service.getUuid() == null)) {
                        continue;
                    }
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            bluetoothGatt.writeDescriptor(descriptor);
                            Log.d("ここ", "よばれるa");
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            Log.d("ここ", data.toString());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic != null) {
                final byte[] data = characteristic.getValue();
                String text = new String(data);
                Log.d("readByte", data.toString());
                Log.d("read", text);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (characteristic != null) {
                final byte[] data = characteristic.getValue();
                try {
                    String text = new String(data, "UTF-8");
                    Log.d("write", text);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
