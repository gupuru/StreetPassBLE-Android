package gupuru.streetpassble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import gupuru.streetpassble.callback.AdvertiseBle;
import gupuru.streetpassble.callback.ScanBle;
import gupuru.streetpassble.parcelable.*;
import gupuru.streetpassble.server.BLEGattServer;
import gupuru.streetpassble.server.BLEServer;
import gupuru.streetpassble.util.StreetPassServiceUtil;

public class StreetPassBle implements ScanBle.OnScanBleListener {


    private static final int DATA_MAX_SIZE = 512;
    private static final int DATA_MIN_SIZE = 20;

    private Context context;
    private StreetPassServiceUtil streetPassServiceUtil;

    private ScanBle scanBle;
    private AdvertiseBle advertiseBle;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private BluetoothAdapter bluetoothAdapter;
    private StreetPassSettings streetPassSettings;
    private BluetoothManager bluetoothManager;

    private BluetoothGattServer gattServer;
    private BLEServer bleServer;
    private BLEGattServer bleGattServer;
    private BluetoothGatt bluetoothGatt;

    public StreetPassBle(Context context) {
        this.context = context;
    }

    //region support

    /**
     * Bluetoothに対応しているか。対応 -> true, 非対応 -> false
     *
     * @return
     */
    public boolean isBluetooth() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    /**
     * BLEに対応しているか。対応 -> true, 非対応 -> false
     *
     * @return
     */
    public boolean isBle() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * BluetoothがONかどうか。 ON -> true, OFF -> false
     *
     * @return
     */
    public boolean isOnBluetooth() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    /**
     * BLEのAdvertiseが使えるか。可能 -> true, 不可能 -> false
     *
     * @return
     */
    public boolean isAdvertise() {
        return (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported());
    }

    /**
     * すれ違い通信が出来るかどうか 可能 -> true, 不可能 -> false
     *
     * @return
     */
    public boolean isStreetPassBle() {
        return isBle() && isBluetooth() && isAdvertise();
    }

    //endregion

    //region service control

    /**
     * すれ違い通信開始
     *
     * @param streetPassSettings
     *
     */
    public void start(StreetPassSettings streetPassSettings) {
        this.streetPassSettings = streetPassSettings;
        initStreetPass();
    }

    /**
     * BLE停止
     */
    public void stop() {
        stopScan();
        if (bluetoothLeAdvertiser != null && advertiseBle != null) {
            bluetoothLeAdvertiser.stopAdvertising(advertiseBle);
            bluetoothLeAdvertiser = null;
        }
        closeGattServer();
        streetPassSettings = null;
        streetPassServiceUtil = null;
        bleServer = null;
    }

    //endregion

    //region Sub Methods

    /**
     * ライブラリのバージョン取得
     *
     * @return
     */
    public String getLibraryVersion() {
        return BuildConfig.VERSION_NAME;
    }

    //endregion


    private void initStreetPass() {

        streetPassServiceUtil = new StreetPassServiceUtil();

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

        if (streetPassSettings.isAdvertiseConnectable()) {
            bleServer = new BLEServer(streetPassSettings);
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

    private void openGattServer() {
        //Serviceを登録
        BluetoothGattService service = new BluetoothGattService(
                streetPassSettings.getServiceUuid().getUuid(),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic readCharacteristic = new BluetoothGattCharacteristic(
                streetPassSettings.getReadCharacteristicUuid().getUuid(),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY |
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY
        );

        BluetoothGattDescriptor dataDescriptor = new BluetoothGattDescriptor(
                streetPassSettings.getReadCharacteristicUuid().getUuid()
                , BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattDescriptor.PERMISSION_READ);
        readCharacteristic.addDescriptor(dataDescriptor);

        service.addCharacteristic(readCharacteristic);

        bleGattServer = new BLEGattServer(readCharacteristic);
        gattServer = bluetoothManager.openGattServer(context, bleGattServer);
        bleGattServer.setBluetoothGattServer(gattServer);

        bleGattServer.setDefaultSendResponseData("neko");

        gattServer.addService(service);
    }

    /**
     * BLE scan開始
     */
    private void scan() {
        if (streetPassSettings.getServiceUuid() != null) {
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(streetPassSettings.getServiceUuid())
                    .build();
            filters.add(filter);

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(streetPassSettings.getScanMode())
                    .build();

            scanBle = new ScanBle(context, bleServer, bluetoothGatt, bluetoothAdapter);
            scanBle.setOnScanBleListener(this);

            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }

            bluetoothLeScanner.startScan(filters, settings, scanBle);
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
                if (streetPassServiceUtil.isLimitDataSize(serviceData, DATA_MIN_SIZE)) {
                    serviceData = streetPassServiceUtil.trimByte(serviceData, DATA_MIN_SIZE, "UTF-8");
                }
            }

            // アドバタイジングデータ
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addServiceUuid(streetPassSettings.getServiceUuid())
                    .addServiceData(streetPassSettings.getServiceUuid(), serviceData.getBytes())
                    .setIncludeDeviceName(streetPassSettings.isAdvertiseIncludeDeviceName())
                    .setIncludeTxPowerLevel(streetPassSettings.isAdvertiseIncludeTxPowerLevel())
                    .build();

            advertiseBle = new AdvertiseBle(context);

            bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseBle);
        }
    }

    private void stopScan() {
        if (scanBle != null && bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanBle);
            bluetoothLeScanner = null;
            scanBle = null;
        }
    }

    private void closeGattServer() {
        if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
        }
        bleGattServer = null;
    }


    @Override
    public void deviceDataInfo(DeviceData deviceData) {
        Log.d("ここ", deviceData.getDeviceName());
    }

    @Override
    public void error(gupuru.streetpassble.parcelable.Error error) {

    }


}
