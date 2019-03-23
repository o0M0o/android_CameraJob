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
class CameraSetting : Cloneable, IDBRow<Int> {
    @DatabaseField(generatedId = true, columnName = FIELD_ID, dataType = DataType.INTEGER)
    var _id: Int = 0

    @DatabaseField(columnName = "face", canBeNull = false, dataType = DataType.INTEGER)
    var face: Int = LENS_FACING_BACK

    @DatabaseField(columnName = "photoSize", canBeNull = false, dataType = DataType.STRING)
    var photoSize: String = ""

    @DatabaseField(columnName = "autoFlash", canBeNull = false, dataType = DataType.BOOLEAN)
    var autoFlash: Boolean = false

    @DatabaseField(columnName = "autoFocus", canBeNull = false, dataType = DataType.BOOLEAN)
    var autoFocus: Boolean = false

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

    override fun getID(): Int {
        return _id
    }

    override fun setID(integer: Int) {
        _id = integer
    }

    companion object {
        const val FIELD_ID = "_id"

        const val LENS_FACING_BACK = 1

    }
}
