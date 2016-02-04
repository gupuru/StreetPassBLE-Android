package gupuru.streetpassble.reciver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.AdvertiseSuccess;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.DeviceData;

public class StreetPassReceiver extends BroadcastReceiver {

    private OnStreetPassReceiverListener onStreetPassReceiverListener;

    public StreetPassReceiver() {
    }

    public interface OnStreetPassReceiverListener {
        void onStreetPassScanResult(DeviceData deviceData);

        void onStreetPassAdvertiseResult(AdvertiseSuccess advertiseSuccess);

        void onStreetPassError(Error error);
    }

    public void setOnStreetPassReceiverListener(OnStreetPassReceiverListener onStreetPassReceiverListener) {
        this.onStreetPassReceiverListener = onStreetPassReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.ACTION_SCAN.equals(action)) {
            DeviceData deviceData
                    = (DeviceData) intent.getExtras().get(Constants.SCAN_DATA);
            if (deviceData != null) {
                onStreetPassReceiverListener.onStreetPassScanResult(deviceData);
            }
        } else if (Constants.ACTION_ADV.equals(action)) {
            AdvertiseSuccess advertiseSuccess
                    = (AdvertiseSuccess) intent.getExtras().get(Constants.ADV_DATA);
            if (advertiseSuccess != null) {
                onStreetPassReceiverListener.onStreetPassAdvertiseResult(advertiseSuccess);
            }
        } else if (Constants.ACTION_SCAN_ADV_ERROR.equals(action)) {
            Error error
                    = (Error) intent.getExtras().get(Constants.ERROR_SCAN_ADV);
            if (error != null) {
                onStreetPassReceiverListener.onStreetPassError(error);
            }
        }
    }

}