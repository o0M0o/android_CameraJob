package com.wxm.camerajob.ui.dialog;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.wxm.camerajob.R;
import com.wxm.camerajob.utility.ContextUtil;
import com.wxm.camerajob.utility.SIMCardInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import wxm.androidutil.Dialog.DlgOKOrNOBase;
import wxm.androidutil.util.UtilFun;
import wxm.androidutil.util.WRMsgHandler;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

/**
 * submit usr message
 * Created by ookoo on 2017/1/9.
 */
public class DlgUsrMessage extends DlgOKOrNOBase {
    // for progress dialog when send http post
    private final static int       PROGRESS_DIALOG     = 0x112;
    private final static int       MSG_PROGRESS_UPDATE = 0x111;
    private int             mProgressStatus     = 0;
    private LocalMsgHandler mHDProgress;
    private ProgressDialog  mPDDlg;

    // for http post
    private static final MediaType JSON =
            MediaType.parse("application/json; charset=utf-8");

    @BindString(R.string.url_post_send_message)
    String mSZUrlPost;

    @BindString(R.string.col_usr)
    String mSZColUsr;

    @BindString(R.string.col_message)
    String mSZColMsg;

    @BindString(R.string.col_app_name)
    String mSZColAppName;

    @BindString(R.string.col_val_app_name)
    String mSZColValAppName;

    @BindString(R.string.cn_usr_message)
    String mSZUsrMessage;

    @BindString(R.string.cn_accept)
    String mSZAccept;

    @BindString(R.string.cn_giveup)
    String mSZGiveUp;

    /*
    @BindView(R.id.et_usr_name)
    TextInputEditText mETUsrName;
    */

    @BindView(R.id.et_usr_message)
    TextInputEditText mETUsrMessage;

    @Override
    protected View InitDlgView() {
        InitDlgTitle(mSZUsrMessage, mSZAccept, mSZGiveUp);
        View vw = View.inflate(getActivity(), R.layout.dlg_send_message, null);
        ButterKnife.bind(this, vw);

        // for progress
        mHDProgress = new LocalMsgHandler(this);
        mPDDlg = new ProgressDialog(getContext());
        return vw;
    }

    @Override
    protected boolean checkBeforeOK() {
        String msg = mETUsrMessage.getText().toString();
        if (UtilFun.StringIsNullOrEmpty(msg)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("消息不能为空")
                    .setTitle("警告");
            AlertDialog dlg = builder.create();
            dlg.show();
            return false;
        }

        String usr = null;
        if (ContextCompat.checkSelfPermission(ContextUtil.getInstance(), READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ContextUtil.getInstance(), READ_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
            SIMCardInfo si = new SIMCardInfo(getContext());
            usr = si.getNativePhoneNumber();
        }

        return sendMsgByHttpPost(UtilFun.StringIsNullOrEmpty(usr) ? "null" : usr, msg);
    }

    /**
     * 通过http post发送消息
     *
     * @param usr 用户
     * @param msg 消息
     * @return 发送成功返回true
     */
    private boolean sendMsgByHttpPost(String usr, String msg) {
        HttpPostTask ht = new HttpPostTask(usr, msg);
        ht.execute();
        return true;
    }


    /**
     * for send http post
     */
    public class HttpPostTask extends AsyncTask<Void, Void, Boolean> {
        private final String mSZUsr;
        private final String mSZMsg;

        HttpPostTask(String usr, String msg) {
            mSZUsr = usr;
            mSZMsg = msg;
        }

        @Override
        protected void onPreExecute()   {
            super.onPreExecute();

            mProgressStatus = 0;

            mPDDlg.setMax(100);
            // 设置对话框的标题
            mPDDlg.setTitle("发送消息");
            // 设置对话框 显示的内容
            mPDDlg.setMessage("发送进度");
            // 设置对话框不能用“取消”按钮关闭
            mPDDlg.setCancelable(true);
            mPDDlg.setButton("取消", (dialogInterface, i) -> {
            });

            // 设置对话框的进度条风格
            mPDDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mPDDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            // 设置对话框的进度条是否显示进度
            mPDDlg.setIndeterminate(false);

            mPDDlg.incrementProgressBy(-mPDDlg.getProgress());
            mPDDlg.show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            try {
                // set param
                JSONObject param = new JSONObject();
                param.put(mSZColUsr, mSZUsr);
                param.put(mSZColMsg, mSZMsg);
                param.put(mSZColAppName,
                        mSZColValAppName + "-" + ContextUtil.getVerName(getContext()));

                RequestBody body = RequestBody.create(JSON, param.toString());

                mProgressStatus = 50;
                Message m = new Message();
                m.what = MSG_PROGRESS_UPDATE;
                mHDProgress.sendMessage(m);

                Request request = new Request.Builder()
                                        .url(mSZUrlPost).post(body).build();
                client.newCall(request).execute();

                mProgressStatus = 100;
                Message m1 = new Message();
                m1.what = MSG_PROGRESS_UPDATE;
                mHDProgress.sendMessage(m1);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean bret) {
            super.onPostExecute(bret);
            mPDDlg.dismiss();
        }
    }

    /**
     * safe message hanlder
     */
    private static class LocalMsgHandler extends WRMsgHandler<DlgUsrMessage> {
        LocalMsgHandler(DlgUsrMessage ac) {
            super(ac);
            TAG = "LocalMsgHandler";
        }

        @Override
        protected void processMsg(Message m, DlgUsrMessage home) {
            switch (m.what) {
                case MSG_PROGRESS_UPDATE: {
                    home.mPDDlg.setProgress(home.mProgressStatus);
                }
                break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", m.toString()));
                    break;
            }
        }
    }
}
