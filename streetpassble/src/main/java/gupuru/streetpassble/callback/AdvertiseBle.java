package gupuru.streetpassble.callback;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;

import gupuru.streetpassble.parcelable.AdvertiseSuccess;
import gupuru.streetpassble.parcelable.Error;

/**
 * Advertisingの結果 AdvertiseCallback
 */
public class AdvertiseBle extends AdvertiseCallback {

    private OnAdvertiseBleListener onAdvertiseBleListener;

    public AdvertiseBle() {

    }

    public void setOnAdvertiseBleListener(OnAdvertiseBleListener onAdvertiseBleListener) {
        this.onAdvertiseBleListener = onAdvertiseBleListener;
    }

    public interface OnAdvertiseBleListener {
        void onAdvertiseBleSuccess(AdvertiseSuccess advertiseSuccess);

        void onAdvertiseBleError(Error error);
    }

    @Override
    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        super.onStartSuccess(settingsInEffect);
        AdvertiseSuccess advertiseSuccess = new AdvertiseSuccess(
                settingsInEffect.getTxPowerLevel(),
                settingsInEffect.getMode(),
                settingsInEffect.getTimeout()
        );
        if (onAdvertiseBleListener != null) {
            onAdvertiseBleListener.onAdvertiseBleSuccess(advertiseSuccess);
        }
    }

    @Override
    public void onStartFailure(int errorCode) {
        super.onStartFailure(errorCode);
        String errorMessage = "";
        switch (errorCode) {
            case ADVERTISE_FAILED_ALREADY_STARTED:
                errorMessage = "ADVERTISE_FAILED_ALREADY_STARTED/既にAdvertiseを実行中です";
                break;
            case ADVERTISE_FAILED_DATA_TOO_LARGE:
                errorMessage = "ADVERTISE_FAILED_DATA_TOO_LARGE/Advertiseのメッセージが大きすぎます";
                break;
            case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                errorMessage = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED/Advertiseをサポートしていません";
                break;
            case ADVERTISE_FAILED_INTERNAL_ERROR:
                errorMessage = "ADVERTISE_FAILED_INTERNAL_ERROR/内部エラーが発生しました";
                break;
            case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                errorMessage = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS/利用可能なAdvertiseのインスタンスが余っていません";
                break;
        }
        Error error = new Error(errorCode, errorMessage);
        if (onAdvertiseBleListener != null) {
            onAdvertiseBleListener.onAdvertiseBleError(error);
        }
    }

}
