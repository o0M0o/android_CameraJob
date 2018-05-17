package com.wxm.camerajob.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.RuntimeExceptionDao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.wxm.camerajob.data.define.CameraJob
import com.wxm.camerajob.data.define.CameraJobStatus
import com.wxm.camerajob.utility.FileLogger

import java.sql.SQLException

import wxm.androidutil.util.UtilFun

/**
 * sqlite helper from app
 * Created by wxm on 2016/8/12.
 */
class DBOrmLiteHelper(context: Context) : OrmLiteSqliteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    val cameraJobREDao: RuntimeExceptionDao<CameraJob, Int> = getRuntimeExceptionDao(CameraJob::class.java)
    val cameraJobStatusREDao: RuntimeExceptionDao<CameraJobStatus, Int> = getRuntimeExceptionDao(CameraJobStatus::class.java)

    override fun onCreate(db: SQLiteDatabase, connectionSource: ConnectionSource) {
        createAndInitTable()
    }

    override fun onUpgrade(database: SQLiteDatabase,
                           connectionSource: ConnectionSource, oldVersion: Int, newVersion: Int) {
        when (newVersion) {
            5 -> {
                createAndInitTable()
            }

            6 -> {
                try {
                    TableUtils.dropTable<CameraJob, Any>(connectionSource, CameraJob::class.java, false)
                    TableUtils.dropTable<CameraJobStatus, Any>(connectionSource, CameraJobStatus::class.java, false)
                } catch (e: SQLException) {
                    e.printStackTrace()
                    FileLogger.logger.severe("dropTable failure, ex = " + UtilFun.ExceptionToString(e))
                }

                createAndInitTable()
            }
        }
    }

    private fun createAndInitTable() {
        try {
            TableUtils.createTable(connectionSource, CameraJob::class.java)
            TableUtils.createTable(connectionSource, CameraJobStatus::class.java)
        } catch (e: SQLException) {
            Log.e(LOG_TAG, "Can't create database", e)
            throw RuntimeException(e)
        }

    }

    companion object {
        private val LOG_TAG = ::DBOrmLiteHelper.javaClass.simpleName
        private const val DATABASE_NAME = "AppLocal.db"
        private const val DATABASE_VERSION = 6
    }
}
