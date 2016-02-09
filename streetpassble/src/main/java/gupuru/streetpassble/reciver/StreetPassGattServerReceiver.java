package gupuru.streetpassble.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.TransferData;

/**
 * GATTサーバーのレシーバー BroadcastReceiver
 */
public class StreetPassGattServerReceiver extends BroadcastReceiver {

    private OnStreetPassGattServerListener onStreetPassGattServerListener;

    public StreetPassGattServerReceiver() {
    }

    public interface OnStreetPassGattServerListener {
        void onStreetPassGattServerWrite(TransferData data);

        void onBLEServerRead(TransferData data);

        void onBLEServerWrite(TransferData data);

        void onBLEServerError(Error error);
    }

    public void setOnStreetPassGattServerListener(OnStreetPassGattServerListener onStreetPassGattServerListener) {
        this.onStreetPassGattServerListener = onStreetPassGattServerListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Constants.ACTION_GATT_SERVER_WRITE_REQUEST.equals(action)) {
            TransferData transferData =  intent.getParcelableExtra(Constants.WRITE_REQUEST);
            if (transferData != null) {
                onStreetPassGattServerListener.onStreetPassGattServerWrite(transferData);
            }
        } else if (Constants.ACTION_BLE_SERVER_READ.equals(action)) {
            TransferData transferData =  intent.getParcelableExtra(Constants.BLE_SERVER_READ);
            if (transferData != null) {
                onStreetPassGattServerListener.onBLEServerRead(transferData);
            }
        } else if (Constants.ACTION_BLE_SERVER_WRITE.equals(action)) {
            TransferData transferData =  intent.getParcelableExtra(Constants.BLE_SERVER_WRITE);
            if (transferData != null) {
                onStreetPassGattServerListener.onBLEServerWrite(transferData);
            }
        } else if (Constants.ACTION_BLE_SERVER_ERROR.equals(action)) {
            onStreetPassGattServerListener.onBLEServerError(
                    (Error) intent.getExtras().get(Constants.BLE_SERVER_ERROR)
            );
        }
    }

}