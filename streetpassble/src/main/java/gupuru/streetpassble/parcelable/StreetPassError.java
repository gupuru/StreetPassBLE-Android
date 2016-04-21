package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class StreetPassError implements Parcelable {

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

    public static final Parcelable.Creator<StreetPassError> CREATOR
            = new Parcelable.Creator<StreetPassError>() {
        public StreetPassError createFromParcel(Parcel in) {
            return new StreetPassError(in);
        }

        public StreetPassError[] newArray(int size) {
            return new StreetPassError[size];
        }
    };

    public StreetPassError(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    private StreetPassError(Parcel in) {
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