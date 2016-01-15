package gupuru.streetpassble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import gupuru.streetpassble.application.StreetPassApplication;
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

    public StreetPassBle(Context context) {
        this.context = context;
        initReceiver();
    }

    public interface OnStreetPassListener {
        void streetPassResult(int callbackType, String deviceAddress, String deviceName, String uuid, double distance, String serviceData);
        void advertiseSuccess(int txPowerLevel, int mode, int timeOut);
        void error(int errorCode, String errorMessage);
    }

    public void setOnStreetPassListener(OnStreetPassListener onStreetPassListener) {
        this.onStreetPassListener = onStreetPassListener;
    }

    public boolean canStreetPass() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isAdvertise() {
        return (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported());
    }

    public void start(String uuid) {
        if (!StreetPassApplication.get().isRunning()) {
            registerReceiver();
            Intent intent = new Intent(context,
                    StreetPassService.class);
            intent.putExtra(Constants.UUID, uuid);
            context.startService(intent);
        }
    }

    public void start(String uuid, String data) {
        if (!StreetPassApplication.get().isRunning()) {
            registerReceiver();
            Intent intent = new Intent(context,
                    StreetPassService.class);
            intent.putExtra(Constants.UUID, uuid);
            intent.putExtra(Constants.DATA, data);
            context.startService(intent);
        }
    }

    public void stop() {
        unregisterReceiver();
        if (StreetPassApplication.get().isRunning()) {
            Intent intent = new Intent(context,
                    StreetPassService.class);
            context.stopService(intent);
        }
    }

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

    private void registerReceiver() {
        context.registerReceiver(scanDataReceiver, scanDataIntentFilter);
        context.registerReceiver(advDataReceiver, advDataIntentFilter);
        context.registerReceiver(errorScanAdvReceiver, errorScanAdvFilter);
    }

    private void unregisterReceiver() {
        context.unregisterReceiver(scanDataReceiver);
        context.unregisterReceiver(errorScanAdvReceiver);
        context.unregisterReceiver(advDataReceiver);
    }

    private class ScanDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ScanDataParcelable scanDataParcelable = (ScanDataParcelable) intent.getExtras().get(Constants.SCAN_DATA);
            if (scanDataParcelable != null) {
                onStreetPassListener.streetPassResult(
                        scanDataParcelable.getCallbackType(),
                        scanDataParcelable.getDeviceAddress(),
                        scanDataParcelable.getDeviceName(),
                        scanDataParcelable.getUuid(),
                        scanDataParcelable.getDistance(),
                        scanDataParcelable.getServiceData()
                );
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

}
