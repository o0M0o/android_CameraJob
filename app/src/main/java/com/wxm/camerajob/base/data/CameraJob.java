package com.wxm.camerajob.base.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.wxm.camerajob.base.utility.UtilFun;

import java.io.IOException;
import java.sql.Timestamp;

/**
 * job
 * Created by wxm on 2016/6/11.
 */
public class CameraJob
        implements Parcelable {
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
        job_starttime = new Timestamp(0);
        job_endtime = new Timestamp(0);

        job_name = "";
        job_type = "";
        job_point = "";
    }

    @Override
    public String toString()
    {
//        String ret = String.format("type : %s, name : %s, point : %s, timestamp : %s",
//                job_type, job_name, job_point,
//                ts.toString());

        return String.format("name : %s, type : %s, point : %s, " +
                            "startdate : %s, enddate : %s",
                            job_name, job_type, job_point, job_starttime, job_endtime);
    }

    // for parcel
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
        ts = new Timestamp(0);
        job_starttime = new Timestamp(0);
        job_endtime = new Timestamp(0);

        _id = in.readInt();
        job_name = in.readString();
        job_type = in.readString();
        job_point = in.readString();
        job_starttime.setTime(in.readLong());
        job_endtime.setTime(in.readLong());
        ts.setTime(in.readLong());
    }

    // for json
    @SuppressWarnings("UnusedReturnValue")
    public boolean writeToJson(JsonWriter jw)   {
        try {
            jw.beginObject();
            jw.name("_id").value(_id);
            jw.name("job_name").value(job_name);
            jw.name("job_type").value(job_type);
            jw.name("job_point").value(job_point);
            jw.name("job_starttime").value(UtilFun.TimestampToString(job_starttime));
            jw.name("job_endtime").value(UtilFun.TimestampToString(job_endtime));
            jw.endObject();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static CameraJob readFromJson(JsonReader jr)  {
        CameraJob ret = null;
        try {
            jr.beginObject();

            while (jr.hasNext())    {
                if(null == ret)
                    ret = new CameraJob();

                String name = jr.nextName();
                switch (name) {
                    case "_id":
                        ret._id = jr.nextInt();
                        break;
                    case "job_name":
                        ret.job_name = jr.nextString();
                        break;
                    case "job_type":
                        ret.job_type = jr.nextString();
                        break;
                    case "job_point":
                        ret.job_point = jr.nextString();
                        break;
                    case "job_starttime":
                        ret.job_starttime = UtilFun.StringToTimestamp(jr.nextString());
                        break;
                    case "job_endtime":
                        ret.job_endtime = UtilFun.StringToTimestamp(jr.nextString());
                        break;
                    default:
                        jr.skipValue();
                        break;
                }
            }

            jr.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
