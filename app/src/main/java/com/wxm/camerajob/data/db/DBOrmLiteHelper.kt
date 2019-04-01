package com.wxm.camerajob.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.RuntimeExceptionDao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.wxm.camerajob.data.entity.CameraJob
import wxm.androidutil.log.TagLog

import java.sql.SQLException

/**
 * sqlite helper from app
 * Created by wxm on 2016/8/12.
 */
class DBOrmLiteHelper(context: Context) : OrmLiteSqliteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    val cameraJobREDao: RuntimeExceptionDao<CameraJob, Int> = getRuntimeExceptionDao(CameraJob::class.java)

    override fun onCreate(db: SQLiteDatabase, connectionSource: ConnectionSource) {
        createAndInitTable()
    }

    override fun onUpgrade(database: SQLiteDatabase,
                           connectionSource: ConnectionSource, oldVersion: Int, newVersion: Int) {
        when (newVersion) {
            8 -> {
                try {
                    TableUtils.dropTable<CameraJob, Any>(connectionSource, CameraJob::class.java, false)
                } catch (e: SQLException) {
                    TagLog.e("Can't create database", e)
                    throw RuntimeException(e)
                }

                createAndInitTable()
            }
        }
    }

    private fun createAndInitTable() {
        try {
            TableUtils.createTable(connectionSource, CameraJob::class.java)
        } catch (e: SQLException) {
            TagLog.e("Can't create database", e)
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val DATABASE_NAME = "AppLocal.db"
        private const val DATABASE_VERSION = 8
    }
}
