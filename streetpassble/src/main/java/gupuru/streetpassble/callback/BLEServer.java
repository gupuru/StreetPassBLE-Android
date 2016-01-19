package gupuru.streetpassble.callback;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class BLEServer extends BluetoothGattCallback {

    private Context context;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;

    public BLEServer(Context context, BluetoothGatt bluetoothGatt) {
        this.context = context;
        this.bluetoothGatt = bluetoothGatt;
    }

    public void sendData(String data) {
        characteristic.setValue(data.getBytes());
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            bluetoothGatt = gatt;
            bluetoothGatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            // 接続が切れたらGATTを空
            if (bluetoothGatt != null) {
                bluetoothGatt.close();
                bluetoothGatt = null;
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
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    this.characteristic = characteristic;
                    this.characteristic.setValue("test".getBytes());
                    bluetoothGatt.writeCharacteristic(this.characteristic);
                    bluetoothGatt.readCharacteristic(this.characteristic);
                    Log.d("ここ", "よばれるa");
                }
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        Log.d("ここ", data.toString());
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic != null) {
            final byte[] data = characteristic.getValue();
            String text = new String(data);
            Log.d("readByte", data.toString());
            Log.d("read", text);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        if (characteristic != null) {
            final byte[] data = characteristic.getValue();
            try {
                String text = new String(data, "UTF-8");
                Log.d("write", text);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

}
