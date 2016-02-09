package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import gupuru.streetpassble.constants.Settings;

public class StreetPassSettings implements Parcelable {

    protected String serviceUuid = Settings.SERVICE_UUID;
    protected String characteristicUuid = Settings.CHARACTERISTIC_UUID;
    protected String data = "";
    protected String defaultResponseData = "";
    protected int scanMode = Settings.SCAN__MODE;
    protected int advertiseMode = Settings.ADVERTISE_MODE;
    protected int txPowerLevel = Settings.TX_POWER_LEVEL;
    protected int timeOut = Settings.TIME_OUT;
    protected boolean advertiseConnectable = Settings.ADVERTISE_CONNECTABLE;
    protected boolean advertiseIncludeDeviceName = Settings.ADVERTISE_INCLUDE_DEVICE_NAME;
    protected boolean advertiseIncludeTxPowerLevel = Settings.ADVERTISE_INCLUDE_TX_POWER_LEVEL;
    protected boolean sendDataMaxSize = Settings.SEND_DATA_MAX_SIZE;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(serviceUuid);
        out.writeString(characteristicUuid);
        out.writeString(data);
        out.writeString(defaultResponseData);
        out.writeInt(scanMode);
        out.writeInt(advertiseMode);
        out.writeInt(txPowerLevel);
        out.writeInt(timeOut);
        out.writeByte((byte) (advertiseConnectable ? 1 : 0));
        out.writeByte((byte) (advertiseIncludeDeviceName ? 1 : 0));
        out.writeByte((byte) (advertiseIncludeTxPowerLevel ? 1 : 0));
        out.writeByte((byte) (sendDataMaxSize ? 1 : 0));
    }

    public static final Parcelable.Creator<StreetPassSettings> CREATOR
            = new Parcelable.Creator<StreetPassSettings>() {
        public StreetPassSettings createFromParcel(Parcel in) {
            return new StreetPassSettings(in);
        }

        public StreetPassSettings[] newArray(int size) {
            return new StreetPassSettings[size];
        }
    };

    public StreetPassSettings() {
    }

    public StreetPassSettings(
            String serviceUuid, String characteristicUuid , String data, String defaultResponseData,
            int scanMode, int advertiseMode, int txPowerLevel, int timeOut, boolean advertiseConnectable,
            boolean advertiseIncludeDeviceName, boolean advertiseIncludeTxPowerLevel, boolean sendDataMaxSize
    ) {
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
        this.data = data;
        this.defaultResponseData = defaultResponseData;
        this.scanMode = scanMode;
        this.advertiseMode = advertiseMode;
        this.txPowerLevel = txPowerLevel;
        this.timeOut = timeOut;
        this.advertiseConnectable = advertiseConnectable;
        this.advertiseIncludeDeviceName = advertiseIncludeDeviceName;
        this.advertiseIncludeTxPowerLevel = advertiseIncludeTxPowerLevel;
        this.sendDataMaxSize = sendDataMaxSize;
    }

    private StreetPassSettings(Parcel in) {
        serviceUuid = in.readString();
        characteristicUuid = in.readString();
        data = in.readString();
        defaultResponseData = in.readString();
        scanMode = in.readInt();
        advertiseMode = in.readInt();
        txPowerLevel = in.readInt();
        timeOut = in.readInt();
        advertiseConnectable = in.readByte() != 0;
        advertiseIncludeDeviceName = in.readByte() != 0;
        advertiseIncludeTxPowerLevel = in.readByte() != 0;
        sendDataMaxSize = in.readByte() != 0;
    }

    public void setSendDataMaxSize(boolean sendDataMaxSize) {
        this.sendDataMaxSize = sendDataMaxSize;
    }

    public boolean isSendDataMaxSize() {
        return sendDataMaxSize;
    }

    public String getDefaultResponseData() {
        return defaultResponseData;
    }

    public void setDefaultResponseData(String defaultResponseData) {
        this.defaultResponseData = defaultResponseData;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getServiceUuid() {
        return serviceUuid;
    }

    public void setServiceUuid(String serviceUuid) {
        this.serviceUuid = serviceUuid;
    }

    public String getCharacteristicUuid() {
        return characteristicUuid;
    }

    public void setCharacteristicUuid(String characteristicUuid) {
        this.characteristicUuid = characteristicUuid;
    }

    public int getAdvertiseMode() {
        return advertiseMode;
    }

    public int getScanMode() {
        return scanMode;
    }

    public int getTxPowerLevel() {
        return txPowerLevel;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public boolean isAdvertiseConnectable() {
        return advertiseConnectable;
    }

    public boolean isAdvertiseIncludeDeviceName() {
        return advertiseIncludeDeviceName;
    }

    public boolean isAdvertiseIncludeTxPowerLevel() {
        return advertiseIncludeTxPowerLevel;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public void setAdvertiseConnectable(boolean advertiseConnectable) {
        this.advertiseConnectable = advertiseConnectable;
    }

    public void setAdvertiseIncludeDeviceName(boolean advertiseIncludeDeviceName) {
        this.advertiseIncludeDeviceName = advertiseIncludeDeviceName;
    }

    public void setAdvertiseIncludeTxPowerLevel(boolean advertiseIncludeTxPowerLevel) {
        this.advertiseIncludeTxPowerLevel = advertiseIncludeTxPowerLevel;
    }

    public void setAdvertiseMode(int advertiseMode) {
        this.advertiseMode = advertiseMode;
    }

    public void setScanMode(int scanMode) {
        this.scanMode = scanMode;
    }

    public void setTxPowerLevel(int txPowerLevel) {
        this.txPowerLevel = txPowerLevel;
    }

}
