package com.wxm.camerajob.base;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * job
 * Created by wxm on 2016/6/11.
 */
public class CameraJob implements Parcelable {
    public int _id;
    public String job_name;
    public String job_type;
    public String job_point;
    public Timestamp ts;

    public CameraJob()
    {
        ts = new Timestamp(0);
        job_name = "";
        job_type = "";
        job_point = "";
    }

    @Override
    public String toString()
    {
        String ret = String.format("type : %s, name : %s, point : %s, timestamp : %s",
                job_type, job_name, job_point,
                ts.toString());
        return ret;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(_id);
        out.writeString(job_name);
        out.writeString(job_type);
        out.writeString(job_point);
        out.writeString(ts.toString());

    }

    public static final Parcelable.Creator<CameraJob> CREATOR
            = new Parcelable.Creator<CameraJob>() {
        public CameraJob createFromParcel(Parcel in) {
            return new CameraJob(in);
        }

        public CameraJob[] newArray(int size) {
            return new CameraJob[size];
        }
    };

    private CameraJob(Parcel in)   {
        _id = in.readInt();
        job_name = in.readString();
        job_type = in.readString();
        job_point = in.readString();

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
