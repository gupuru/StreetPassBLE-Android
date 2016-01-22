package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import gupuru.streetpassble.constants.Settings;

public class StreetPassSettings implements Parcelable {

    protected String uuid = Settings.SERVICE_UUID;
    protected String data = "";
    protected int scanMode = Settings.SCAN__MODE;
    protected int advertiseMode = Settings.ADVERTISE_MODE;
    protected int txPowerLevel = Settings.TX_POWER_LEVEL;
    protected int timeOut = Settings.TIME_OUT;
    protected boolean advertiseConnectable = Settings.ADVERTISE_CONNECTABLE;
    protected boolean advertiseIncludeDeviceName = Settings.ADVERTISE_INCLUDE_DEVICE_NAME;
    protected boolean advertiseIncludeTxPowerLevel = Settings.ADVERTISE_INCLUDE_TX_POWER_LEVEL;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(uuid);
        out.writeString(data);
        out.writeInt(scanMode);
        out.writeInt(advertiseMode);
        out.writeInt(txPowerLevel);
        out.writeInt(timeOut);
        out.writeByte((byte) (advertiseConnectable ? 1 : 0));
        out.writeByte((byte) (advertiseIncludeDeviceName ? 1 : 0));
        out.writeByte((byte) (advertiseIncludeTxPowerLevel ? 1 : 0));
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

    public StreetPassSettings(String uuid, String data, int scanMode, int advertiseMode, int txPowerLevel
            , int timeOut, boolean advertiseConnectable, boolean advertiseIncludeDeviceName, boolean advertiseIncludeTxPowerLevel) {
        this.uuid = uuid;
        this.data = data;
        this.scanMode = scanMode;
        this.advertiseMode = advertiseMode;
        this.txPowerLevel = txPowerLevel;
        this.timeOut = timeOut;
        this.advertiseConnectable = advertiseConnectable;
        this.advertiseIncludeDeviceName = advertiseIncludeDeviceName;
        this.advertiseIncludeTxPowerLevel = advertiseIncludeTxPowerLevel;
    }

    private StreetPassSettings(Parcel in) {
        uuid = in.readString();
        data = in.readString();
        scanMode = in.readInt();
        advertiseMode = in.readInt();
        txPowerLevel = in.readInt();
        timeOut = in.readInt();
        advertiseConnectable = in.readByte() != 0;
        advertiseIncludeDeviceName = in.readByte() != 0;
        advertiseIncludeTxPowerLevel = in.readByte() != 0;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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
