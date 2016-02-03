package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import gupuru.streetpassble.constants.Settings;

public class DeviceConnectionSettings implements Parcelable {

    protected String serviceUuid = Settings.SERVICE_UUID;
    protected String characteristicUuid = Settings.CHARACTERISTIC_UUID;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(serviceUuid);
        out.writeString(characteristicUuid);
    }

    public static final Parcelable.Creator<DeviceConnectionSettings> CREATOR
            = new Parcelable.Creator<DeviceConnectionSettings>() {
        public DeviceConnectionSettings createFromParcel(Parcel in) {
            return new DeviceConnectionSettings(in);
        }

        public DeviceConnectionSettings[] newArray(int size) {
            return new DeviceConnectionSettings[size];
        }
    };

    public DeviceConnectionSettings() {
    }

    public DeviceConnectionSettings(String serviceUuid, String characteristicUuid) {
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
    }

    private DeviceConnectionSettings(Parcel in) {
        serviceUuid = in.readString();
        characteristicUuid = in.readString();
    }

    public String getCharacteristicUuid() {
        return characteristicUuid;
    }

    public String getServiceUuid() {
        return serviceUuid;
    }

}
