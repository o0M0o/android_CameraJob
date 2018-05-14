package com.wxm.camerajob.ui.dialog

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Message
import android.support.design.widget.TextInputEditText
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View

import com.wxm.camerajob.R
import com.wxm.camerajob.utility.ContextUtil
import com.wxm.camerajob.hardware.SIMCardInfo

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException

import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import wxm.androidutil.Dialog.DlgOKOrNOBase
import wxm.androidutil.util.UtilFun
import wxm.androidutil.util.WRMsgHandler
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.READ_SMS

/**
 * submit usr message
 * Created by WangXM on 2017/1/9.
 */
class DlgUsrMessage : DlgOKOrNOBase() {
    private var mProgressStatus = 0
    private var mHDProgress: LocalMsgHandler? = null
    private var mPDDlg: ProgressDialog? = null

    @BindString(R.string.url_post_send_message)
    internal var mSZUrlPost: String? = null

    @BindString(R.string.col_usr)
    internal var mSZColUsr: String? = null

    @BindString(R.string.col_message)
    internal var mSZColMsg: String? = null

    @BindString(R.string.col_app_name)
    internal var mSZColAppName: String? = null

    @BindString(R.string.col_val_app_name)
    internal var mSZColValAppName: String? = null

    @BindString(R.string.cn_usr_message)
    internal var mSZUsrMessage: String? = null

    @BindString(R.string.cn_accept)
    internal var mSZAccept: String? = null

    @BindString(R.string.cn_giveup)
    internal var mSZGiveUp: String? = null

    /*
    @BindView(R.id.et_usr_name)
    TextInputEditText mETUsrName;
    */

    @BindView(R.id.et_usr_message)
    internal var mETUsrMessage: TextInputEditText? = null

    protected fun InitDlgView(): View {
        InitDlgTitle(mSZUsrMessage, mSZAccept, mSZGiveUp)
        val vw = View.inflate(activity, R.layout.dlg_send_message, null)
        ButterKnife.bind(this, vw)

        // for progress
        mHDProgress = LocalMsgHandler(this)
        mPDDlg = ProgressDialog(context)
        return vw
    }

    override fun checkBeforeOK(): Boolean {
        val msg = mETUsrMessage!!.text.toString()
        if (UtilFun.StringIsNullOrEmpty(msg)) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage("消息不能为空")
                    .setTitle("警告")
            val dlg = builder.create()
            dlg.show()
            return false
        }

        var usr: String? = null
        if (ContextCompat.checkSelfPermission(ContextUtil.instance, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ContextUtil.instance, READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            val si = SIMCardInfo(context)
            usr = si.nativePhoneNumber
        }

        return sendMsgByHttpPost(if (UtilFun.StringIsNullOrEmpty(usr)) "null" else usr, msg)
    }

    /**
     * use http send message
     *
     * @param usr   user info
     * @param msg   message info
     * @return      true if success
     */
    private fun sendMsgByHttpPost(usr: String, msg: String): Boolean {
        val ht = HttpPostTask(usr, msg)
        ht.execute()
        return true
    }


    /**
     * for send http post
     */
    inner class HttpPostTask internal constructor(private val mSZUsr: String, private val mSZMsg: String) : AsyncTask<Void, Void, Boolean>() {

        override fun onPreExecute() {
            super.onPreExecute()

            mProgressStatus = 0

            mPDDlg!!.max = 100
            // 设置对话框的标题
            mPDDlg!!.setTitle("发送消息")
            // 设置对话框 显示的内容
            mPDDlg!!.setMessage("发送进度")
            // 设置对话框不能用“取消”按钮关闭
            mPDDlg!!.setCancelable(true)
            mPDDlg!!.setButton(DialogInterface.BUTTON_POSITIVE,
                    "取消") { dialogInterface, i -> }

            // 设置对话框的进度条风格
            mPDDlg!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            mPDDlg!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            // 设置对话框的进度条是否显示进度
            mPDDlg!!.isIndeterminate = false

            mPDDlg!!.incrementProgressBy(-mPDDlg!!.progress)
            mPDDlg!!.show()
        }

        override fun onCancelled() {
            super.onCancelled()
        }

        override fun doInBackground(vararg params: Void): Boolean? {
            val client = OkHttpClient()
            try {
                // set param
                val param = JSONObject()
                param.put(mSZColUsr, mSZUsr)
                param.put(mSZColMsg, mSZMsg)
                param.put(mSZColAppName,
                        mSZColValAppName + "-" + ContextUtil.getVerName(context))

                val body = RequestBody.create(JSON, param.toString())

                mProgressStatus = 50
                val m = Message()
                m.what = MSG_PROGRESS_UPDATE
                mHDProgress!!.sendMessage(m)

                val request = Request.Builder()
                        .url(mSZUrlPost!!).post(body).build()
                client.newCall(request).execute()

                mProgressStatus = 100
                val m1 = Message()
                m1.what = MSG_PROGRESS_UPDATE
                mHDProgress!!.sendMessage(m1)
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return true
        }


        override fun onPostExecute(bret: Boolean?) {
            super.onPostExecute(bret)
            mPDDlg!!.dismiss()
        }
    }

    /**
     * safe message hanlder
     */
    private class LocalMsgHandler internal constructor(ac: DlgUsrMessage) : WRMsgHandler<DlgUsrMessage>(ac) {
        init {
            TAG = "LocalMsgHandler"
        }

        override fun processMsg(m: Message, home: DlgUsrMessage) {
            when (m.what) {
                MSG_PROGRESS_UPDATE -> {
                    home.mPDDlg!!.progress = home.mProgressStatus
                }

                else -> Log.e(TAG, String.format("msg(%s) can not process", m.toString()))
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
