package gupuru.streetpassble;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Parcelable;

import java.util.List;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.AdvertiseSuccess;
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.StreetPassSettings;
import gupuru.streetpassble.reciver.ConnectDeviceReceiver;
import gupuru.streetpassble.reciver.StreetPassReceiver;
import gupuru.streetpassble.service.StreetPassService;

public class StreetPassBle implements StreetPassReceiver.OnStreetPassReceiverListener,
        ConnectDeviceReceiver.OnConnectDeviceReceiverListener {

    private Context context;
    private OnStreetPassListener onStreetPassListener;
    private IntentFilter streetPassIntentFilter;
    private StreetPassReceiver streetPassReceiver;
    private ConnectDeviceReceiver connectDeviceReceiver;
    private OnConnectDeviceListener onConnectDeviceListener;

    public StreetPassBle(Context context) {
        this.context = context;
    }

    public interface OnStreetPassListener {
        void onReceivedData(DeviceData deviceData);

        void onAdvertiseResult(AdvertiseSuccess advertiseSuccess);

        void onStreetPassError(Error error);
    }

    public interface OnConnectDeviceListener {
        void onConnectedDeviceData(DeviceData deviceData);

        void onConnectedResult(boolean isConnected);

        void canConnect(boolean result);
    }

    public void setOnStreetPassListener(OnStreetPassListener onStreetPassListener) {
        this.onStreetPassListener = onStreetPassListener;
    }

    public void setOnConnectDeviceListener(OnConnectDeviceListener onConnectDeviceListener) {
        this.onConnectDeviceListener = onConnectDeviceListener;
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

    //endregion

    //region service control

    /**
     * すれ違い開始 uuid以外は、デフォルトの設定にする
     * @param uuid
     */
    public void start(String uuid) {
        if (!serviceIsRunning()) {
            initStreetPassReceiver();
            context.registerReceiver(streetPassReceiver, streetPassIntentFilter);
            StreetPassSettings streetPassSettings = new StreetPassSettings();
            streetPassSettings.setServiceUuid(uuid);
            startService(Constants.STREET_PASS_SETTINGS, streetPassSettings);
        }
    }

    /**
     * すれ違い通信開始 自由設定
     * @param streetPassSettings
     */
    public void start(StreetPassSettings streetPassSettings) {
        if (!serviceIsRunning()) {
            initStreetPassReceiver();
            context.registerReceiver(streetPassReceiver, streetPassIntentFilter);
            startService(Constants.STREET_PASS_SETTINGS, streetPassSettings);
        }
    }

    /**
     * すれ違い通信開始 端末と接続をするか true -> 接続する(GATTサーバーたてる), false -> しない
     * @param streetPassSettings
     * @param canConnect
     */
    public void start(StreetPassSettings streetPassSettings, boolean canConnect) {
        if (canConnect) {
            connectDeviceRegister();
        }
        if (!serviceIsRunning()) {
            initStreetPassReceiver();
            context.registerReceiver(streetPassReceiver, streetPassIntentFilter);
            startService(Constants.STREET_PASS_SETTINGS, streetPassSettings, canConnect);
        }
    }

    /**
     * StreetPassService 開始
     * @param name
     * @param parcelable
     */
    private void startService(String name, Parcelable parcelable) {
        Intent intent = new Intent(context,
                StreetPassService.class);
        intent.putExtra(name, parcelable);
        context.startService(intent);
    }

    /**
     * StreetPassService 開始 端末と接続する場合
     * @param name
     * @param parcelable
     * @param canConnect
     */
    private void startService(String name, Parcelable parcelable, boolean canConnect) {
        Intent intent = new Intent(context,
                StreetPassService.class);
        intent.putExtra(name, parcelable);
        intent.putExtra(Constants.CAN_CONNECT, canConnect);
        context.startService(intent);
    }

    /**
     * Service停止(BLEの送受信停止)
     */
    public void stop() {
        unregisterStreetPassReceiver();
        connectDeviceUnregister();
        if (serviceIsRunning()) {
            Intent intent = new Intent(context,
                    StreetPassService.class);
            context.stopService(intent);
        }
    }

    /**
     * serviceが動いているか  稼働中 -> true, 非稼働中 -> false
     *
     * @return
     */
    public boolean isRunning() {
        return serviceIsRunning();
    }

    /**
     * Serviceが稼働しているか。稼働中->true, 非稼働中->falseを返す
     *
     * @return
     */
    private boolean serviceIsRunning() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo info : services) {
            if (Constants.SERVICE_NAME.equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * BLE scanを停止する
     */
    public void stopScan() {
        updateBroadCast(Constants.ACTION_START_STOP_SCAN, Constants.DATA, false);
    }

    /**
     * BLE scanを開始する
     */
    public void startScan() {
        updateBroadCast(Constants.ACTION_START_STOP_SCAN, Constants.DATA, true);
    }

    //endregion

    //region Receiver

    /**
     * StreetPassReceiverの初期化
     */
    private void initStreetPassReceiver() {
        streetPassReceiver = new StreetPassReceiver();
        streetPassReceiver.setOnStreetPassReceiverListener(this);
        streetPassIntentFilter = new IntentFilter();
        streetPassIntentFilter.addAction(Constants.ACTION_SCAN);
        streetPassIntentFilter.addAction(Constants.ACTION_SCAN_ADV_ERROR);
        streetPassIntentFilter.addAction(Constants.ACTION_ADV);
    }

    /**
     * StreetPassReceiver解除
     */
    private void unregisterStreetPassReceiver() {
        try {
            context.unregisterReceiver(streetPassReceiver);
        } catch (IllegalArgumentException e) {
            Error error = new Error(Constants.CODE_UN_REGISTER_RECEIVER_ERROR, e.toString());
            if (onStreetPassListener != null) {
                onStreetPassListener.onStreetPassError(error);
            }
        }
    }

    /**
     * connectDevice Receiver登録
     */
    private void connectDeviceRegister() {
        connectDeviceReceiver = new ConnectDeviceReceiver();
        connectDeviceReceiver.setOnConnectDeviceReceiverListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_GATT_SERVICE_ADDED);
        intentFilter.addAction(Constants.ACTION_GATT_SERVER_STATE_CHANGE);
        intentFilter.addAction(Constants.ACTION_BLE_SERVER_CONNECTED);
        context.registerReceiver(connectDeviceReceiver, intentFilter);
    }

    /**
     * connectDevice Receiver解除
     */
    private void connectDeviceUnregister() {
        if (connectDeviceReceiver != null) {
            try {
                context.unregisterReceiver(connectDeviceReceiver);
                connectDeviceReceiver = null;
            } catch (IllegalArgumentException e) {
                Error error = new Error(Constants.CODE_UN_REGISTER_RECEIVER_ERROR, e.toString());
                if (onStreetPassListener != null) {
                    onStreetPassListener.onStreetPassError(error);
                }
            }
        }
    }

    //endregion

    //region StreetPassReceiver callback

    @Override
    public void onStreetPassScanResult(DeviceData deviceData) {
        if (onStreetPassListener != null) {
            onStreetPassListener.onReceivedData(deviceData);
        }
    }

    @Override
    public void onStreetPassAdvertiseResult(AdvertiseSuccess advertiseSuccess) {
        if (onStreetPassListener != null) {
            onStreetPassListener.onAdvertiseResult(advertiseSuccess);
        }
    }

    @Override
    public void onStreetPassError(Error error) {
        if (onStreetPassListener != null) {
            onStreetPassListener.onStreetPassError(error);
        }
    }

    //endregion

    //region OnConnectDeviceReceiverListener

    @Override
    public void onStreetPassServiceAdded(boolean result) {
        if (onConnectDeviceListener != null) {
            onConnectDeviceListener.canConnect(result);
        }
    }

    @Override
    public void onStreetPassGattServerStateChange(DeviceData deviceData, boolean isConnect) {
        if (onConnectDeviceListener != null) {
            onConnectDeviceListener.onConnectedDeviceData(deviceData);
        }
    }

    @Override
    public void onBLEConnected(boolean result) {
        if (onConnectDeviceListener != null) {
            onConnectDeviceListener.onConnectedResult(result);
        }
    }

    //endregion

    //region broadcast

    /**
     * broadcastを送信する
     * @param action
     * @param name
     * @param flg
     */
    private void updateBroadCast(String action, String name, boolean flg){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(name, flg);
        context.sendBroadcast(intent);
    }

    //endregion

    //region Sub Methods

    /**
     * 端末に接続する
     *
     * @param address
     */
    public void connectDevice(String address) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_CONNECT_DEVICE);
        intent.putExtra(Constants.DEVICE_ADDRESS, address);
        context.sendBroadcast(intent);
    }

    /**
     * ライブラリのバージョン取得
     * @return
     */
    public String getLibraryVersion() {
        return BuildConfig.VERSION_NAME;
    }

    //endregion

}
