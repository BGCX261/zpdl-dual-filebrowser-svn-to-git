package zpdl.studio.api.dialog;

import android.os.Parcel;
import android.os.Parcelable;

public class ApiDialogButtonParam implements Parcelable {
    public int id;
    public String text;

    public ApiDialogButtonParam() {
    }

    public ApiDialogButtonParam(Parcel src) {
        id = src.readInt();
        text = src.readString();
    }

    public ApiDialogButtonParam(int id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(text);
    }

    public static final Parcelable.Creator<ApiDialogButtonParam> CREATOR = new Parcelable.Creator<ApiDialogButtonParam>() {
        @Override
        public ApiDialogButtonParam createFromParcel(Parcel source) {
            return new ApiDialogButtonParam(source);
        }

        @Override
        public ApiDialogButtonParam[] newArray(int size) {
            return new ApiDialogButtonParam[size];
        }

   };
}
