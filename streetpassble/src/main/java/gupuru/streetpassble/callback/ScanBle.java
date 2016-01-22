package gupuru.streetpassble.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;

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
        if (result == null) {
            return;
        }
        BluetoothDevice bluetoothDevice = result.getDevice();
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null) {
            //推定距離
            double distance = getDistance(scanRecord, result);

            String uuid = "";
            byte[] data = null;
            String serviceData = "";
            for (ParcelUuid parcelUuid : scanRecord.getServiceUuids()) {
                //ServiceData取得
                data = scanRecord.getServiceData(parcelUuid);
                //uuid取得
                uuid = parcelUuid.getUuid().toString();
            }
            //ServiceDataをstringに変換
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

    /**
     * 推定距離を返す
     * @param scanRecord
     * @param result
     * @return
     */
    private double getDistance(ScanRecord scanRecord, ScanResult result) {
        return scanRecord.getTxPowerLevel() - result.getRssi();
    }

}
