package gupuru.streetpassble;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import gupuru.streetpassble.constants.Constants;
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.Error;
import gupuru.streetpassble.reciver.ConnectDeviceReceiver;

public class ConnectDevice implements ConnectDeviceReceiver.OnConnectDeviceReceiverListener {

    private Context context;
    private ConnectDeviceReceiver connectDeviceReceiver;

    private OnConnectDeviceListener onConnectDeviceListener;

    public ConnectDevice(Context context) {
        this.context = context;
    }

    public interface OnConnectDeviceListener {
        void onDeviceConnectResult(DeviceData deviceData, boolean isConnect);

        void onOpenServer(boolean result);

        void onDeviceConnectError(Error error);

        void deviceCommunicationConnected(boolean isConnected);
    }

    public void setOnConnectDeviceListener(OnConnectDeviceListener onConnectDeviceListener) {
        this.onConnectDeviceListener = onConnectDeviceListener;
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

    public void start() {
        //Receiver登録
        register();
    }

    public void stop() {
        //レシーバー解除
        unregister();
    }

    /**
     * Receiver登録
     */
    private void register() {
        connectDeviceReceiver = new ConnectDeviceReceiver();
        connectDeviceReceiver.setOnConnectDeviceReceiverListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_GATT_SERVICE_ADDED);
        intentFilter.addAction(Constants.ACTION_GATT_SERVER_STATE_CHANGE);
        intentFilter.addAction(Constants.ACTION_BLE_SERVER_CONNECTED);
        context.registerReceiver(connectDeviceReceiver, intentFilter);
    }

    /**
     * Receiver解除
     */
    private void unregister() {
        try {
            context.unregisterReceiver(connectDeviceReceiver);
            connectDeviceReceiver = null;
        } catch (IllegalArgumentException e) {
            Error error = new Error(Constants.CODE_UN_REGISTER_RECEIVER_ERROR, e.toString());
            onConnectDeviceListener.onDeviceConnectError(error);
        }
    }

    @Override
    public void onStreetPassServiceAdded(boolean result) {
        onConnectDeviceListener.onOpenServer(result);
    }

    @Override
    public void onStreetPassGattServerStateChange(DeviceData deviceData, boolean isConnect) {
        onConnectDeviceListener.onDeviceConnectResult(deviceData, isConnect);
    }

    @Override
    public void onBLEConnected(boolean result) {
        onConnectDeviceListener.deviceCommunicationConnected(result);
    }

}
