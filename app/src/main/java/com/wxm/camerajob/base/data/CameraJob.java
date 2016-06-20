package com.wxm.camerajob.base.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Timestamp;

/**
 * job
 * Created by wxm on 2016/6/11.
 */
public class CameraJob implements Parcelable {
    public int _id;
    public String job_name;
    public String job_type;
    public String job_point;
    public Timestamp job_endtime;
    public Timestamp job_starttime;
    public Timestamp ts;

    public CameraJob()
    {
        ts = new Timestamp(0);
        job_name = "";
        job_type = "";
        job_point = "";

        job_starttime = new Timestamp(0);
        job_endtime = new Timestamp(0);
    }

    @Override
    public String toString()
    {
//        String ret = String.format("type : %s, name : %s, point : %s, timestamp : %s",
//                job_type, job_name, job_point,
//                ts.toString());

        String ret = String.format("name : %s, type : %s, point : %s",
                            job_name, job_type, job_point);
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
        out.writeLong(job_starttime.getTime());
        out.writeLong(job_endtime.getTime());
        out.writeLong(ts.getTime());
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
        job_starttime.setTime(in.readLong());
        job_endtime.setTime(in.readLong());
        ts.setTime(in.readLong());
    }
}
