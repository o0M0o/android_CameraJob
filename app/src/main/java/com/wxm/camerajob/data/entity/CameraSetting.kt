package com.wxm.camerajob.data.entity

import android.os.Parcel
import android.os.Parcelable
import android.util.JsonReader
import android.util.JsonWriter

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import wxm.androidutil.db.IDBRow
import wxm.androidutil.improve.doJudge

import java.io.IOException

/**
 * job for take picture
 * Created by wxm on 2016/6/11.
 */
@DatabaseTable(tableName = "tbCameraSetting")
class CameraSetting : Parcelable, Cloneable, IDBRow<Int> {
    @DatabaseField(generatedId = true, columnName = FIELD_ID, dataType = DataType.INTEGER)
    var _id: Int = 0

    @DatabaseField(columnName = "face", canBeNull = false, dataType = DataType.INTEGER)
    var face: Int = LENS_FACING_BACK

    @DatabaseField(columnName = "photoSize", canBeNull = false, dataType = DataType.STRING)
    var photoSize: String = ""

    @DatabaseField(columnName = "point", canBeNull = false, dataType = DataType.BOOLEAN)
    var autoFlash: Boolean = false

    @DatabaseField(columnName = "point", canBeNull = false, dataType = DataType.BOOLEAN)
    var autoFocus: Boolean = false

    constructor()

    public override fun clone(): Any {
        return (super.clone() as CameraSetting).let{
            it._id = _id
            it.face = face
            it.photoSize = photoSize
            it.autoFlash = autoFlash
            it.autoFocus = autoFocus

            it
        }
    }

    override fun toString(): String {
        return String.format(
                "id : %d, face : %d, photoSize : %s, autoFlash : %b, autofocus : %b",
                _id, face, photoSize, autoFlash, autoFocus)
    }

    // for parcel
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeInt(_id)
        out.writeInt(face)
        out.writeString(photoSize)
        out.writeInt(autoFlash.doJudge(1, 0))
        out.writeInt(autoFocus.doJudge(1, 0))
    }

    constructor(inSteam: Parcel) {
        _id = inSteam.readInt()
        face = inSteam.readInt()
        photoSize = inSteam.readString()
        autoFlash = (inSteam.readInt() == 1).doJudge(true, false)
        autoFocus = (inSteam.readInt() == 1).doJudge(true, false)
    }

    // for json
    fun writeToJson(jw: JsonWriter): Boolean {
        try {
            jw.beginObject()
            jw.name("_id").value(_id.toLong())
            jw.name("face").value(face)
            jw.name("photoSize").value(photoSize)
            jw.name("autoFlash").value(autoFlash)
            jw.name("autoFocus").value(autoFocus)
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

        const val LENS_FACING_BACK = 1
        const val LENS_FACING_FRONT = 0

        val CREATOR: Parcelable.Creator<CameraSetting> = object : Parcelable.Creator<CameraSetting> {
            override fun createFromParcel(inSteam: Parcel): CameraSetting {
                return CameraSetting(inSteam)
            }

            override fun newArray(size: Int): Array<CameraSetting> {
                return Array(size) { CameraSetting() }
            }
        }

        fun readFromJson(jr: JsonReader): CameraSetting? {
            var ret: CameraSetting? = null
            try {
                jr.beginObject()

                while (jr.hasNext()) {
                    if (null == ret)
                        ret = CameraSetting()

                    val name = jr.nextName()
                    when (name) {
                        "_id" -> ret._id = jr.nextInt()
                        "face" -> ret.face = jr.nextInt()
                        "photoSize" -> ret.photoSize = jr.nextString()
                        "autoFlash" -> ret.autoFlash = jr.nextBoolean()
                        "autoFocus" -> ret.autoFocus = jr.nextBoolean()
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
