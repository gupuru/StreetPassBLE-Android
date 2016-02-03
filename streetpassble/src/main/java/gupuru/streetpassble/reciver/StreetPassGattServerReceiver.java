package gupuru.streetpassble.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.ErrorParcelable;

/**
 * GATTサーバーのレシーバー BroadcastReceiver
 */
public class StreetPassGattServerReceiver extends BroadcastReceiver {

    private OnStreetPassGattServerListener onStreetPassGattServerListener;

    public StreetPassGattServerReceiver() {
    }

    public interface OnStreetPassGattServerListener {
        void onStreetPassGattServerStateChange(DeviceData deviceData, boolean isConnect);

        void onStreetPassGattServerRead(DeviceData deviceData);

        void onStreetPassGattServerWrite(String message);

        void onStreetPassServiceAdded(boolean result);

        void onBLEServerRead(String data);

        void onBLEServerWrite(String data);

        void onBLEConnected(boolean result);

        void onBLEServerError(ErrorParcelable errorParcelable);
    }

    public void setOnStreetPassGattServerListener(OnStreetPassGattServerListener onStreetPassGattServerListener) {
        this.onStreetPassGattServerListener = onStreetPassGattServerListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.ACTION_GATT_SERVER_STATE_CHANGE.equals(action)) {
            DeviceData deviceData
                    = (DeviceData) intent.getExtras().get(Constants.CONNECTION_DATA);
            boolean isConnect = intent.getBooleanExtra(Constants.IS_CONNECTION, false);
            if (deviceData != null) {
                onStreetPassGattServerListener.onStreetPassGattServerStateChange(deviceData, isConnect);
            }
        } else if (Constants.ACTION_GATT_SERVER_READ_REQUEST.equals(action)) {
            DeviceData deviceData
                    = (DeviceData) intent.getExtras().get(Constants.READ_REQUEST);
            if (deviceData != null) {
                onStreetPassGattServerListener.onStreetPassGattServerRead(deviceData);
            }
        } else if (Constants.ACTION_GATT_SERVER_WRITE_REQUEST.equals(action)) {
            String message
                    = intent.getStringExtra(Constants.WRITE_REQUEST);
            if (message != null) {
                onStreetPassGattServerListener.onStreetPassGattServerWrite(message);
            }
        } else if (Constants.ACTION_GATT_SERVICE_ADDED.equals(action)) {
            onStreetPassGattServerListener.onStreetPassServiceAdded(
                    intent.getBooleanExtra(Constants.SERVICE_ADDED, false)
            );
        } else if (Constants.ACTION_BLE_SERVER_CONNECTED.equals(action)) {
            onStreetPassGattServerListener.onBLEConnected(
                    intent.getBooleanExtra(Constants.BLE_SERVER_CONNECTED, false)
            );
        } else if (Constants.ACTION_BLE_SERVER_READ.equals(action)){
            onStreetPassGattServerListener.onBLEServerRead(
                    intent.getStringExtra(Constants.BLE_SERVER_READ)
            );
        } else if (Constants.ACTION_BLE_SERVER_WRITE.equals(action)){
            onStreetPassGattServerListener.onBLEServerWrite(
                    intent.getStringExtra(Constants.BLE_SERVER_WRITE)
            );
        } else if (Constants.ACTION_BLE_SERVER_ERROR.equals(action)){
            onStreetPassGattServerListener.onBLEServerError(
                    (ErrorParcelable) intent.getExtras().get(Constants.BLE_SERVER_ERROR)
            );
        }
    }

}