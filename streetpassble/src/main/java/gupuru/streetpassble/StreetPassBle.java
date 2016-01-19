package gupuru.streetpassble;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import java.util.List;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.AdvertiseSuccessParcelable;
import gupuru.streetpassble.parcelable.ErrorParcelable;
import gupuru.streetpassble.parcelable.ScanDataParcelable;
import gupuru.streetpassble.service.StreetPassService;

public class StreetPassBle {

    private Context context;
    private OnStreetPassListener onStreetPassListener;

    private ErrorScanAdvReceiver errorScanAdvReceiver = null;
    private IntentFilter errorScanAdvFilter = null;
    private ScanDataReceiver scanDataReceiver = null;
    private IntentFilter scanDataIntentFilter = null;
    private AdvDataReceiver advDataReceiver = null;
    private IntentFilter advDataIntentFilter = null;

    private int scanMode = ScanSettings.SCAN_MODE_LOW_POWER;
    private int advertiseMode = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
    private int txPowerLevel = AdvertiseSettings.ADVERTISE_TX_POWER_LOW;

    public StreetPassBle(Context context) {
        this.context = context;
        initReceiver();
    }

    public interface OnStreetPassListener {
        void streetPassResult(ScanDataParcelable scanDataParcelable);
        void advertiseSuccess(int txPowerLevel, int mode, int timeOut);
        void error(int errorCode, String errorMessage);
    }

    public void setOnStreetPassListener(OnStreetPassListener onStreetPassListener) {
        this.onStreetPassListener = onStreetPassListener;
    }

    /**
     * BLEに対応しているか。対応している場合はtrue, していない場合はfalseを返す
     * @return
     */
    public boolean isStreetPass() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 送信可能の場合true, できない場合はfalseを返す
     * @return
     */
    public boolean isAdvertise() {
        return (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported());
    }

    public void start(String uuid) {
        if (!serviceIsRunning()) {
            registerReceiver();
            Intent intent = new Intent(context,
                    StreetPassService.class);
            intent.putExtra(Constants.SERVICE_UUID, uuid);
            context.startService(intent);
        }
    }

    public void start(String uuid, String data) {
        if (!serviceIsRunning()) {
            registerReceiver();
            Intent intent = new Intent(context,
                    StreetPassService.class);
            intent.putExtra(Constants.SERVICE_UUID, uuid);
            intent.putExtra(Constants.DATA, data);
            intent.putExtra(Constants.SCAN_MODE, scanMode);
            intent.putExtra(Constants.ADVERTISE_MODE, advertiseMode);
            intent.putExtra(Constants.TX_POWER_LEVEL, txPowerLevel);
            context.startService(intent);
        }
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
     * ScanModeの設定
     * @param scanMode
     */
    public void setScanMode(int scanMode) {
        this.scanMode = scanMode;
    }

    /**
     * Advertiseの設定
     * @param advertiseMode
     */
    public void setAdvertiseMode(int advertiseMode) {
        this.advertiseMode = advertiseMode;
    }

    /**
     * TxPowerLevelの設定
     * @param txPowerLevel
     */
    public void setTxPowerLevel(int txPowerLevel) {
        this.txPowerLevel = txPowerLevel;
    }

    /**
     * 端末に接続する
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
    private void initReceiver(){
        scanDataReceiver = new ScanDataReceiver();
        scanDataIntentFilter = new IntentFilter(
                Constants.ACTION_SCAN);
        errorScanAdvReceiver = new ErrorScanAdvReceiver();
        errorScanAdvFilter = new IntentFilter(
                Constants.ACTION_SCAN_ADV_ERROR);
        advDataReceiver = new AdvDataReceiver();
        advDataIntentFilter = new IntentFilter(
                Constants.ACTION_ADV);
    }

    /**
     * Receiver登録
     */
    private void registerReceiver() {
        context.registerReceiver(scanDataReceiver, scanDataIntentFilter);
        context.registerReceiver(advDataReceiver, advDataIntentFilter);
        context.registerReceiver(errorScanAdvReceiver, errorScanAdvFilter);
    }

    /**
     * Receiver解除
     */
    private void unregisterReceiver() {
        if (scanDataReceiver != null) {
            context.unregisterReceiver(scanDataReceiver);
        }
        if (errorScanAdvReceiver != null) {
            context.unregisterReceiver(errorScanAdvReceiver);
        }
        if (advDataReceiver != null) {
            context.unregisterReceiver(advDataReceiver);
        }
    }

    private class ScanDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ScanDataParcelable scanDataParcelable = (ScanDataParcelable) intent.getExtras().get(Constants.SCAN_DATA);
            if (scanDataParcelable != null) {
                onStreetPassListener.streetPassResult(scanDataParcelable);
            }
        }
    }

    private class AdvDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            AdvertiseSuccessParcelable advertiseSuccessParcelable = (AdvertiseSuccessParcelable) intent.getExtras().get(Constants.ADV_DATA);
            if (advertiseSuccessParcelable != null) {
                onStreetPassListener.advertiseSuccess(
                        advertiseSuccessParcelable.getTxPowerLevel(),
                        advertiseSuccessParcelable.getMode(),
                        advertiseSuccessParcelable.getTimeOut()
                );
            }
        }
    }

    private class ErrorScanAdvReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ErrorParcelable errorParcelable = (ErrorParcelable) intent.getExtras().get(Constants.ERROR_SCAN_ADV);
            if (errorParcelable != null) {
                onStreetPassListener.error(
                        errorParcelable.getErrorCode(),
                        errorParcelable.getErrorMessage()
                );
            }
        }
    }

    //endregion

    /**
     * Serviceが稼働しているか。稼働中->true, 非稼働中->falseを返す
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

}
