package com.wxm.camerajob.data.entity

import android.os.Parcel
import android.os.Parcelable

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import com.wxm.camerajob.data.define.EJobStatus
import wxm.androidutil.db.IDBRow

import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale


/**
 * status for camera job
 * Created by wxm on 2016/6/16.
 */
@DatabaseTable(tableName = "tbCameraJobStatus")
class CameraJobStatus : Parcelable, Cloneable, IDBRow<Int> {
    @DatabaseField(generatedId = true, columnName = "_id", dataType = DataType.INTEGER)
    var _id: Int = 0

    @DatabaseField(columnName = "job_status", dataType = DataType.STRING)
    var job_status: String? = null

    @DatabaseField(columnName = "job_photo_count", dataType = DataType.INTEGER)
    var job_photo_count: Int = 0

    @DatabaseField(columnName = "ts", dataType = DataType.TIME_STAMP)
    var ts: Timestamp = Timestamp(System.currentTimeMillis())

    constructor() {
        job_status = EJobStatus.UNKNOWN.status
        job_photo_count = 0

        _id = 0
    }

    public override fun clone(): Any {
        return (super.clone() as CameraJobStatus).let {
            it._id = _id
            it.job_status = job_status
            it.job_photo_count = job_photo_count
            it.ts = ts

            it
        }
    }

    override fun toString(): String {
        return String.format(Locale.CHINA,
                "id : %d, job_status : %s, job_photo_count : %d, ts : %s",
                _id, job_status, job_photo_count, ts.toString())
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(_id)
        out.writeString(job_status)
        out.writeInt(job_photo_count)
        out.writeString(ts.toString())
    }

    constructor(inSteam: Parcel) {
        _id = inSteam.readInt()
        job_status = inSteam.readString()
        job_photo_count = inSteam.readInt()

        try {
            ts = Timestamp(0)

            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).let {
                ts.time = it.parse(inSteam.readString()).time
            }
        } catch (ex: ParseException) {
            ts = Timestamp(0)
        }
    }

    override fun getID(): Int {
        return _id
    }

    override fun setID(integer: Int) {
        _id = integer
    }

    companion object {
        const val FIELD_ID = "_id"


        val CREATOR: Parcelable.Creator<CameraJobStatus> = object : Parcelable.Creator<CameraJobStatus> {
            override fun createFromParcel(inSteam : Parcel): CameraJobStatus {
                return CameraJobStatus(inSteam)
            }

            override fun newArray(size: Int): Array<CameraJobStatus> {
                return Array(size) { CameraJobStatus() }
            }
        }
    }
}
