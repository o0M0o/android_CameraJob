package com.wxm.camerajob.data.entity

import android.os.Parcel
import android.os.Parcelable
import android.util.JsonReader
import android.util.JsonWriter

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import com.wxm.camerajob.data.define.EJobStatus
import wxm.androidutil.db.IDBRow
import wxm.androidutil.time.toFullTag
import wxm.androidutil.time.toTimestamp
import wxm.androidutil.type.MySize

import java.io.IOException
import java.sql.Timestamp

/**
 * job for take picture
 * Created by wxm on 2016/6/11.
 */
@DatabaseTable(tableName = "tbCameraJob")
class CameraJob : Cloneable, IDBRow<Int> {
    @DatabaseField(generatedId = true, columnName = FIELD_ID, dataType = DataType.INTEGER)
    var _id: Int = 0

    @DatabaseField(columnName = "paraName", canBeNull = false, dataType = DataType.STRING)
    var name: String = ""

    @DatabaseField(columnName = "type", canBeNull = false, dataType = DataType.STRING)
    var type: String = ""

    @DatabaseField(columnName = "point", canBeNull = false, dataType = DataType.STRING)
    var point: String = ""

    @DatabaseField(columnName = "status", canBeNull = false, dataType = DataType.STRING)
    var status: String = EJobStatus.STOP.status

    @DatabaseField(columnName = "photo_count", dataType = DataType.INTEGER)
    var photoCount: Int = 0

    @DatabaseField(columnName = "face", canBeNull = false, dataType = DataType.INTEGER)
    var face: Int = CameraParam.LENS_FACING_BACK

    @DatabaseField(columnName = "photo_size", canBeNull = false, dataType = DataType.STRING)
    var photoSize: String = ""

    @DatabaseField(columnName = "auto_flash", canBeNull = false, dataType = DataType.BOOLEAN)
    var autoFlash: Boolean = false

    @DatabaseField(columnName = "auto_focus", canBeNull = false, dataType = DataType.BOOLEAN)
    var autoFocus: Boolean = false

    @DatabaseField(columnName = "end_time", canBeNull = false, dataType = DataType.TIME_STAMP)
    var endTime: Timestamp = Timestamp(System.currentTimeMillis())

    @DatabaseField(columnName = "start_time", canBeNull = false, dataType = DataType.TIME_STAMP)
    var startTime: Timestamp = Timestamp(System.currentTimeMillis())

    @DatabaseField(columnName = "last_photo_time", canBeNull = false, dataType = DataType.TIME_STAMP)
    var lastPhotoTime: Timestamp = Timestamp(System.currentTimeMillis())

    @DatabaseField(columnName = "ts", dataType = DataType.TIME_STAMP)
    var ts: Timestamp = Timestamp(System.currentTimeMillis())

    public override fun clone(): Any {
        return (super.clone() as CameraJob).let{
            it._id = _id
            it.name = name
            it.type = type
            it.point = point
            it.status = status
            it.photoCount = photoCount
            it.face = face
            it.photoSize = photoSize
            it.autoFlash = autoFlash
            it.autoFocus = autoFocus
            it.startTime = Timestamp(startTime.time)
            it.endTime = Timestamp(endTime.time)
            it.lastPhotoTime = Timestamp(lastPhotoTime.time)
            it.ts = ts

            it
        }
    }

    override fun toString(): String {
        return "CameraJob[name = $name, type = $type, point = $point, status = $status, " +
                "photoCount = $photoCount, face = $face, photoSize = $photoSize, " +
                "startTime = $startTime, endTime = $endTime, lastPhotoTime = $lastPhotoTime, ts = $ts]"
    }

    // for json
    fun writeToJson(jw: JsonWriter): Boolean {
        try {
            jw.beginObject()
            jw.name("_id").value(_id.toLong())
            jw.name("name").value(name)
            jw.name("type").value(type)
            jw.name("point").value(point)
            jw.name("startTime").value(startTime.toFullTag())
            jw.name("endTime").value(endTime.toFullTag())
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

    /**
     * get camera param for job
     * @return camera param
     */
    fun getCameraParam(): CameraParam   {
        val self = this
        return CameraParam(null).apply {
            mFace = self.face
            mPhotoSize = MySize.parseSize(self.photoSize)
            mAutoFlash = self.autoFlash
            mAutoFocus = self.autoFocus
        }
    }

    companion object {
        const val FIELD_ID = "_id"

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
                        "name" -> ret.name = jr.nextString()
                        "type" -> ret.type = jr.nextString()
                        "point" -> ret.point = jr.nextString()
                        "startTime" -> ret.startTime = jr.nextString().toTimestamp()
                        "endTime" -> ret.endTime = jr.nextString().toTimestamp()
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
