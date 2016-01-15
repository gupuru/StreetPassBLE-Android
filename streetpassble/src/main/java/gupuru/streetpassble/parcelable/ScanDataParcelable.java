package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class ScanDataParcelable implements Parcelable {

    protected int callbackType;
    protected String deviceAddress;
    protected String deviceName;
    protected String uuid;
    protected double distance;
    protected String serviceData;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(callbackType);
        out.writeString(deviceAddress);
        out.writeString(deviceName);
        out.writeString(uuid);
        out.writeDouble(distance);
        out.writeString(serviceData);
    }

    public static final Parcelable.Creator<ScanDataParcelable> CREATOR
            = new Parcelable.Creator<ScanDataParcelable>() {
        public ScanDataParcelable createFromParcel(Parcel in) {
            return new ScanDataParcelable(in);
        }

        public ScanDataParcelable[] newArray(int size) {
            return new ScanDataParcelable[size];
        }
    };

    public ScanDataParcelable(int callbackType, String deviceAddress, String deviceName, String uuid, double distance, String serviceData) {
        this.callbackType = callbackType;
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.uuid = uuid;
        this.distance = distance;
        this.serviceData = serviceData;
    }

    private ScanDataParcelable(Parcel in) {
        callbackType = in.readInt();
        deviceAddress = in.readString();
        deviceName = in.readString();
        uuid = in.readString();
        distance = in.readDouble();
        serviceData = in.readString();
    }

    public double getDistance() {
        return distance;
    }

    public int getCallbackType() {
        return callbackType;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getServiceData() {
        return serviceData;
    }

    public String getUuid() {
        return uuid;
    }

}
