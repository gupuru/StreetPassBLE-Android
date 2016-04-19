package gupuru.streetpassble.callback;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.ParcelUuid;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.server.BLEServer;

/**
 * BLEのスキャン処理 ScanCallback
 */
public class ScanBle extends ScanCallback {

    private Context context;
    private OnScanBleListener onScanBleListener;
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

    public interface OnScanBleListener {
        void deviceDataInfo(DeviceData deviceData);

        void error(Error error);
    }

    public void setOnScanBleListener(OnScanBleListener onScanBleListener) {
        this.onScanBleListener = onScanBleListener;
    }

    //region BluetoothLeScanner

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        if (result != null) {
            analyzeScanResult(callbackType, result);
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
                errorMessage = "SCAN_FAILED_ALREADY_STARTED/既にBLEスキャンを実行中です";
                break;
            case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                errorMessage = "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED/BLEスキャンを開始できませんでした";
                break;
            case SCAN_FAILED_FEATURE_UNSUPPORTED:
                errorMessage = "SCAN_FAILED_FEATURE_UNSUPPORTED/BLEの検索をサポートしていません。";
                break;
            case SCAN_FAILED_INTERNAL_ERROR:
                errorMessage = "SCAN_FAILED_INTERNAL_ERROR/内部エラーが発生しました";
                break;
        }
        //error callback
        Error error = new Error(errorCode, errorMessage);
        if (onScanBleListener != null) {
            onScanBleListener.error(error);
        }
    }

    //endregion

    //region Sub Methods

    /**
     * scanした結果を解析する
     *
     * @param callbackType
     * @param result
     */
    private void analyzeScanResult(int callbackType, ScanResult result) {
        BluetoothDevice bluetoothDevice = result.getDevice();
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null) {
            //推定距離
            double distance = getDistance(scanRecord, result);
            //uuid, ServiceData取得
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
                    return;
                }
            }
            //advertisingしている端末の情報
            DeviceData deviceData = new DeviceData(
                    callbackType,
                    bluetoothDevice.getAddress(),
                    bluetoothDevice.getName(),
                    uuid,
                    distance,
                    serviceData
            );
            //端末に接続するか
            if (isConnectedDevice(deviceData)) {
                //接続
                connectDevice(bluetoothDevice.getAddress(), deviceData);
            }
        }
    }

    /**
     * 端末に接続できるか
     *
     * @param deviceData
     * @return
     */
    private boolean isConnectedDevice(DeviceData deviceData) {
        try {
            if (deviceDataArrayList.isEmpty()) {
                deviceDataArrayList.add(deviceData);
                return true;
            } else {
                //接続したことない端末のみ接続する
                for (int i = 0; i < deviceDataArrayList.size(); i++) {
                    if (!deviceDataArrayList.get(i).getDeviceAddress().equals(deviceData.getDeviceAddress())) {
                        deviceDataArrayList.add(deviceData);
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 端末接続
     *
     * @param address
     * @param deviceData
     */
    private void connectDevice(String address, DeviceData deviceData) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothGatt = device.connectGatt(context, false, bleServer);
        //接続
        bluetoothGatt.connect();
        //callback
        if (onScanBleListener != null) {
            onScanBleListener.deviceDataInfo(deviceData);
        }
    }

    /**
     * 推定距離を返す
     *
     * @param scanRecord
     * @param result
     * @return
     */
    private double getDistance(ScanRecord scanRecord, ScanResult result) {
        return scanRecord.getTxPowerLevel() - result.getRssi();
    }

    //endregion

}
