package gupuru.streetpassble.callback;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.AdvertiseSuccess;
import gupuru.streetpassble.parcelable.Error;

public class AdvertiseBle extends AdvertiseCallback {

    private Context context;

    public AdvertiseBle(Context context) {
        this.context = context;
    }

    @Override
    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        super.onStartSuccess(settingsInEffect);

        AdvertiseSuccess advertiseSuccess = new AdvertiseSuccess(
                settingsInEffect.getTxPowerLevel(),
                settingsInEffect.getMode(),
                settingsInEffect.getTimeout()
        );

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_ADV);
        intent.putExtra(Constants.ADV_DATA, advertiseSuccess);
        context.sendBroadcast(intent);
    }

    @Override
    public void onStartFailure(int errorCode) {
        super.onStartFailure(errorCode);
        String errorMessage = "";
        switch (errorCode) {
            case ADVERTISE_FAILED_ALREADY_STARTED:
                errorMessage = "既にAdvertiseを実行中です";
                break;
            case ADVERTISE_FAILED_DATA_TOO_LARGE:
                errorMessage = "Advertiseのメッセージが大きすぎます";
                break;
            case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                errorMessage = "Advertiseをサポートしていません";
                break;
            case ADVERTISE_FAILED_INTERNAL_ERROR:
                errorMessage = "内部エラーが発生しました";
                break;
            case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                errorMessage = "利用可能なAdvertiseのインスタンスが余っていません";
                break;
        }

        Error error = new Error(errorCode, errorMessage);

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_SCAN_ADV_ERROR);
        intent.putExtra(Constants.ERROR_SCAN_ADV, error);
        context.sendBroadcast(intent);
    }

}
