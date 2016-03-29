package gupuru.streetpassble.server;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.StreetPassSettings;
import gupuru.streetpassble.parcelable.TransferData;

public class BLEServer extends BluetoothGattCallback {

    private BluetoothGatt bluetoothGatt;
    private StreetPassSettings streetPassSettings;
    private OnBLEServerListener onBLEServerListener;
    private String message;

    public BLEServer(StreetPassSettings streetPassSettings) {
        this.streetPassSettings = streetPassSettings;
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

            BluetoothGattCharacteristic write = getCharacteristicData(
                    streetPassSettings.getServiceUuid().getUuid(),
                    streetPassSettings.getWriteCharacteristicUuid().getUuid());
            if (bluetoothGatt != null && write != null) {
                write.setValue(message.getBytes());
                bluetoothGatt.writeCharacteristic(write);
            }
        }
    }

    public void readData() {
        BluetoothGattCharacteristic read = getCharacteristicData(
                streetPassSettings.getServiceUuid().getUuid(),
                streetPassSettings.getReadCharacteristicUuid().getUuid()
        );
        if (read != null) {
            bluetoothGatt.readCharacteristic(read);
        } else {
            if (onBLEServerListener != null) {
                onBLEServerListener.onBLEServerError(getErrorParcelable("Characteristic uuidがnullです。"));
            }
        }
    }

    private BluetoothGattCharacteristic getCharacteristicData(UUID sid, UUID cid) {
        if (bluetoothGatt == null || bluetoothGatt.getServices() == null) {
            onBLEServerListener.onBLEServerError(getErrorParcelable("接続されていません"));
            return null;
        }
        BluetoothGattService bluetoothGattService;
        try {
            bluetoothGattService = bluetoothGatt.getService(sid);
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
        BluetoothGattCharacteristic c = bluetoothGattService.getCharacteristic(cid);
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
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                for (BluetoothGattService service : gatt.getServices()) {
                    if ((service == null) || (service.getUuid() == null)) {
                        break;
                    } else {
                        // サービスを見つけた
                        List<BluetoothGattCharacteristic> characteristics =
                                service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : characteristics) {
                            if (characteristic != null) {

                                // Notification を要求する
                                gatt.setCharacteristicNotification(characteristic, true);

                                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                                    Log.d("ここ", "ごりら " + descriptor.getUuid().toString());
                                }

                                // Characteristic の Notification 有効化
                               /* BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                        streetPassSettings.getReadCharacteristicUuid().getUuid());
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);*/

                                readData();

                                // Notification を要求する
                               /* gatt.setCharacteristicNotification(characteristic, true);
                                for (BluetoothGattDescriptor descriptor: characteristic.getDescriptors()){
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    gatt.writeDescriptor(descriptor);
                                }*/

                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d("ここ", "呼ばれる？");
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {

        Log.d("ここ", "呼");

        if (characteristic != null && gatt != null) {
            String data = characteristic.getStringValue(0);
            Log.d("ここ", "onCharacteristicRead " + data);
            TransferData transferData = new TransferData(gatt.getDevice().getAddress(), data);
            onBLEServerListener.onBLEServerRead(transferData);
        }

//        writeData("e", 20);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {

        Log.d("ここ", "a");

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
        Log.d("ここ", "sss");

        BluetoothGattCharacteristic write = getCharacteristicData(
                streetPassSettings.getServiceUuid().getUuid(),
                streetPassSettings.getReadCharacteristicUuid().getUuid());
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
