package com.wxm.camerajob.data.define

import android.os.Parcel
import android.os.Parcelable
import android.util.JsonReader
import android.util.JsonWriter

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

import java.io.IOException
import java.sql.Timestamp

import wxm.androidutil.DBHelper.IDBRow
import wxm.androidutil.util.UtilFun

/**
 * job for take picture
 * Created by wxm on 2016/6/11.
 */
@DatabaseTable(tableName = "tbCameraJob")
class CameraJob : Parcelable, Cloneable, IDBRow<Int> {

    @DatabaseField(generatedId = true, columnName = "_id", dataType = DataType.INTEGER)
    var _id: Int = 0

    @DatabaseField(columnName = "paraName", canBeNull = false, dataType = DataType.STRING)
    var name: String = ""

    @DatabaseField(columnName = "type", canBeNull = false, dataType = DataType.STRING)
    var type: String = ""

    @DatabaseField(columnName = "point", canBeNull = false, dataType = DataType.STRING)
    var point: String = ""

    @DatabaseField(columnName = "status_id", foreign = true, foreignAutoCreate = true,
            foreignColumnName = CameraJobStatus.FIELD_ID, canBeNull = false)
    var status: CameraJobStatus = CameraJobStatus()

    @DatabaseField(columnName = "endtime", canBeNull = false, dataType = DataType.TIME_STAMP)
    var endtime: Timestamp = Timestamp(System.currentTimeMillis())

    @DatabaseField(columnName = "starttime", canBeNull = false, dataType = DataType.TIME_STAMP)
    var starttime: Timestamp = Timestamp(System.currentTimeMillis())

    @DatabaseField(columnName = "ts", dataType = DataType.TIME_STAMP)
    var ts: Timestamp = Timestamp(System.currentTimeMillis())

    constructor()

    public override fun clone(): Any {
        return (super.clone() as CameraJob).let{
            it._id = _id
            it.name = name
            it.status = status
            it.type = type
            it.point = point
            it.endtime = endtime
            it.starttime = starttime
            it.ts = ts

            it
        }
    }

    override fun toString(): String {
        return String.format("paraName : %s, type : %s, point : %s, startDate : %s, endDate : %s, status : %s",
                name, type, point,
                starttime, endtime, status.toString())
    }

    // for parcel
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(_id)
        out.writeString(name)
        out.writeString(type)
        out.writeString(point)
        out.writeLong(starttime.time)
        out.writeLong(endtime.time)
        out.writeLong(ts.time)
        status.writeToParcel(out, flags)
    }

    constructor(inSteam: Parcel) {
        ts = Timestamp(0)
        starttime = Timestamp(0)
        endtime = Timestamp(0)

        _id = inSteam.readInt()
        name = inSteam.readString()
        type = inSteam.readString()
        point = inSteam.readString()
        starttime.time = inSteam.readLong()
        endtime.time = inSteam.readLong()
        ts.time = inSteam.readLong()
        status = CameraJobStatus(inSteam)
    }

    // for json
    fun writeToJson(jw: JsonWriter): Boolean {
        try {
            jw.beginObject()
            jw.name("_id").value(_id.toLong())
            jw.name("Name").value(name)
            jw.name("Type").value(type)
            jw.name("Point").value(point)
            jw.name("Starttime").value(UtilFun.TimestampToString(starttime))
            jw.name("Endtime").value(UtilFun.TimestampToString(endtime))
            jw.endObject()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    override fun getID(): Int {
        return _id
    }

    override fun setID(integer: Int) {
        _id = integer
    }

    companion object {
        const val FIELD_ID = "_id"

        val CREATOR: Parcelable.Creator<CameraJob> = object : Parcelable.Creator<CameraJob> {
            override fun createFromParcel(inSteam: Parcel): CameraJob {
                return CameraJob(inSteam)
            }

            override fun newArray(size: Int): Array<CameraJob> {
                return Array(size, {CameraJob()})
            }
        }

        fun readFromJson(jr: JsonReader): CameraJob? {
            var ret: CameraJob? = null
            try {
                jr.beginObject()

                while (jr.hasNext()) {
                    if (null == ret)
                        ret = CameraJob()

                    val name = jr.nextName()
                    when (name) {
                        "_id" -> ret._id = jr.nextInt()
                        "Name" -> ret.name = jr.nextString()
                        "Type" -> ret.type = jr.nextString()
                        "Point" -> ret.point = jr.nextString()
                        "Starttime" -> ret.starttime = UtilFun.StringToTimestamp(jr.nextString())
                        "Endtime" -> ret.endtime = UtilFun.StringToTimestamp(jr.nextString())
                        else -> jr.skipValue()
                    }
                }

                jr.endObject()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return ret
        }
    }
}
