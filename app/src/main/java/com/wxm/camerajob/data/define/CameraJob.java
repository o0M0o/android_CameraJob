package com.wxm.camerajob.data.define;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.IOException;
import java.sql.Timestamp;

import wxm.androidutil.DBHelper.IDBRow;
import wxm.androidutil.util.UtilFun;

/**
 * job for take picture
 * Created by wxm on 2016/6/11.
 */
@DatabaseTable(tableName = "tbCameraJob")
public class CameraJob
        implements Parcelable, Cloneable, IDBRow<Integer> {
    public final static String FIELD_ID = "_id";

    @DatabaseField(generatedId = true, columnName = "_id", dataType = DataType.INTEGER)
    private int _id;

    @DatabaseField(columnName = "name", canBeNull = false, dataType = DataType.STRING)
    private String Name;

    @DatabaseField(columnName = "type", canBeNull = false, dataType = DataType.STRING)
    private String Type;

    @DatabaseField(columnName = "point", canBeNull = false, dataType = DataType.STRING)
    private String Point;

    @DatabaseField(columnName = "status_id", foreign = true, foreignAutoCreate = true,
            foreignColumnName = CameraJobStatus.FIELD_ID,  canBeNull = false)
    private CameraJobStatus  Status;

    @DatabaseField(columnName = "endtime", canBeNull = false, dataType = DataType.TIME_STAMP)
    private Timestamp Endtime;

    @DatabaseField(columnName = "starttime", canBeNull = false, dataType = DataType.TIME_STAMP)
    private Timestamp Starttime;

    @DatabaseField(columnName = "ts",  dataType = DataType.TIME_STAMP)
    private Timestamp ts;

    public CameraJob()
    {
        setTs(new Timestamp(0));
        setStarttime(new Timestamp(0));
        setEndtime(new Timestamp(0));
        setStatus(new CameraJobStatus());

        setName("");
        setType("");
        setPoint("");
    }

    @Override
    public Object clone()  {
        CameraJob n = null;
        try {
            n = (CameraJob)super.clone();
            n.set_id(get_id());
            n.setName(getName());
            n.setStatus(getStatus());
            n.setType(getType());
            n.setPoint(getPoint());
            n.setEndtime(getEndtime());
            n.setStarttime(getStarttime());
            n.setTs(getTs());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return n;
    }

    @Override
    public String toString()
    {
        return String.format("name : %s, type : %s, point : %s, " +
                            "startdate : %s, enddate : %s, status : %s",
                getName(), getType(), getPoint(),
                getStarttime(), getEndtime(), getStatus().toString());
    }

    // for parcel
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(get_id());
        out.writeString(getName());
        out.writeString(getType());
        out.writeString(getPoint());
        out.writeLong(getStarttime().getTime());
        out.writeLong(getEndtime().getTime());
        out.writeLong(getTs().getTime());
        Status.writeToParcel(out, flags);
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

    public CameraJob(Parcel in)   {
        setTs(new Timestamp(0));
        setStarttime(new Timestamp(0));
        setEndtime(new Timestamp(0));

        set_id(in.readInt());
        setName(in.readString());
        setType(in.readString());
        setPoint(in.readString());
        getStarttime().setTime(in.readLong());
        getEndtime().setTime(in.readLong());
        getTs().setTime(in.readLong());
        setStatus(new CameraJobStatus(in));
    }

    // for json
    @SuppressWarnings("UnusedReturnValue")
    public boolean writeToJson(JsonWriter jw)   {
        try {
            jw.beginObject();
            jw.name("_id").value(get_id());
            jw.name("Name").value(getName());
            jw.name("Type").value(getType());
            jw.name("Point").value(getPoint());
            jw.name("Starttime").value(UtilFun.TimestampToString(getStarttime()));
            jw.name("Endtime").value(UtilFun.TimestampToString(getEndtime()));
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
                        ret.set_id(jr.nextInt());
                        break;
                    case "Name":
                        ret.setName(jr.nextString());
                        break;
                    case "Type":
                        ret.setType(jr.nextString());
                        break;
                    case "Point":
                        ret.setPoint(jr.nextString());
                        break;
                    case "Starttime":
                        ret.setStarttime(UtilFun.StringToTimestamp(jr.nextString()));
                        break;
                    case "Endtime":
                        ret.setEndtime(UtilFun.StringToTimestamp(jr.nextString()));
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

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getPoint() {
        return Point;
    }

    public void setPoint(String point) {
        Point = point;
    }

    public Timestamp getEndtime() {
        return Endtime;
    }

    public void setEndtime(Timestamp endtime) {
        Endtime = endtime;
    }

    public Timestamp getStarttime() {
        return Starttime;
    }

    public void setStarttime(Timestamp starttime) {
        Starttime = starttime;
    }

    public Timestamp getTs() {
        return ts;
    }

    public void setTs(Timestamp ts) {
        this.ts = ts;
    }

    public CameraJobStatus getStatus() {
        return Status;
    }

    public void setStatus(CameraJobStatus status) {
        Status = status;
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
