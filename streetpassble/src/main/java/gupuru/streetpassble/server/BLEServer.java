package gupuru.streetpassble.server;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.UUID;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.ErrorParcelable;

public class BLEServer extends BluetoothGattCallback {

    private BluetoothGatt bluetoothGatt;
    private String serviceUuid;
    private String characteristicUuid;
    private OnBLEServerListener onBLEServerListener;

    public BLEServer() {
    }

    public interface OnBLEServerListener {
        void onBLEServerRead(String data);

        void onBLEServerWrite(String data);

        void onConnected(boolean result);

        void onBLEServerError(ErrorParcelable errorParcelable);
    }

    public void setOnBLEServerListener(OnBLEServerListener onBLEServerListener) {
        this.onBLEServerListener = onBLEServerListener;
    }

    private ErrorParcelable getErrorParcelable(String message) {
        return new ErrorParcelable(Constants.CODE_BLE_SERVER_ERROR, message);
    }

    public void writeData(String message) {
        BluetoothGattCharacteristic write = getCharacteristicData(serviceUuid, characteristicUuid);
        if (write != null) {
            write.setValue(message.getBytes());
            bluetoothGatt.writeCharacteristic(write);
        } else {
            onBLEServerListener.onBLEServerError(getErrorParcelable("Characteristic uuidがnullです。"));
        }
    }

    public void readData() {
        BluetoothGattCharacteristic read = getCharacteristicData(serviceUuid, characteristicUuid);
        if (read != null) {
            bluetoothGatt.readCharacteristic(read);
        } else {
            onBLEServerListener.onBLEServerError(getErrorParcelable("Characteristic uuidがnullです。"));
        }
    }

    private BluetoothGattCharacteristic getCharacteristicData(String sid, String cid) {
        BluetoothGattService s = bluetoothGatt.getService(UUID.fromString(sid));
        if (s == null) {
            onBLEServerListener.onBLEServerError(getErrorParcelable("Service uuidがnullです。"));
            return null;
        }
        BluetoothGattCharacteristic c = s.getCharacteristic(UUID.fromString(cid));
        if (c == null) {
            onBLEServerListener.onBLEServerError(getErrorParcelable("Characteristic uuidがnullです。"));
            return null;
        }
        return c;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            bluetoothGatt = gatt;
            bluetoothGatt.discoverServices();
            onBLEServerListener.onConnected(true);
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // 接続が切れたらGATTを空
            if (bluetoothGatt != null) {
                bluetoothGatt.close();
                bluetoothGatt = null;
                serviceUuid = null;
                characteristicUuid = null;
                onBLEServerListener.onConnected(false);
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            for (BluetoothGattService service : gatt.getServices()) {
                if ((service == null) || (service.getUuid() == null)) {
                    continue;
                }
                readData();
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        Log.d("ここ", "onCharacteristicChanged" + data.toString());
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic != null) {
            String data = characteristic.getStringValue(0);
            onBLEServerListener.onBLEServerRead(data);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic != null) {
            String data = characteristic.getStringValue(0);
            onBLEServerListener.onBLEServerWrite(data);
        }
    }

}
