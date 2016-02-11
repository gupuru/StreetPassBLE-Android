package gupuru.streetpassble.server;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.util.UUID;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.TransferData;

public class BLEServer extends BluetoothGattCallback {

    private BluetoothGatt bluetoothGatt;
    private String serviceUuid;
    private String characteristicUuid;
    private OnBLEServerListener onBLEServerListener;
    private String message;

    public BLEServer(String serviceUuid, String characteristicUuid) {
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
    }

    public interface OnBLEServerListener {
        void onBLEServerRead(TransferData data);

        void onBLEServerWrite(TransferData data);

        void onConnected(boolean result);

        void onBLEServerError(Error error);
    }

    public void setOnBLEServerListener(OnBLEServerListener onBLEServerListener) {
        this.onBLEServerListener = onBLEServerListener;
    }

    private Error getErrorParcelable(String message) {
        return new Error(Constants.CODE_BLE_SERVER_ERROR, message);
    }

    public void writeData(String message, int dataSize) {
        if (bluetoothGatt != null) {
            if (message == null) {
                message = "";
            }
            this.message = message;
            bluetoothGatt.requestMtu(dataSize);
        }
    }

    public void readData() {
        BluetoothGattCharacteristic read = getCharacteristicData(serviceUuid, characteristicUuid);
        if (read != null) {
            bluetoothGatt.readCharacteristic(read);
        } else {
            if (onBLEServerListener != null) {
                onBLEServerListener.onBLEServerError(getErrorParcelable("Characteristic uuidがnullです。"));
            }
        }
    }

    private BluetoothGattCharacteristic getCharacteristicData(String sid, String cid) {
        if (bluetoothGatt == null || bluetoothGatt.getServices() == null) {
            onBLEServerListener.onBLEServerError(getErrorParcelable("接続されていません"));
            return null;
        }
        BluetoothGattService bluetoothGattService;
        try {
            bluetoothGattService = bluetoothGatt.getService(UUID.fromString(sid));
        } catch (NullPointerException e) {
            if (onBLEServerListener != null) {
                onBLEServerListener.onBLEServerError(getErrorParcelable("uuidがnullです。"));
            }
            return null;
        }
        if (bluetoothGattService == null) {
            if (onBLEServerListener != null) {
                onBLEServerListener.onBLEServerError(getErrorParcelable("Service uuidがnullです。"));
            }
            return null;
        }
        BluetoothGattCharacteristic c = bluetoothGattService.getCharacteristic(UUID.fromString(cid));
        if (c == null) {
            if (onBLEServerListener != null) {
                onBLEServerListener.onBLEServerError(getErrorParcelable("Characteristic uuidがnullです。"));
            }
            return null;
        }
        return c;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            bluetoothGatt = gatt;
            bluetoothGatt.discoverServices();
            if (onBLEServerListener != null) {
                onBLEServerListener.onConnected(true);
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // 接続が切れたらGATTを空
            if (bluetoothGatt != null) {
                bluetoothGatt.close();
                if (onBLEServerListener != null) {
                    onBLEServerListener.onConnected(false);
                }
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
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic != null && gatt != null) {
            String data = characteristic.getStringValue(0);
            TransferData transferData = new TransferData(gatt.getDevice().getAddress(), data);
            onBLEServerListener.onBLEServerRead(transferData);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic != null && gatt != null) {
            String data = characteristic.getStringValue(0);
            TransferData transferData = new TransferData(gatt.getDevice().getAddress(), data);
            if (onBLEServerListener != null) {
                onBLEServerListener.onBLEServerWrite(transferData);
            }
        }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        BluetoothGattCharacteristic write = getCharacteristicData(serviceUuid, characteristicUuid);
        if (bluetoothGatt != null && write != null) {
            write.setValue(message.getBytes());
            bluetoothGatt.writeCharacteristic(write);
        } else {
            if (onBLEServerListener != null) {
                onBLEServerListener.onBLEServerError(getErrorParcelable("Characteristic uuidがnullです。"));
            }
        }
    }

}
