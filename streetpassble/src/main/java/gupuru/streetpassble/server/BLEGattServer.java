package gupuru.streetpassble.server;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import gupuru.streetpassble.parcelable.TransferData;

/**
 * BluetoothGattでread, write, 端末接続された場合の処理 BluetoothGattServerCallback
 */
public class BLEGattServer extends BluetoothGattServerCallback {

    private BluetoothGattServer bluetoothGattServer;
    private BLEServer bleServer;
    private BluetoothDevice connectDevice;
    private String defaultSendResponseData = "";
    private OnBLEGattServerListener onBLEGattServerListener;

    public BLEGattServer(BLEServer bleServer) {
        this.bleServer = bleServer;
    }

    public interface OnBLEGattServerListener {
        void onServiceAdded(boolean result);

        void onCharacteristicWriteRequest(TransferData data);

        void onConnectionStateChange(boolean isConnect, BluetoothDevice device);
    }

    public void setOnBLEGattServerListener(OnBLEGattServerListener onBLEGattServerListener) {
        this.onBLEGattServerListener = onBLEGattServerListener;
    }

    /**
     * 未接続の場合はnullを返す
     *
     * @return
     */
    public BluetoothDevice getConnectDevice() {
        return connectDevice;
    }

    public void setDefaultSendResponseData(String defaultSendResponseData) {
        this.defaultSendResponseData = defaultSendResponseData;
    }

    public void setBluetoothGattServer(BluetoothGattServer gattServer) {
        this.bluetoothGattServer = gattServer;
    }

    /**
     * サービス開始
     *
     * @param status
     * @param service
     */
    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                if (onBLEGattServerListener != null) {
                    onBLEGattServerListener.onServiceAdded(true);
                }
            break;
            default:
                if (onBLEGattServerListener != null) {
                    onBLEGattServerListener.onServiceAdded(false);
                }
                break;
        }
    }

    /**
     * セントラル（クライアント）からReadRequestが来ると呼ばれる
     *
     * @param device
     * @param requestId
     * @param offset
     * @param characteristic
     */
    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        if (bluetoothGattServer != null) {
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, defaultSendResponseData.getBytes());
        }
    }

    /**
     * セントラル（クライアント）からWriteRequestが来ると呼ばれる
     *
     * @param device
     * @param requestId
     * @param characteristic
     * @param preparedWrite
     * @param responseNeeded
     * @param offset
     * @param value
     */
    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        if (bluetoothGattServer != null && value != null) {
            characteristic.setValue(value);
            String message = characteristic.getStringValue(offset);
            TransferData transferData = new TransferData(device.getAddress(), message);
            if (onBLEGattServerListener != null) {
                onBLEGattServerListener.onCharacteristicWriteRequest(transferData);
            }
            bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
        }
    }


    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId
            , BluetoothGattDescriptor descriptor, boolean preparedWrite
            , boolean responseNeeded, int offset, byte[] value) {
        Log.d("ここ", "onDescriptorWriteRequest");
    }


    /**
     * 接続状態
     *
     * @param device
     * @param status
     * @param newState
     */
    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);
        boolean isConnect;
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED:
                connectDevice = device;
                isConnect = true;
                bleServer.writeData("tes", 20);
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                connectDevice = null;
                isConnect = false;
                break;
            default:
                connectDevice = null;
                isConnect = false;
                break;
        }
        if (onBLEGattServerListener != null) {
            onBLEGattServerListener.onConnectionStateChange(isConnect, connectDevice);
        }
    }

}
