package com.wxm.camerajob.data.define;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.wxm.andriodutillib.DBHelper.IDBRow;

/**
 * 拍照任务状态
 * Created by 123 on 2016/6/16.
 */
@DatabaseTable(tableName = "tbCameraJobStatus")
public class CameraJobStatus
        implements Parcelable, IDBRow<Integer> {
    public final static String FIELD_ID = "_id";

    @DatabaseField(generatedId = true, columnName = "_id", dataType = DataType.INTEGER)
    private int _id;

    @DatabaseField(columnName = "job_status", dataType = DataType.STRING)
    private String job_status;

    @DatabaseField(columnName = "job_photo_count", dataType = DataType.INTEGER)
    private int job_photo_count;

    @DatabaseField(columnName = "ts",  dataType = DataType.TIME_STAMP)
    private Timestamp ts;

    public CameraJobStatus()    {
        setTs(new Timestamp(0));
        setJob_status(GlobalDef.STR_CAMERAJOB_UNKNOWN);
        setJob_photo_count(0);

        set_id(0);
    }

    public CameraJobStatus Clone()  {
        CameraJobStatus n = new CameraJobStatus();
        n.set_id(get_id());
        n.setJob_status(getJob_status());
        n.setJob_photo_count(getJob_photo_count());
        n.setTs(getTs());

        return n;
    }

    @Override
    public String toString()    {
        String ret = String.format(Locale.CHINA,
                "id : %d, job_status : %s, job_photo_count : %d, ts : %s"
                , get_id()
                , getJob_status()
                , getJob_photo_count()
                , getTs().toString());

        return ret;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(get_id());
        out.writeString(getJob_status());
        out.writeInt(getJob_photo_count());
        out.writeString(getTs().toString());

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

    public CameraJobStatus(Parcel in)   {
        set_id(in.readInt());
        setJob_status(in.readString());
        setJob_photo_count(in.readInt());

        try {
            setTs(new Timestamp(0));

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            Date date = format.parse(in.readString());
            getTs().setTime(date.getTime());
        }
        catch (ParseException ex)
        {
            setTs(new Timestamp(0));
        }
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getJob_status() {
        return job_status;
    }

    public void setJob_status(String job_status) {
        this.job_status = job_status;
    }

    public int getJob_photo_count() {
        return job_photo_count;
    }

    public void setJob_photo_count(int job_photo_count) {
        this.job_photo_count = job_photo_count;
    }

    public Timestamp getTs() {
        return ts;
    }

    public void setTs(Timestamp ts) {
        this.ts = ts;
    }

    @Override
    public Integer getID() {
        return get_id();
    }

    @Override
    public void setID(Integer integer) {
        set_id(integer);
    }
}
