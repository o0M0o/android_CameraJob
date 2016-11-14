package com.wxm.camerajob.ui.test;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.PreferencesUtil;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.SilentCameraHelper;

import java.lang.ref.WeakReference;

import cn.wxm.andriodutillib.util.ImageUtil;
import cn.wxm.andriodutillib.util.UtilFun;

public class ACSilentCameraTest extends AppCompatActivity implements View.OnClickListener {
    private Button  mBTCapture;
    private ImageView   mIVPhoto;
    private ACTestMsgHandler   mSelfHandler;
    private TakePhotoParam     mTPParam;

    private static final int SELFMSGWHAT_TAKEPHOTO_SUCCESS = 1;
    private static final int SELFMSGWHAT_TAKEPHOTO_FAILED  = 2;

    private int mCLGrey;
    private int mCLBlack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_silent_camera_test);

        mBTCapture = UtilFun.cast_t(findViewById(R.id.acbt_capture));
        mBTCapture.setOnClickListener(this);

        Button mBTLeave = UtilFun.cast_t(findViewById(R.id.acbt_leave));
        mBTLeave.setOnClickListener(this);

        mIVPhoto = UtilFun.cast_t(findViewById(R.id.aciv_photo));
        mIVPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);

        mSelfHandler = new ACTestMsgHandler(this);
        mTPParam = null;

        mCLGrey = getResources().getColor(R.color.gray);
        mCLBlack = getResources().getColor(R.color.black);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.acbt_leave : {
                Intent data = new Intent();
                setResult(GlobalDef.INTRET_NOTCARE, data);
                finish();
            }
            break;

            case R.id.acbt_capture :    {
                mBTCapture.setClickable(false);
                mBTCapture.setTextColor(mCLGrey);

                String sp = ContextUtil.getInstance().getAppPhotoRootDir();
                mTPParam = new TakePhotoParam(sp, "tmp.jpg", "1");

                //noinspection ConstantConditions
                final SilentCameraHelper sh = ContextUtil.getCameraHelper();
                if(null != sh) {
                    sh.setTakePhotoCallBack(
                            new SilentCameraHelper.takePhotoCallBack() {
                                @Override
                                public void onTakePhotoSuccess(TakePhotoParam tp) {
                                    //noinspection ConstantConditions
                                    sh.setTakePhotoCallBack(null);
                                    mSelfHandler.sendEmptyMessage(SELFMSGWHAT_TAKEPHOTO_SUCCESS);
                                }

                                @Override
                                public void onTakePhotoFailed(TakePhotoParam tp) {
                                    //noinspection ConstantConditions
                                    sh.setTakePhotoCallBack(null);
                                    mSelfHandler.sendEmptyMessage(SELFMSGWHAT_TAKEPHOTO_FAILED);
                                }
                            }
                    );

                    //noinspection ConstantConditions
                    sh.TakePhoto(PreferencesUtil.loadCameraParam(), mTPParam);
                }

            }
            break;
        }

    }


    public static class ACTestMsgHandler extends Handler {
        private static final String TAG = "ACTestMsgHandler";
        private WeakReference<ACSilentCameraTest> mActivity;

        ACTestMsgHandler(ACSilentCameraTest acstart) {
            super();
            mActivity = new WeakReference<>(acstart);
        }

        @Override
        public void handleMessage(Message msg) {
            ACSilentCameraTest ac_home = mActivity.get();
            if(null == ac_home)
                return;

            switch (msg.what) {
                case SELFMSGWHAT_TAKEPHOTO_SUCCESS: {
                    Toast.makeText(ac_home,
                            "takephoto ok",
                            Toast.LENGTH_SHORT).show();

                    Rect rt = new Rect();
                    ac_home.mIVPhoto.getDrawingRect(rt);
                    //MySize psz = new MySize(rt.width(), rt.height());
                    //Log.i(TAG, "perfence size : " + psz);

                    String fn = ac_home.mTPParam.mPhotoFileDir + "/" + ac_home.mTPParam.mFileName;
                    //Bitmap bm = ImageUtil.getRotatedLocalBitmap(fn, psz);
                    Bitmap bm = ImageUtil.getLocalBitmap(fn);
                    if(null != bm) {
                        ac_home.mIVPhoto.setImageBitmap(bm);
                    }
                    else    {
                        Toast.makeText(ac_home,
                                "load '" + fn + "' failed!",
                                Toast.LENGTH_SHORT).show();
                    }

                    ac_home.mBTCapture.setClickable(true);
                    ac_home.mBTCapture.setTextColor(ac_home.mCLBlack);
                }
                break;

                case SELFMSGWHAT_TAKEPHOTO_FAILED:  {
                    Toast.makeText(ac_home,
                            "takephoto failed",
                            Toast.LENGTH_SHORT).show();

                    ac_home.mBTCapture.setClickable(true);
                    ac_home.mBTCapture.setTextColor(ac_home.mCLBlack);
                }
                break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }
    }
}
