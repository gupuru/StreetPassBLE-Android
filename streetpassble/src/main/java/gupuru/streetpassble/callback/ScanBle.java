package gupuru.streetpassble.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.ErrorParcelable;
import gupuru.streetpassble.parcelable.ScanDataParcelable;

public class ScanBle extends ScanCallback {

    private Context context;

    public ScanBle(Context context) {
        this.context = context;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        if (result == null
                || result.getDevice() == null
                || TextUtils.isEmpty(result.getDevice().getName()))
            return;

        BluetoothDevice bluetoothDevice = result.getDevice();
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null) {
            double distance = scanRecord.getTxPowerLevel() - result.getRssi();
            String uuid = "";
            byte[] data = null;
            String serviceData = "";

            for (ParcelUuid parcelUuid : scanRecord.getServiceUuids()) {
                data = scanRecord.getServiceData(parcelUuid);
                uuid = parcelUuid.getUuid().toString();
            }
            if (data != null) {
                try {
                    serviceData = new String(data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            ScanDataParcelable scanDataParcelable
                    = new ScanDataParcelable(callbackType, bluetoothDevice.getAddress(), bluetoothDevice.getName(), uuid, distance, serviceData);
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_SCAN);
            intent.putExtra(Constants.SCAN_DATA, scanDataParcelable);
            context.sendBroadcast(intent);
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        String errorMessage = "";
        switch (errorCode) {
            case SCAN_FAILED_ALREADY_STARTED:
                errorMessage = "既にBLEスキャンを実行中です";
                break;
            case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                errorMessage = "BLEスキャンを開始できませんでした";
                break;
            case SCAN_FAILED_FEATURE_UNSUPPORTED:
                errorMessage = "BLEの検索をサポートしていません。";
                break;
            case SCAN_FAILED_INTERNAL_ERROR:
                errorMessage = "内部エラーが発生しました";
                break;
        }

        ErrorParcelable errorParcelable = new ErrorParcelable(errorCode, errorMessage);

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_SCAN_ADV_ERROR);
        intent.putExtra(Constants.ERROR_SCAN_ADV, errorParcelable);
        context.sendBroadcast(intent);
    }

}
