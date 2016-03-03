package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import java.util.UUID;

import gupuru.streetpassble.constants.Settings;

public class StreetPassSettings implements Parcelable {

    protected String serviceUuid;
    protected String writeCharacteristicUuid;
    protected String readCharacteristicUuid;
    protected String data;
    protected int scanMode;
    protected int advertiseMode;
    protected int txPowerLevel;
    protected int timeOut;
    protected boolean advertiseConnectable;
    protected boolean advertiseIncludeDeviceName;
    protected boolean advertiseIncludeTxPowerLevel;
    protected boolean sendDataMaxSize;

    public static class Builder {
        protected String serviceUuid = Settings.SERVICE_UUID;
        protected String writeCharacteristicUuid = Settings.WRITE_CHARACTERISTIC_UUID;
        protected String readCharacteristicUuid = Settings.READ_CHARACTERISTIC_UUID;
        protected String data = "";
        protected int scanMode = Settings.SCAN__MODE;
        protected int advertiseMode = Settings.ADVERTISE_MODE;
        protected int txPowerLevel = Settings.TX_POWER_LEVEL;
        protected int timeOut = Settings.TIME_OUT;
        protected boolean advertiseConnectable = Settings.ADVERTISE_CONNECTABLE;
        protected boolean advertiseIncludeDeviceName = Settings.ADVERTISE_INCLUDE_DEVICE_NAME;
        protected boolean advertiseIncludeTxPowerLevel = Settings.ADVERTISE_INCLUDE_TX_POWER_LEVEL;
        protected boolean sendDataMaxSize = Settings.SEND_DATA_MAX_SIZE;

        public Builder serviceUuid(String serviceUuid) { this.serviceUuid = serviceUuid; return this; }
        public Builder writeCharacteristicUuid(String writeCharacteristicUuid) { this.writeCharacteristicUuid = writeCharacteristicUuid; return this; }
        public Builder readCharacteristicUuid(String readCharacteristicUuid) { this.readCharacteristicUuid = readCharacteristicUuid; return this; }
        public Builder data(String data) { this.data = data; return this; }
        public Builder scanMode(int scanMode) { this.scanMode = scanMode; return this; }
        public Builder advertiseMode(int advertiseMode) { this.advertiseMode = advertiseMode; return this; }
        public Builder txPowerLevel(int txPowerLevel) { this.txPowerLevel = txPowerLevel; return this; }
        public Builder timeOut(int timeOut) { this.timeOut = timeOut; return this; }
        public Builder advertiseConnectable(boolean advertiseConnectable) { this.advertiseConnectable = advertiseConnectable; return this; }
        public Builder advertiseIncludeDeviceName(boolean advertiseIncludeDeviceName) { this.advertiseIncludeDeviceName = advertiseIncludeDeviceName; return this; }
        public Builder advertiseIncludeTxPowerLevel(boolean advertiseIncludeTxPowerLevel) { this.advertiseIncludeTxPowerLevel = advertiseIncludeTxPowerLevel; return this; }
        public Builder sendDataMaxSize(boolean sendDataMaxSize) { this.sendDataMaxSize = sendDataMaxSize; return this; }

        public StreetPassSettings build() {
            return new StreetPassSettings(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(serviceUuid);
        out.writeString(writeCharacteristicUuid);
        out.writeString(readCharacteristicUuid);
        out.writeString(data);
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

    private StreetPassSettings(Builder builder) {
        this.serviceUuid = builder.serviceUuid;
        this.writeCharacteristicUuid = builder.writeCharacteristicUuid;
        this.readCharacteristicUuid = builder.readCharacteristicUuid;
        this.data = builder.data;
        this.scanMode = builder.scanMode;
        this.advertiseMode = builder.advertiseMode;
        this.txPowerLevel = builder.txPowerLevel;
        this.timeOut = builder.timeOut;
        this.advertiseConnectable = builder.advertiseConnectable;
        this.advertiseIncludeDeviceName = builder.advertiseIncludeDeviceName;
        this.advertiseIncludeTxPowerLevel = builder.advertiseIncludeTxPowerLevel;
        this.sendDataMaxSize = builder.sendDataMaxSize;
    }

    private StreetPassSettings(Parcel in) {
        serviceUuid = in.readString();
        readCharacteristicUuid = in.readString();
        writeCharacteristicUuid = in.readString();
        data = in.readString();
        scanMode = in.readInt();
        advertiseMode = in.readInt();
        txPowerLevel = in.readInt();
        timeOut = in.readInt();
        advertiseConnectable = in.readByte() != 0;
        advertiseIncludeDeviceName = in.readByte() != 0;
        advertiseIncludeTxPowerLevel = in.readByte() != 0;
        sendDataMaxSize = in.readByte() != 0;
    }

    public boolean isSendDataMaxSize() {
        return sendDataMaxSize;
    }

    public String getData() {
        return data;
    }

    public ParcelUuid getServiceUuid() {
        return new ParcelUuid(UUID.fromString(serviceUuid));
    }

    public ParcelUuid getReadCharacteristicUuid() {
        return new ParcelUuid(UUID.fromString(readCharacteristicUuid));
    }

    public ParcelUuid getWriteCharacteristicUuid() {
        return new ParcelUuid(UUID.fromString(writeCharacteristicUuid));
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

}
