package gupuru.streetpassble;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import java.util.List;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.AdvertiseSuccessParcelable;
import gupuru.streetpassble.parcelable.ErrorParcelable;
import gupuru.streetpassble.parcelable.ScanDataParcelable;
import gupuru.streetpassble.parcelable.StreetPassSettings;
import gupuru.streetpassble.reciver.StreetPassReceiver;
import gupuru.streetpassble.service.StreetPassService;

public class StreetPassBle implements StreetPassReceiver.OnStreetPassReceiverListener {

    private Context context;
    private OnStreetPassListener onStreetPassListener;
    private IntentFilter intentFilter;
    private StreetPassReceiver streetPassReceiver;

    public StreetPassBle(Context context) {
        this.context = context;
        initReceiver();
    }

    public interface OnStreetPassListener {
        void streetPassResult(ScanDataParcelable scanDataParcelable);

        void advertiseSuccess(AdvertiseSuccessParcelable advertiseSuccessParcelable);

        void error(ErrorParcelable errorParcelable);
    }

    public void setOnStreetPassListener(OnStreetPassListener onStreetPassListener) {
        this.onStreetPassListener = onStreetPassListener;
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

    public void start(String uuid) {
        if (!serviceIsRunning()) {
            context.registerReceiver(streetPassReceiver, intentFilter);
            StreetPassSettings streetPassSettings = new StreetPassSettings();
            streetPassSettings.setUuid(uuid);
            updateBroadCast(streetPassSettings);

        }
    }

    public void start(StreetPassSettings streetPassSettings) {
        if (!serviceIsRunning()) {
            context.registerReceiver(streetPassReceiver, intentFilter);
            updateBroadCast(streetPassSettings);
        }
    }

    private void updateBroadCast(StreetPassSettings streetPassSettings ) {
        Intent intent = new Intent(context,
                StreetPassService.class);
        intent.putExtra(Constants.STREET_PASS_SETTINGS, streetPassSettings);
        context.startService(intent);
    }

    /**
     * Service停止(BLEの送受信停止)
     */
    public void stop() {
        unregisterReceiver();
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
     * 端末に接続する
     *
     * @param address
     */
    public void connectDevice(String address, String characteristicUuid) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_CONNECT_DEVICE);
        intent.putExtra(Constants.DEVICE_ADDRESS, address);
        intent.putExtra(Constants.CHARACTERISTIC_UUID, characteristicUuid);
        context.sendBroadcast(intent);
    }

    /**
     * 端末にStringのデータを送信する
     *
     * @param data
     */
    public void sendDataToDevice(String data) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_SEND_DATA_TO_DEVICE);
        intent.putExtra(Constants.DATA, data);
        context.sendBroadcast(intent);
    }

    //region Receiver

    /**
     * Receiverの初期化
     */
    private void initReceiver() {
        streetPassReceiver = new StreetPassReceiver();
        streetPassReceiver.setOnStreetPassReceiverListener(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_SCAN);
        intentFilter.addAction(Constants.ACTION_SCAN_ADV_ERROR);
        intentFilter.addAction(Constants.ACTION_ADV);
    }

    /**
     * Receiver解除
     */
    private void unregisterReceiver() {
        boolean isRegistered = true;
        try {
            context.unregisterReceiver(streetPassReceiver);
        } catch (IllegalArgumentException e) {
            isRegistered = false;
        }
        if (!isRegistered) {
            ErrorParcelable errorParcelable = new ErrorParcelable(222, "unregisterReceiverエラー");
            onStreetPassListener.error(errorParcelable);
        }
    }

    //endregion

    //region StreetPassReceiver callback

    @Override
    public void scanResult(ScanDataParcelable scanDataParcelable) {
        onStreetPassListener.streetPassResult(scanDataParcelable);
    }

    @Override
    public void advertiseResult(AdvertiseSuccessParcelable advertiseSuccessParcelable) {
        onStreetPassListener.advertiseSuccess(advertiseSuccessParcelable);
    }

    @Override
    public void error(ErrorParcelable errorParcelable) {
        onStreetPassListener.error(errorParcelable);
    }

    //endregion

}
