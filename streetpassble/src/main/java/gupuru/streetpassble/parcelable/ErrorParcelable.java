package gupuru.streetpassble.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class ErrorParcelable implements Parcelable {

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

    public static final Parcelable.Creator<ErrorParcelable> CREATOR
            = new Parcelable.Creator<ErrorParcelable>() {
        public ErrorParcelable createFromParcel(Parcel in) {
            return new ErrorParcelable(in);
        }

        public ErrorParcelable[] newArray(int size) {
            return new ErrorParcelable[size];
        }
    };

    public ErrorParcelable(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    private ErrorParcelable(Parcel in) {
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