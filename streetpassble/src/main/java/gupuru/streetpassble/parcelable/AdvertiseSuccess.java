package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class AdvertiseSuccess implements Parcelable {

    protected int txPowerLevel;
    protected int mode;
    protected int timeOut;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(txPowerLevel);
        out.writeInt(mode);
        out.writeInt(timeOut);
    }

    public static final Parcelable.Creator<AdvertiseSuccess> CREATOR
            = new Parcelable.Creator<AdvertiseSuccess>() {
        public AdvertiseSuccess createFromParcel(Parcel in) {
            return new AdvertiseSuccess(in);
        }

        public AdvertiseSuccess[] newArray(int size) {
            return new AdvertiseSuccess[size];
        }
    };

    public AdvertiseSuccess(int txPowerLevel, int mode, int timeOut){
        this.txPowerLevel = txPowerLevel;
        this.mode = mode;
        this.timeOut = timeOut;
    }

    private AdvertiseSuccess(Parcel in) {
        txPowerLevel = in.readInt();
        mode = in.readInt();
        timeOut = in.readInt();
    }

    public int getMode() {
        return mode;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public int getTxPowerLevel() {
        return txPowerLevel;
    }

}
