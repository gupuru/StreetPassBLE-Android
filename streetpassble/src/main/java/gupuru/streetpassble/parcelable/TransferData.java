package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class TransferData implements Parcelable {

    protected String message;
    protected String deviceAddress;
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(message);
        out.writeString(deviceAddress);
    }

    public static final Parcelable.Creator<TransferData> CREATOR
            = new Parcelable.Creator<TransferData>() {
        public TransferData createFromParcel(Parcel in) {
            return new TransferData(in);
        }

        public TransferData[] newArray(int size) {
            return new TransferData[size];
        }
    };

    public TransferData(String deviceAddress, String message) {
        this.message = message;
        this.deviceAddress = deviceAddress;
    }

    private TransferData(Parcel in) {
        message = in.readString();
        deviceAddress = in.readString();
    }

    public String getMessage() {
        return message;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

}
