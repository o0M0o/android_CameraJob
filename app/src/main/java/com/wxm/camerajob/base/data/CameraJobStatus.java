package com.wxm.camerajob.base.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 拍照任务状态
 * Created by 123 on 2016/6/16.
 */
public class CameraJobStatus  implements Parcelable {
    public int _id;
    public int camerjob_id;
    public String camerajob_status;
    public int camerajob_photo_count;
    public Timestamp ts;

    public CameraJobStatus()    {
        ts = new Timestamp(0);
        camerjob_id =  GlobalDef.INT_INVALID_ID;

        camerajob_status = GlobalDef.STR_CAMERAJOB_UNKNOWN;
        camerajob_photo_count = 0;

        _id = GlobalDef.INT_INVALID_ID;
    }

    @Override
    public String toString()    {
        String ret = String.format(
                "id : %d, camerajob_id : %d, camerajob_status : %s, camerajob_photo_count : %d, ts : %s"
                ,_id
                ,camerjob_id
                ,camerajob_status
                ,camerajob_photo_count
                ,ts.toString());

        return ret;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(_id);
        out.writeInt(camerjob_id);
        out.writeString(camerajob_status);
        out.writeInt(camerajob_photo_count);
        out.writeString(ts.toString());

    }

    public static final Parcelable.Creator<CameraJobStatus> CREATOR
            = new Parcelable.Creator<CameraJobStatus>() {
        public CameraJobStatus createFromParcel(Parcel in) {
            return new CameraJobStatus(in);
        }

        public CameraJobStatus[] newArray(int size) {
            return new CameraJobStatus[size];
        }
    };

    private CameraJobStatus(Parcel in)   {
        _id = in.readInt();
        camerjob_id = in.readInt();
        camerajob_status = in.readString();
        camerajob_photo_count = in.readInt();

        try {
            ts = new Timestamp(0);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(in.readString());
            ts.setTime(date.getTime());
        }
        catch (ParseException ex)
        {
            ts = new Timestamp(0);
        }
    }
}
