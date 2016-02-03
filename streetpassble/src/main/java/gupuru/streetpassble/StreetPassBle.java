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
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.ErrorParcelable;
import gupuru.streetpassble.parcelable.StreetPassSettings;
import gupuru.streetpassble.reciver.StreetPassReceiver;
import gupuru.streetpassble.service.StreetPassService;

public class StreetPassBle implements StreetPassReceiver.OnStreetPassReceiverListener {

    private Context context;
    private OnStreetPassListener onStreetPassListener;

    private IntentFilter streetPassIntentFilter;
    private StreetPassReceiver streetPassReceiver;

    public StreetPassBle(Context context) {
        this.context = context;
    }

    public interface OnStreetPassListener {
        void streetPassResult(DeviceData deviceData);

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
            initStreetPassReceiver();
            context.registerReceiver(streetPassReceiver, streetPassIntentFilter);
            StreetPassSettings streetPassSettings = new StreetPassSettings();
            streetPassSettings.setServiceUuid(uuid);
            updateBroadCast(streetPassSettings);
        }
    }

    public void start(StreetPassSettings streetPassSettings) {
        if (!serviceIsRunning()) {
            initStreetPassReceiver();
            context.registerReceiver(streetPassReceiver, streetPassIntentFilter);
            updateBroadCast(streetPassSettings);
        }
    }

    public void start(StreetPassSettings streetPassSettings, boolean canConnect) {
        if (!serviceIsRunning()) {
            initStreetPassReceiver();
            context.registerReceiver(streetPassReceiver, streetPassIntentFilter);
            updateBroadCast(streetPassSettings, canConnect);
        }
    }

    private void updateBroadCast(StreetPassSettings streetPassSettings) {
        Intent intent = new Intent(context,
                StreetPassService.class);
        intent.putExtra(Constants.STREET_PASS_SETTINGS, streetPassSettings);
        context.startService(intent);
    }

    private void updateBroadCast(StreetPassSettings streetPassSettings, boolean canConnect) {
        Intent intent = new Intent(context,
                StreetPassService.class);
        intent.putExtra(Constants.STREET_PASS_SETTINGS, streetPassSettings);
        intent.putExtra(Constants.CAN_CONNECT, canConnect);
        context.startService(intent);
    }

    /**
     * Service停止(BLEの送受信停止)
     */
    public void stop() {
        unregisterStreetPassReceiver();
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
            ErrorParcelable errorParcelable = new ErrorParcelable(Constants.CODE_UN_REGISTER_RECEIVER_ERROR, e.toString());
            onStreetPassListener.error(errorParcelable);
        }
    }

    //endregion

    //region StreetPassReceiver callback

    @Override
    public void onStreetPassScanResult(DeviceData deviceData) {
        onStreetPassListener.streetPassResult(deviceData);
    }

    @Override
    public void onStreetPassAdvertiseResult(AdvertiseSuccessParcelable advertiseSuccessParcelable) {
        onStreetPassListener.advertiseSuccess(advertiseSuccessParcelable);
    }

    @Override
    public void onStreetPassError(ErrorParcelable errorParcelable) {
        onStreetPassListener.error(errorParcelable);
    }

    //endregion

}
