package gupuru.streetpassble.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.DeviceData;

public class ConnectDeviceReceiver extends BroadcastReceiver {

    private OnConnectDeviceReceiverListener onConnectDeviceReceiverListener;

    public ConnectDeviceReceiver() {
    }

    public interface OnConnectDeviceReceiverListener {
        void onStreetPassServiceAdded(boolean result);

        void onStreetPassGattServerStateChange(DeviceData deviceData, boolean isConnect);

        void onBLEConnected(boolean result);
    }

    public void setOnConnectDeviceReceiverListener(OnConnectDeviceReceiverListener onConnectDeviceReceiverListener) {
        this.onConnectDeviceReceiverListener = onConnectDeviceReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.ACTION_GATT_SERVICE_ADDED.equals(action)) {
            onConnectDeviceReceiverListener.onStreetPassServiceAdded(
                    intent.getBooleanExtra(Constants.SERVICE_ADDED, false)
            );
        } else if (Constants.ACTION_GATT_SERVER_STATE_CHANGE.equals(action)) {
            DeviceData deviceData
                    = (DeviceData) intent.getExtras().get(Constants.CONNECTION_DATA);
            boolean isConnect = intent.getBooleanExtra(Constants.IS_CONNECTION, false);
            if (deviceData != null) {
                onConnectDeviceReceiverListener.onStreetPassGattServerStateChange(deviceData, isConnect);
            }
        } else if (Constants.ACTION_BLE_SERVER_CONNECTED.equals(action)) {
            onConnectDeviceReceiverListener.onBLEConnected(
                    intent.getBooleanExtra(Constants.BLE_SERVER_CONNECTED, false)
            );
        }
    }

}
