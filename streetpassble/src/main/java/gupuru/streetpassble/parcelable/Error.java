package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class Error implements Parcelable {

    protected int errorCode;
    protected String errorMessage;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(errorCode);
        out.writeString(errorMessage);
    }

    public static final Parcelable.Creator<Error> CREATOR
            = new Parcelable.Creator<Error>() {
        public Error createFromParcel(Parcel in) {
            return new Error(in);
        }

        public Error[] newArray(int size) {
            return new Error[size];
        }
    };

    public Error(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    private Error(Parcel in) {
        errorCode = in.readInt();
        errorMessage = in.readString();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}