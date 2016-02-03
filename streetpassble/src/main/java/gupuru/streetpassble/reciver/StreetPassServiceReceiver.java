package gupuru.streetpassble.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import gupuru.streetpassble.constants.Constants;

public class StreetPassServiceReceiver extends BroadcastReceiver {

    private OnStreetPassServiceReceiverListener onStreetPassServiceReceiverListener;

    public StreetPassServiceReceiver() {
    }

    public interface OnStreetPassServiceReceiverListener {
        void onConnectDeviceData(String deviceAddress);

        void onSendData(String data);

        void onClose();
    }

    public void setOnStreetPassServiceReceiverListener(OnStreetPassServiceReceiverListener onStreetPassServiceReceiverListener) {
        this.onStreetPassServiceReceiverListener = onStreetPassServiceReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.ACTION_CONNECT_DEVICE.equals(action)) {
            if (!TextUtils.isEmpty(intent.getStringExtra(Constants.DEVICE_ADDRESS))) {
                onStreetPassServiceReceiverListener.onConnectDeviceData(
                        intent.getStringExtra(Constants.DEVICE_ADDRESS)
                );
            }
        } else if (Constants.ACTION_SEND_DATA_TO_DEVICE.equals(action)) {
            if (!TextUtils.isEmpty(intent.getStringExtra(Constants.DATA))) {
                onStreetPassServiceReceiverListener.onSendData(intent.getStringExtra(Constants.DATA));
            }
        } else if (Constants.ACTION_CLOSE_GATT.equals(action)) {
            onStreetPassServiceReceiverListener.onClose();
        }
    }

}