package gupuru.streetpassble.callback;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.server.BLEServer;

public class ScanBle extends ScanCallback {

    private Context context;
    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter;
    private BLEServer bleServer;
    private ArrayList<DeviceData> deviceDataArrayList = new ArrayList<>();

    public ScanBle(Context context, BLEServer bleServer, BluetoothGatt bluetoothGatt, BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bleServer = bleServer;
        this.bluetoothGatt = bluetoothGatt;
        this.bluetoothAdapter = bluetoothAdapter;
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

            DeviceData deviceData
                    = new DeviceData(callbackType, bluetoothDevice.getAddress(), bluetoothDevice.getName(), uuid, distance, serviceData);

            if (deviceDataArrayList.isEmpty()) {
                deviceDataArrayList.add(deviceData);

                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());
                bluetoothGatt = device.connectGatt(context, true, bleServer);
                bluetoothGatt.connect();

                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_SCAN);
                intent.putExtra(Constants.SCAN_DATA, deviceData);
                context.sendBroadcast(intent);
            } else {
                try {
                    for (DeviceData aDeviceData : deviceDataArrayList) {
                        if (!aDeviceData.getDeviceAddress().equals(deviceData.getDeviceAddress())) {
                            deviceDataArrayList.add(deviceData);


                            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(bluetoothDevice.getAddress());
                            bluetoothGatt = device.connectGatt(context, false, bleServer);
                            bluetoothGatt.connect();




                            Intent intent = new Intent();
                            intent.setAction(Constants.ACTION_SCAN);
                            intent.putExtra(Constants.SCAN_DATA, deviceData);
                            context.sendBroadcast(intent);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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

        Error error = new Error(errorCode, errorMessage);

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_SCAN_ADV_ERROR);
        intent.putExtra(Constants.ERROR_SCAN_ADV, error);
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
