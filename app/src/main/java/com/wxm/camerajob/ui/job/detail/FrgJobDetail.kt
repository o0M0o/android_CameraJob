package com.wxm.camerajob.ui.job.detail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import com.wxm.camerajob.R
import com.wxm.camerajob.data.db.DBDataChangeEvent
import com.wxm.camerajob.data.define.*
import com.wxm.camerajob.data.entity.CameraJob
import com.wxm.camerajob.ui.base.JobGallery
import com.wxm.camerajob.ui.job.slide.ACJobSlide
import com.wxm.camerajob.App
import kotterknife.bindView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import wxm.androidutil.image.ImageUtil
import wxm.androidutil.improve.doJudge
import wxm.androidutil.improve.let1
import wxm.androidutil.time.CalendarUtility
import wxm.androidutil.ui.dialog.DlgAlert
import wxm.androidutil.ui.frg.FrgSupportBaseAdv
import wxm.androidutil.ui.moreAdapter.MoreAdapter
import wxm.androidutil.ui.view.ViewHelper
import wxm.androidutil.ui.view.ViewHolder
import wxm.androidutil.util.FileUtil
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.HashMap


/**
 * fragment for show job
 * Created by WangXM on 2016/10/14.
 */
class FrgJobDetail : FrgSupportBaseAdv() {
    private val mGVPic: GridView by bindView(R.id.gv_pic)

    private val mIBDelete: ImageButton by bindView(R.id.ib_job_stop)
    private val mIBPlay: ImageButton by bindView(R.id.ib_job_run_or_pause)
    private val mIBLook: ImageButton by bindView(R.id.ib_job_look)
    private val mIBSlide: ImageButton by bindView(R.id.ib_job_slide_look)

    override fun isUseEventBus(): Boolean = true
    override fun getLayoutID(): Int = R.layout.pg_job_detail

    private var mJobID = GlobalDef.INT_INVALID_ID
    private var mJobPath: String = ""

    private val mJobHMap = HashMap<String, String>()

    /**
     * 数据库内数据变化处理器
     * @param event     事件
     */
    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDBDataChangeEvent(event: DBDataChangeEvent) {
        reloadUI()
    }

    override fun initUI(savedInstanceState: Bundle?) {
        arguments!!.let1 {
            mJobID = it.getInt(ACJobDetail.KEY_JOB_ID, GlobalDef.INT_INVALID_ID)
            mJobPath = it.getString(ACJobDetail.KEY_JOB_DIR, "")

            assert(GlobalDef.INT_INVALID_ID != mJobID || !mJobPath.isEmpty())
        }

        loadUI(savedInstanceState)
    }

    override fun loadUI(savedInstanceState: Bundle?) {
        // for job
        if (GlobalDef.INT_INVALID_ID != mJobID) {
            mJobPath = App.getCameraJobDir(mJobID)!!
            App.getCameraJobHelper().getCameraJobById(mJobID)?.let1 {
                aliveCameraJob(it)
            }
        } else {
            diedCameraJob(mJobPath)
        }

        // for buttons
        arrayOf(mIBDelete, mIBPlay, mIBLook, mIBSlide).forEach {
            it.setOnClickListener(::onIBClick)
        }
        refreshButton()

        // for job pics
        val pic = FileUtil.getDirFiles(mJobPath, "jpg", false)
        mGVPic.adapter = GVPicAdapter(LinkedList<HashMap<String, String>>().apply {
            addAll(pic.map { HashMap<String, String>().apply { put(KEY_PIC_PATH, it) } })
        })
    }

    /// BEGIN PRIVATE
    private fun onIBClick(v: View) {
        val id = Integer.parseInt(mJobHMap[KEY_ID] ?: "-1")
        when (v.id) {
            R.id.ib_job_stop -> {
                if (ALIVE_JOB == mJobHMap[KEY_TYPE]) {
                    DlgAlert.showAlert(context!!, R.string.dlg_warn, R.string.info_remove_job) {
                        it.setPositiveButton(R.string.cn_sure) { _, _ ->
                            App.getCameraJobHelper().removeCameraJob(id)

                            mJobHMap[KEY_TYPE] = DIED_JOB
                            mJobID = GlobalDef.INT_INVALID_ID

                            reloadUI()
                        }
                    }

                } else {
                    DlgAlert.showAlert(context!!, R.string.dlg_warn, R.string.info_delete_job) {
                        it.setPositiveButton(R.string.cn_sure) { _, _ ->
                            App.getCameraJobHelper().deleteCameraJob(id)
                            activity!!.finish()
                        }
                    }
                }
            }

            R.id.ib_job_run_or_pause -> {
                App.getCameraJobHelper().getCameraJobById(id)?.let1 { cj ->
                    cj.let1 {
                        it.status = (it.status == EJobStatus.PAUSE.status)
                                .doJudge(EJobStatus.RUN.status, EJobStatus.PAUSE.status)
                        App.getCameraJobHelper().modifyCameraJob(it)
                    }
                    reloadUI()
                }
            }

            R.id.ib_job_look -> {
                App.getCameraJobDir(id)?.let {
                    JobGallery().openGallery(activity!!, it)
                }
            }

            R.id.ib_job_slide_look -> {
                Intent(activity, ACJobSlide::class.java).let {
                    it.putExtra(EAction.LOAD_PHOTO_DIR.actName, App.getCameraJobDir(id)!!)
                    startActivityForResult(it, 1)
                }
            }
        }

        refreshButton()
    }

    private fun refreshButton() {
        when (mJobHMap[KEY_STATUS]) {
            EJobStatus.RUN.status -> {
                mIBPlay.visibility = View.VISIBLE
                mIBPlay.setBackgroundResource(R.drawable.ic_pause)
            }
            EJobStatus.PAUSE.status -> {
                mIBPlay.visibility = View.VISIBLE
                mIBPlay.setBackgroundResource(R.drawable.ic_start)
            }
            else -> {
                mIBPlay.visibility = View.GONE
            }
        }

        if (0 == FileUtil.getDirFilesCount(App.getCameraJobDir(Integer.parseInt(mJobHMap[KEY_ID]))!!,
                        "jpg", false)) {
            mIBLook.visibility = View.GONE
            mIBSlide.visibility = View.GONE
        } else {
            mIBLook.visibility = View.VISIBLE
            mIBSlide.visibility = View.VISIBLE
        }
    }


    private fun diedCameraJob(dir: String) {
        mJobHMap.clear()
        val cj = App.getCameraJobFromDir(dir) ?: return
        mJobHMap[KEY_JOB_NAME] = cj.name + "(已移除)"
        mJobHMap[KEY_JOB_TYPE] = ""
        mJobHMap[KEY_JOB_START_END_DATE] = ""
        mJobHMap[KEY_PHOTO_COUNT] = "可查看已拍摄图片"
        mJobHMap[KEY_PHOTO_LAST_TIME] = "可移除此任务"
        mJobHMap[KEY_ID] = Integer.toString(cj._id)
        mJobHMap[KEY_STATUS] = EJobStatus.STOP.status
        mJobHMap[KEY_TYPE] = DIED_JOB

        doShow()
    }

    private fun aliveCameraJob(cj: CameraJob) {
        mJobHMap.clear()
        val at = CalendarUtility.YearMonthDayHourMinute.let {
            context!!.getString(R.string.fs_start_end_date, it.format(cj.startTime), it.format(cj.endTime))
        }

        val jobName = (if (cj.status == EJobStatus.RUN.status) "运行" else "暂停").let {
            "${cj.name}($it)"
        }

        val detail = if (0 != cj.photoCount)
            context!!.getString(R.string.fs_photo_last, CalendarUtility.Full.format(cj.lastPhotoTime))
        else ""

        mJobHMap[KEY_JOB_NAME] = jobName
        mJobHMap[KEY_JOB_TYPE] = context!!.getString(R.string.fs_job_type, cj.type, cj.point)
        mJobHMap[KEY_JOB_START_END_DATE] = at
        mJobHMap[KEY_PHOTO_COUNT] = context!!.getString(R.string.fs_photo_count, cj.photoCount)
        mJobHMap[KEY_PHOTO_LAST_TIME] = detail
        mJobHMap[KEY_ID] = Integer.toString(cj._id)
        mJobHMap[KEY_STATUS] = cj.status
        mJobHMap[KEY_TYPE] = ALIVE_JOB

        doShow()
    }

    private fun doShow() {
        ViewHelper(view!!).let1 {
            it.setText(R.id.tv_job_name, mJobHMap[KEY_JOB_NAME]!!)
            it.setText(R.id.tv_job_type, mJobHMap[KEY_JOB_TYPE]!!)
            it.setText(R.id.tv_job_date, mJobHMap[KEY_JOB_START_END_DATE]!!)
            it.setText(R.id.tv_phtot_count, mJobHMap[KEY_PHOTO_COUNT]!!)
            it.setText(R.id.tv_photo_last_time, mJobHMap[KEY_PHOTO_LAST_TIME]!!)
        }
    }

    private fun loadBitMapForGV(path: String): Bitmap {
        val bitmap = decodeFile(path)!!
        val degree = ImageUtil.readPictureDegree(path)
        return Matrix().let {
            val w = bitmap.width
            val h = bitmap.height
            if (0 != degree) {
                it.setRotate(degree.toFloat(), (w / 2).toFloat(), (h / 2).toFloat())
                Bitmap.createBitmap(bitmap, 0, 0, w, h, it, true)!!
            } else  {
                bitmap
            }
        }
    }

    private fun decodeFile(f: String): Bitmap? {
        try {
            // decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(f), null, o)

            // Find the correct scale value. It should be the power of 2.
            val requiredSize = 70
            var widthTmp = o.outWidth
            var heightTmp = o.outHeight
            var scale = 1
            while (true) {
                if (widthTmp / 2 < requiredSize || heightTmp / 2 < requiredSize)
                    break

                widthTmp /= 2
                heightTmp /= 2
                scale *= 2
            }

            // decode with inSampleSize
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            return BitmapFactory.decodeStream(FileInputStream(f), null, o2)
        } catch (e: FileNotFoundException) {
        }

        return null
    }

    /// END PRIVATE

    /**
     * adapter for listview to show jobs status
     * Created by wxm on 2016/8/13.
     */
    inner class GVPicAdapter(data: List<Map<String, String>>)
        : MoreAdapter(context!!, data, R.layout.gi_pic) {
        override fun loadView(pos: Int, vhHolder: ViewHolder) {
            @Suppress("UNCHECKED_CAST")
            val path = (getItem(pos) as Map<String, String>).getValue(KEY_PIC_PATH)
            vhHolder.getView<ImageView>(R.id.iv_pic)!!
                    .setImageBitmap(loadBitMapForGV(path))
        }
    }

    companion object {
        private const val KEY_PIC_PATH = "pic_path"

        const val ALIVE_JOB = "alive"
        const val DIED_JOB = "died"

        const val KEY_JOB_NAME = "job_name"

        const val KEY_JOB_TYPE = "job_type"
        const val KEY_JOB_START_END_DATE = "job_start_end_date"

        const val KEY_PHOTO_COUNT = "key_photo_count"
        const val KEY_PHOTO_LAST_TIME = "key_photo_last_time"

        const val KEY_STATUS = "key_status"
        const val KEY_TYPE = "key_type"
        const val KEY_ID = "key_id"
    }
}

