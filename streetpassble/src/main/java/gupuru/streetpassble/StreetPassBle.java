package gupuru.streetpassble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import gupuru.streetpassble.callback.AdvertiseBle;
import gupuru.streetpassble.callback.ScanBle;
import gupuru.streetpassble.parcelable.AdvertiseSuccess;
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.StreetPassSettings;
import gupuru.streetpassble.parcelable.TransferData;
import gupuru.streetpassble.server.BLEGattServer;
import gupuru.streetpassble.server.BLEServer;

/**
 * すれ違い通信
 */
public class StreetPassBle implements ScanBle.OnScanBleListener,
        AdvertiseBle.OnAdvertiseBleListener, BLEServer.OnBLEServerListener,
        BLEGattServer.OnBLEGattServerListener {

    private Context context;
    private OnStreetPassBleListener onStreetPassBleListener;
    private OnStreetPassBleServerListener onStreetPassBleServerListener;

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

    public StreetPassBle(Context context) {
        this.context = context;
    }

    public void setOnStreetPassBleListener(OnStreetPassBleListener onStreetPassBleListener) {
        this.onStreetPassBleListener = onStreetPassBleListener;
    }

    public void setOnStreetPassBleServerListener(OnStreetPassBleServerListener onStreetPassBleServerListener) {
        this.onStreetPassBleServerListener = onStreetPassBleServerListener;
    }

    public interface OnStreetPassBleListener {
        void nearByDevices(DeviceData deviceData);

        void error(gupuru.streetpassble.parcelable.Error error);

        void receivedData(TransferData data);
    }

    public interface OnStreetPassBleServerListener {

        void onScanCallbackDeviceDataInfo(DeviceData deviceData);

        void onScanCallbackError(gupuru.streetpassble.parcelable.Error error);

        void onAdvertiseBleSuccess(AdvertiseSuccess advertiseSuccess);

        void onAdvertiseBleError(gupuru.streetpassble.parcelable.Error error);

        void onBLEServerRead(TransferData data);

        void onBLEServerWrite(TransferData data);

        void onBLEServerConnected(boolean result);

        void onBLEServerError(Error error);

        void onBLEGattServerServiceAdded(boolean result);

        void onBLEGattServerCharacteristicWriteRequest(TransferData data);

        void onBLEGattServerConnectionStateChange(boolean isConnect, BluetoothDevice device);

    }

    //region service control

    /**
     * すれ違い通信開始
     *
     * @param streetPassSettings
     */
    public void start(StreetPassSettings streetPassSettings) {
        this.streetPassSettings = streetPassSettings;
        initStreetPass();
    }

    /**
     * すれ違い通信開始
     */
    public void start() {
        this.streetPassSettings = new StreetPassSettings.Builder().build();
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
        bleServer = null;
    }

    //endregion

    /**
     * scanとadvertising初期化
     */
    private void initStreetPass() {
        //BLEの送信に対応しているか
        if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

            bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();

            bleServer = new BLEServer(streetPassSettings);
            bleServer.setOnBLEServerListener(this);
            //gatt server開く
            openGattServer();

            //対応->送受信
            scan();
            advertising();
        }
    }

    /**
     * GattServerを開く
     */
    private void openGattServer() {
        //Serviceを登録
        BluetoothGattService service = new BluetoothGattService(
                streetPassSettings.getServiceUuid().getUuid(),
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //readCharacteristic登録
        BluetoothGattCharacteristic readCharacteristic = new BluetoothGattCharacteristic(
                streetPassSettings.getReadCharacteristicUuid().getUuid(),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY |
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY
        );
        //writeCharacteristic登録
        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                streetPassSettings.getWriteCharacteristicUuid().getUuid(),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY |
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY
        );

        service.addCharacteristic(readCharacteristic);
        service.addCharacteristic(writeCharacteristic);

        bleGattServer = new BLEGattServer(streetPassSettings.getData());
        gattServer = bluetoothManager.openGattServer(context, bleGattServer);
        bleGattServer.setBluetoothGattServer(gattServer);
        bleGattServer.setOnBLEGattServerListener(this);

        gattServer.addService(service);
    }

    /**
     * BLE scan開始
     */
    private void scan() {
        if (streetPassSettings != null && streetPassSettings.getServiceUuid() != null) {
            //uuidのフィルター設定
            List<ScanFilter> filters = new ArrayList<>();
            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(streetPassSettings.getServiceUuid())
                    .build();
            filters.add(filter);

            //scan頻度の設定
            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(streetPassSettings.getScanMode())
                    .build();

            scanBle = new ScanBle(context, bleServer, bluetoothAdapter);
            scanBle.setOnScanBleListener(this);

            if (bluetoothLeScanner == null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }
            //scan開始
            bluetoothLeScanner.startScan(filters, settings, scanBle);
        }
    }

    /**
     * advertising設定
     */
    private void advertising() {
        if (streetPassSettings != null && streetPassSettings.getServiceUuid() != null) {
            // Advertising設定
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(streetPassSettings.getAdvertiseMode())
                    .setTxPowerLevel(streetPassSettings.getTxPowerLevel())
                    .setTimeout(0)
                    .setConnectable(true)
                    .build();
            // アドバタイジングデータ
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addServiceUuid(streetPassSettings.getServiceUuid())
                    .addServiceData(streetPassSettings.getServiceUuid(), "android".getBytes())
                    .setIncludeDeviceName(streetPassSettings.isAdvertiseIncludeDeviceName())
                    .setIncludeTxPowerLevel(streetPassSettings.isAdvertiseIncludeTxPowerLevel())
                    .build();

            advertiseBle = new AdvertiseBle();
            advertiseBle.setOnAdvertiseBleListener(this);

            //アドバタイジング開始
            bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, advertiseBle);
        }
    }

    /**
     * scan停止
     */
    private void stopScan() {
        if (scanBle != null && bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanBle);
            bluetoothLeScanner = null;
            scanBle = null;
        }
    }

    /**
     * GattServer停止
     */
    private void closeGattServer() {
        if (gattServer != null) {
            gattServer.clearServices();
            gattServer.close();
            gattServer = null;
        }
        bleGattServer = null;
    }

    //region scan callback

    @Override
    public void deviceDataInfo(DeviceData deviceData) {
        if (onStreetPassBleListener != null) {
            onStreetPassBleListener.nearByDevices(deviceData);
        }
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onScanCallbackDeviceDataInfo(deviceData);
        }
    }

    @Override
    public void error(gupuru.streetpassble.parcelable.Error error) {
        if (onStreetPassBleListener != null) {
            onStreetPassBleListener.error(error);
        }
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onScanCallbackError(error);
        }

    }

    //endregion

    //region Advertise callback

    @Override
    public void onAdvertiseBleSuccess(AdvertiseSuccess advertiseSuccess) {
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onAdvertiseBleSuccess(advertiseSuccess);
        }
    }

    @Override
    public void onAdvertiseBleError(gupuru.streetpassble.parcelable.Error error) {
        if (onStreetPassBleListener != null) {
            onStreetPassBleListener.error(error);
        }
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onAdvertiseBleError(error);
        }
    }

    //endregion

    //region BLEServer callback

    @Override
    public void onBLEServerRead(TransferData data) {
        if (bleServer != null) {
            bleServer.writeData(streetPassSettings.getData(), 512);
        }
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onBLEServerRead(data);
        }
    }

    @Override
    public void onBLEServerWrite(TransferData data) {
        if (bleServer != null) {
            bleServer.disConnect();
        }
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onBLEServerWrite(data);
        }
    }

    @Override
    public void onConnected(boolean result) {
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onBLEServerConnected(result);
        }
    }

    @Override
    public void onBLEServerError(Error error) {
        if (onStreetPassBleListener != null) {
            onStreetPassBleListener.error(error);
        }
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onBLEServerError(error);
        }
    }

    //endregion

    //region BLEGattServer callback

    @Override
    public void onServiceAdded(boolean result) {
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onBLEGattServerServiceAdded(result);
        }
    }

    @Override
    public void onCharacteristicWriteRequest(TransferData data) {
        if (onStreetPassBleListener != null) {
            onStreetPassBleListener.receivedData(data);
        }
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onBLEGattServerCharacteristicWriteRequest(data);
        }
    }

    @Override
    public void onConnectionStateChange(boolean isConnect, BluetoothDevice device) {
        if (onStreetPassBleServerListener != null) {
            onStreetPassBleServerListener.onBLEGattServerConnectionStateChange(isConnect, device);
        }
    }

    //endregion

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

}
