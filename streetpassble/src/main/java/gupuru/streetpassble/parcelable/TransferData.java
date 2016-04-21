package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class TransferData implements Parcelable {

    protected String data;
    protected String deviceAddress;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(data);
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

    public TransferData(String deviceAddress, String data) {
        this.data = data;
        this.deviceAddress = deviceAddress;
    }

    private TransferData(Parcel in) {
        data = in.readString();
        deviceAddress = in.readString();
    }

    public String getData() {
        return data;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setData(String message) {
        this.data = data;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

}
