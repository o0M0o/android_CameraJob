package com.wxm.camerajob.ui.dialog

import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.READ_SMS
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.os.Message
import android.support.design.widget.TextInputEditText
import android.view.View
import com.wxm.camerajob.R
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import wxm.androidutil.app.AppBase
import wxm.androidutil.log.TagLog
import wxm.androidutil.ui.dialog.DlgAlert
import wxm.androidutil.ui.dialog.DlgOKOrNOBase
import wxm.androidutil.util.SIMCardUtil
import wxm.androidutil.util.UtilFun
import wxm.androidutil.util.WRMsgHandler
import java.io.IOException

/**
 * submit usr message
 * Created by WangXM on 2017/1/9.
 */
class DlgUsrMessage : DlgOKOrNOBase() {
    private var mProgressStatus = 0
    private lateinit var mHDProgress: LocalMsgHandler
    private lateinit var mPDDlg: ProgressDialog

    private lateinit var mSZUrlPost: String
    private lateinit var mSZColUsr: String
    private lateinit var mSZColMsg: String
    private lateinit var mSZColAppName: String
    private lateinit var mSZColValAppName: String
    private lateinit var mSZUsrMessage: String
    private lateinit var mSZAccept: String
    private lateinit var mSZGiveUp: String

    private lateinit var mETUsrMessage: TextInputEditText

    override fun createDlgView(savedInstanceState: Bundle?): View {
        initDlgTitle(mSZUsrMessage, mSZAccept, mSZGiveUp)
        return View.inflate(activity, R.layout.dlg_send_message, null)
    }

    override fun initDlgView(savedInstanceState: Bundle?) {
        mETUsrMessage = findDlgChildView(R.id.et_usr_message)!!

        context!!.resources.let {
            mSZUrlPost = it.getString(R.string.url_post_send_message)
            mSZColUsr = it.getString(R.string.col_usr)
            mSZColMsg = it.getString(R.string.col_message)
            mSZColAppName = it.getString(R.string.col_app_name)
            mSZColValAppName = it.getString(R.string.col_val_app_name)
            mSZUsrMessage = it.getString(R.string.cn_usr_message)
            mSZAccept = it.getString(R.string.accept)
            mSZGiveUp = it.getString(R.string.cn_cancel)

            Unit
        }

        // for progress
        mHDProgress = LocalMsgHandler(this)
        mPDDlg = ProgressDialog(context)
    }

    override fun checkBeforeOK(): Boolean {
        mETUsrMessage.text.toString().let {
            if (UtilFun.StringIsNullOrEmpty(it)) {
                DlgAlert.showAlert(activity!!, "警告", "消息不能为空")
                return false
            }

            return (if (AppBase.checkPermission(READ_PHONE_STATE) && AppBase.checkPermission(READ_SMS)) {
                SIMCardUtil(context).nativePhoneNumber
            } else "null").let {pn ->
                sendMsgByHttpPost(pn, it)
            }
        }
    }

    /**
     * use http send message
     *
     * @param usr   user info
     * @param msg   message info
     * @return      true if success
     */
    private fun sendMsgByHttpPost(usr: String, msg: String): Boolean {
        HttpPostTask(usr, msg).execute()
        return true
    }

    /**
     * for send http post
     */
    inner class HttpPostTask internal constructor(private val mSZUsr: String, private val mSZMsg: String)
        : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()
            mProgressStatus = 0

            mPDDlg.apply {
                max = 100
                setTitle("发送消息")
                setMessage("发送进度")
                setCancelable(true)
                setButton(DialogInterface.BUTTON_POSITIVE, "取消")
                { _, _ -> }

                // 设置对话框的进度条风格
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                // 设置对话框的进度条是否显示进度
                isIndeterminate = false

                incrementProgressBy(-progress)
                show()
            }
        }

        override fun doInBackground(vararg params: Void): Boolean? {
            val client = OkHttpClient()
            try {
                // set param
                val body = JSONObject().apply {
                    put(mSZColUsr, mSZUsr)
                    put(mSZColMsg, mSZMsg)
                    put(mSZColAppName, "$mSZColValAppName-${AppBase.getVerName()}")
                }.let {
                    RequestBody.create(JSON, it.toString())
                }

                mProgressStatus = 50
                Message().apply { what = MSG_PROGRESS_UPDATE }.let {
                    mHDProgress.sendMessage(it)
                }

                Request.Builder().url(mSZUrlPost).post(body).build().let {
                    client.newCall(it).execute()
                }

                mProgressStatus = 100
                Message().apply { what = MSG_PROGRESS_UPDATE }.let {
                    mHDProgress.sendMessage(it)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return true
        }


        override fun onPostExecute(bret: Boolean?) {
            super.onPostExecute(bret)
            mPDDlg.dismiss()
        }
    }

    /**
     * safe message hanlder
     */
    private class LocalMsgHandler
        internal constructor(ac: DlgUsrMessage) : WRMsgHandler<DlgUsrMessage>(ac) {
        override fun processMsg(m: Message, home: DlgUsrMessage) {
            when (m.what) {
                MSG_PROGRESS_UPDATE -> {
                    home.mPDDlg.progress = home.mProgressStatus
                }

                else -> TagLog.e( "$m can not process")
            }
        }
    }

    companion object {
        // for progress dialog when send http post
        private val PROGRESS_DIALOG = 0x112
        private val MSG_PROGRESS_UPDATE = 0x111

        // for http post
        private val JSON = MediaType.parse("application/json; charset=utf-8")
    }
}
