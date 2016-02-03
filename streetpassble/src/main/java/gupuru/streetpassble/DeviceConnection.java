package gupuru.streetpassble;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.ErrorParcelable;
import gupuru.streetpassble.reciver.StreetPassGattServerReceiver;

public class DeviceConnection implements StreetPassGattServerReceiver.OnStreetPassGattServerListener {

    private Context context;
    private StreetPassGattServerReceiver streetPassGattServerReceiver;
    private IntentFilter streetPassGattServerIntentFilter;

    private OnDeviceConnectionListener onDeviceConnectionListener;
    private OnDeviceCommunicationListener onDeviceCommunicationListener;

    public DeviceConnection(Context context) {
        this.context = context;
    }

    public interface OnDeviceConnectionListener {
        void deviceConnectInfo(DeviceData deviceData, boolean isConnect);

        void deviceConnectSendInfo(DeviceData deviceData);

        void deviceConnectSendReceiveData(String message);

        void deviceConnectOpenServer(boolean result);

        void deviceConnectError(ErrorParcelable errorParcelable);
    }

    public interface OnDeviceCommunicationListener {
        void deviceCommunicationReceiveData(String data);

        void deviceCommunicationSendData(String data);

        void deviceCommunicationConnected(boolean isConnected);

        void deviceCommunicationError(ErrorParcelable errorParcelable);
    }

    public void setOnStreetPassListener(OnDeviceConnectionListener onDeviceConnectionListener) {
        this.onDeviceConnectionListener = onDeviceConnectionListener;
    }

    public void setOnDeviceCommunicationListener(OnDeviceCommunicationListener onDeviceCommunicationListener) {
        this.onDeviceCommunicationListener = onDeviceCommunicationListener;
    }

    public void open() {
        //Receiver登録
        initStreetPassGattServerReceiver();
        context.registerReceiver(streetPassGattServerReceiver, streetPassGattServerIntentFilter);
    }

    public void close() {
        //broadcast送信
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_CLOSE_GATT);
        context.sendBroadcast(intent);
        //レシーバー解除
        unregisterStreetPassReceiver();
    }

    /**
     * 端末に接続する
     *
     * @param address
     */
    public void connectDevice(String address) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_CONNECT_DEVICE);
        intent.putExtra(Constants.DEVICE_ADDRESS, address);
        context.sendBroadcast(intent);
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

    private void initStreetPassGattServerReceiver() {
        streetPassGattServerReceiver = new StreetPassGattServerReceiver();
        streetPassGattServerReceiver.setOnStreetPassGattServerListener(this);
        streetPassGattServerIntentFilter = new IntentFilter();
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_GATT_SERVER_STATE_CHANGE);
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_GATT_SERVER_READ_REQUEST);
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_GATT_SERVER_WRITE_REQUEST);
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_GATT_SERVICE_ADDED);
        streetPassGattServerIntentFilter.addAction(Constants.ACTION_BLE_SERVER_CONNECTED);
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
            ErrorParcelable errorParcelable = new ErrorParcelable(Constants.CODE_UN_REGISTER_RECEIVER_ERROR, e.toString());
            onDeviceConnectionListener.deviceConnectError(errorParcelable);
        }
    }

    //region StreetPassGattServer callback

    @Override
    public void onStreetPassGattServerStateChange(DeviceData deviceData, boolean isConnect) {
        onDeviceConnectionListener.deviceConnectInfo(deviceData, isConnect);
    }

    @Override
    public void onStreetPassGattServerRead(DeviceData deviceData) {
        onDeviceConnectionListener.deviceConnectSendInfo(deviceData);
    }

    @Override
    public void onStreetPassGattServerWrite(String message) {
        onDeviceConnectionListener.deviceConnectSendReceiveData(message);
    }

    @Override
    public void onStreetPassServiceAdded(boolean result) {
        onDeviceConnectionListener.deviceConnectOpenServer(result);
    }

    //endregion

    //region BLEServer callback

    @Override
    public void onBLEServerRead(String data) {
        onDeviceCommunicationListener.deviceCommunicationReceiveData(data);
    }

    @Override
    public void onBLEServerWrite(String data) {
        onDeviceCommunicationListener.deviceCommunicationSendData(data);
    }

    @Override
    public void onBLEConnected(boolean result) {
        onDeviceCommunicationListener.deviceCommunicationConnected(result);
    }

    @Override
    public void onBLEServerError(ErrorParcelable errorParcelable) {
        onDeviceCommunicationListener.deviceCommunicationError(errorParcelable);
    }

    //endregion

}
