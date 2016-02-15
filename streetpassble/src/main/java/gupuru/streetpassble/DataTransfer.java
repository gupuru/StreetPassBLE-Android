package gupuru.streetpassble;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.parcelable.TransferData;
import gupuru.streetpassble.reciver.StreetPassGattServerReceiver;

public class DataTransfer implements StreetPassGattServerReceiver.OnStreetPassGattServerListener {

    private Context context;
    private StreetPassGattServerReceiver streetPassGattServerReceiver;
    private IntentFilter streetPassGattServerIntentFilter;

    private OnDataTransferListener onDataTransferListener;

    public DataTransfer(Context context) {
        this.context = context;
    }

    public interface OnDataTransferListener {
        void dataTransferSendMessage(TransferData message);

        void dataTransferReceiveMessage(TransferData message);

        void dataTransferError(Error error);
    }

    public void setOnDataTransferListener(OnDataTransferListener onDataTransferListener) {
        this.onDataTransferListener = onDataTransferListener;
    }

    /**
     * GATTサーバーたてる
     */
    public void open() {
        //Receiver登録
        initStreetPassGattServerReceiver();
        context.registerReceiver(streetPassGattServerReceiver, streetPassGattServerIntentFilter);
    }

    /**
     * GATTサーバー閉じる
     */
    public void close() {
        //レシーバー解除
        unregisterStreetPassReceiver();
        //broadcast送信
        updateBroadCast(Constants.ACTION_DISCONNECT_DEVICE);
        updateBroadCast(Constants.ACTION_CLOSE_GATT);
    }

    /**
     * 端末の接続を切断
     */
    public void disconnectDevice() {
        //broadcast送信
        updateBroadCast(Constants.ACTION_DISCONNECT_DEVICE);
    }

    /**
     * 端末にStringのデータを送信する
     *
     * @param data
     */
    public void sendDataToDevice(String data) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_SEND_DATA_TO_DEVICE);
        intent.putExtra(Constants.DATA, data);
        context.sendBroadcast(intent);
    }

    /**
     * BroadCast送信
     *
     * @param action
     */
    private void updateBroadCast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        context.sendBroadcast(intent);
    }

    /**
     * Receiver登録
     */
    private void initStreetPassGattServerReceiver() {
        streetPassGattServerReceiver = new StreetPassGattServerReceiver();
        streetPassGattServerReceiver.setOnStreetPassGattServerListener(this);
        streetPassGattServerIntentFilter = new IntentFilter();
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_GATT_SERVER_READ_REQUEST);
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_GATT_SERVER_WRITE_REQUEST);
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_BLE_SERVER_READ);
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_BLE_SERVER_WRITE);
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_BLE_SERVER_ERROR);
    }

    /**
     * StreetPassReceiver解除
     */
    private void unregisterStreetPassReceiver() {
        try {
            context.unregisterReceiver(streetPassGattServerReceiver);
            streetPassGattServerReceiver = null;
        } catch (IllegalArgumentException e) {
            Error error = new Error(Constants.CODE_UN_REGISTER_RECEIVER_ERROR, e.toString());
            if (onDataTransferListener != null) {
                onDataTransferListener.dataTransferError(error);
            }
        }
    }

    //region StreetPassGattServer callback

    @Override
    public void onStreetPassGattServerWrite(TransferData data) {
        if (onDataTransferListener != null) {
            onDataTransferListener.dataTransferReceiveMessage(data);
        }
    }

    //endregion

    //region BLEServer callback

    @Override
    public void onBLEServerRead(TransferData data) {
        if (onDataTransferListener != null) {
            onDataTransferListener.dataTransferReceiveMessage(data);
        }
    }

    @Override
    public void onBLEServerWrite(TransferData data) {
        if (onDataTransferListener != null) {
            onDataTransferListener.dataTransferSendMessage(data);
        }
    }

    @Override
    public void onBLEServerError(Error error) {
        if (onDataTransferListener != null) {
            onDataTransferListener.dataTransferError(error);
        }
    }

    //endregion

}
