package com.wxm.camerajob.ui.dialog

import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.READ_SMS
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.view.View
import android.widget.ProgressBar
import com.wxm.camerajob.BuildConfig
import com.wxm.camerajob.R
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import wxm.androidutil.app.AppBase
import wxm.androidutil.improve.doJudge
import wxm.androidutil.improve.forObj
import wxm.androidutil.improve.let1
import wxm.androidutil.log.TagLog
import wxm.androidutil.ui.dialog.DlgAlert
import wxm.androidutil.ui.dialog.DlgOKOrNOBase
import wxm.androidutil.util.SIMCardUtil
import java.io.IOException

/**
 * submit usr message
 * Created by WangXM on 2017/1/9.
 */
class DlgUsrMessage : DlgOKOrNOBase() {
    private var mProgressStatus = 0

    private lateinit var mSZUrlPost: String
    private lateinit var mSZColUsr: String
    private lateinit var mSZColMsg: String
    private lateinit var mSZColAppName: String
    private lateinit var mSZColValAppName: String
    private lateinit var mSZColReplyAddress: String

    private lateinit var mETUsrMessage: TextInputEditText
    private lateinit var mETReplyAddress: TextInputEditText

    private lateinit var mPBLoginProgress: ProgressBar

    override fun createDlgView(savedInstanceState: Bundle?): View {
        context!!.resources.let1 {
            initDlgTitle(it.getString(R.string.usr_message_title),
                    it.getString(R.string.accept),
                    it.getString(R.string.cn_cancel))
        }
        return View.inflate(context, R.layout.dlg_send_message, null)
    }

    override fun initDlgView(savedInstanceState: Bundle?) {
        mETUsrMessage = findDlgChildView(R.id.et_usr_message)!!
        mETReplyAddress = findDlgChildView(R.id.et_reply_address)!!
        mPBLoginProgress = findDlgChildView(R.id.login_progress)!!

        context!!.resources.let1 {
            mSZUrlPost = BuildConfig.UsrMsgUrl
            mSZColUsr = it.getString(R.string.col_usr)
            mSZColMsg = it.getString(R.string.col_message)
            mSZColReplyAddress = it.getString(R.string.col_reply_address)
            mSZColAppName = it.getString(R.string.col_app_name)
            mSZColValAppName = it.getString(R.string.col_val_app_name)
        }
    }

    override fun checkBeforeOK(): Boolean {
        mETUsrMessage.text.toString().let {
            if (it.isEmpty()) {
                DlgAlert.showAlert(activity!!, R.string.dlg_warn, "消息不能为空")
                return false
            }

            val usr = (AppBase.checkPermission(READ_PHONE_STATE) && AppBase.checkPermission(READ_SMS)).doJudge(
                    SIMCardUtil(context).nativePhoneNumber, "null")
            val replyAddress = mETReplyAddress.text.toString().forObj({t -> t},  {""})
            HttpPostTask(usr, it, replyAddress).execute()
            return true
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.

        mPBLoginProgress.visibility = if (show) View.VISIBLE else View.GONE
        mPBLoginProgress.animate().setDuration(shortAnimTime.toLong())
                .alpha((if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mPBLoginProgress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    /**
     * for send http post
     */
    inner class HttpPostTask internal constructor(private val mSZUsr: String,
                                                  private val mSZMsg: String,
                                                  private val mSZReplyAddress: String)
        : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            super.onPreExecute()
            mProgressStatus = 0

            showProgress(true)
            TagLog.i("start post")
        }

        override fun doInBackground(vararg params: Void): Boolean? {
            TagLog.i("in post")
            val client = OkHttpClient()
            try {
                // set param
                val body = JSONObject().apply {
                    put(mSZColUsr, mSZUsr)
                    put(mSZColMsg, mSZMsg)
                    put(mSZColReplyAddress, mSZReplyAddress)
                    put(mSZColAppName, "$mSZColValAppName-${AppBase.getVerName()}")
                }.let {
                    RequestBody.create(JSON, it.toString())
                }

                /*
                mProgressStatus = 50
                Message().apply { what = MSG_PROGRESS_UPDATE }.let {
                    mHDProgress.sendMessage(it)
                }
                */

                Request.Builder().url(mSZUrlPost).post(body).build().let {
                    client.newCall(it).execute()
                }

                /*
                mProgressStatus = 100
                Message().apply { what = MSG_PROGRESS_UPDATE }.let {
                    mHDProgress.sendMessage(it)
                }
                */
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return true
        }

        override fun onPostExecute(bret: Boolean?) {
            super.onPostExecute(bret)
            showProgress(false)
            TagLog.i("end post")
        }
    }


    /*
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

                else -> TagLog.e("$m can not process")
            }
        }
    }
    */

    companion object {
        // for http post
        private val JSON = MediaType.parse("application/json; charset=utf-8")
        private const val shortAnimTime = 5000
    }
}
