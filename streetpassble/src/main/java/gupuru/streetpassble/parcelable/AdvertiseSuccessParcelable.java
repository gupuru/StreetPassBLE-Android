package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class AdvertiseSuccessParcelable implements Parcelable {

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

    public static final Parcelable.Creator<AdvertiseSuccessParcelable> CREATOR
            = new Parcelable.Creator<AdvertiseSuccessParcelable>() {
        public AdvertiseSuccessParcelable createFromParcel(Parcel in) {
            return new AdvertiseSuccessParcelable(in);
        }

        public AdvertiseSuccessParcelable[] newArray(int size) {
            return new AdvertiseSuccessParcelable[size];
        }
    };

    public AdvertiseSuccessParcelable(int txPowerLevel, int mode, int timeOut){
        this.txPowerLevel = txPowerLevel;
        this.mode = mode;
        this.timeOut = timeOut;
    }

    private AdvertiseSuccessParcelable(Parcel in) {
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
