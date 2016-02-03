package gupuru.streetpassble.reciver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.AdvertiseSuccessParcelable;
import gupuru.streetpassble.parcelable.ErrorParcelable;
import gupuru.streetpassble.parcelable.DeviceData;

public class StreetPassReceiver extends BroadcastReceiver {

    private OnStreetPassReceiverListener onStreetPassReceiverListener;

    public StreetPassReceiver() {
    }

    public interface OnStreetPassReceiverListener {
        void onStreetPassScanResult(DeviceData deviceData);

        void onStreetPassAdvertiseResult(AdvertiseSuccessParcelable advertiseSuccessParcelable);

        void onStreetPassError(ErrorParcelable errorParcelable);
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
            AdvertiseSuccessParcelable advertiseSuccessParcelable
                    = (AdvertiseSuccessParcelable) intent.getExtras().get(Constants.ADV_DATA);
            if (advertiseSuccessParcelable != null) {
                onStreetPassReceiverListener.onStreetPassAdvertiseResult(advertiseSuccessParcelable);
            }
        } else if (Constants.ACTION_SCAN_ADV_ERROR.equals(action)) {
            ErrorParcelable errorParcelable
                    = (ErrorParcelable) intent.getExtras().get(Constants.ERROR_SCAN_ADV);
            if (errorParcelable != null) {
                onStreetPassReceiverListener.onStreetPassError(errorParcelable);
            }
        }
    }

}