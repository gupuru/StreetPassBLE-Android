package gupuru.streetpassble.reciver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.AdvertiseSuccessParcelable;
import gupuru.streetpassble.parcelable.ErrorParcelable;
import gupuru.streetpassble.parcelable.ScanDataParcelable;

public class StreetPassReceiver extends BroadcastReceiver {

    private OnStreetPassReceiverListener onStreetPassReceiverListener;

    public StreetPassReceiver() {
    }

    public interface OnStreetPassReceiverListener {
        void scanResult(ScanDataParcelable scanDataParcelable);

        void advertiseResult(AdvertiseSuccessParcelable advertiseSuccessParcelable);

        void error(ErrorParcelable errorParcelable);
    }

    public void setOnStreetPassReceiverListener(OnStreetPassReceiverListener onStreetPassReceiverListener) {
        this.onStreetPassReceiverListener = onStreetPassReceiverListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.ACTION_SCAN.equals(action)) {
            ScanDataParcelable scanDataParcelable
                    = (ScanDataParcelable) intent.getExtras().get(Constants.SCAN_DATA);
            if (scanDataParcelable != null) {
                onStreetPassReceiverListener.scanResult(scanDataParcelable);
            }
        } else if (Constants.ACTION_ADV.equals(action)) {
            AdvertiseSuccessParcelable advertiseSuccessParcelable
                    = (AdvertiseSuccessParcelable) intent.getExtras().get(Constants.ADV_DATA);
            if (advertiseSuccessParcelable != null) {
                onStreetPassReceiverListener.advertiseResult(advertiseSuccessParcelable);
            }
        } else if (Constants.ACTION_SCAN_ADV_ERROR.equals(action)) {
            ErrorParcelable errorParcelable
                    = (ErrorParcelable) intent.getExtras().get(Constants.ERROR_SCAN_ADV);
            if (errorParcelable != null) {
                onStreetPassReceiverListener.error(errorParcelable);
            }
        }
    }

}