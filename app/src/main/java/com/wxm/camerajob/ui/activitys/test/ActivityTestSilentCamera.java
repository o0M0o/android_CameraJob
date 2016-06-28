package com.wxm.camerajob.ui.activitys.test;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.SilentCameraHelper;
import com.wxm.camerajob.base.utility.UtilFun;

public class ActivityTestSilentCamera extends AppCompatActivity implements View.OnClickListener {

    private Button  mBTCapture;
    private ImageView   mIVPhoto;
    private ACTestMsgHandler   mSelfHandler;
    private TakePhotoParam     mTPParam;

    private static final int SELFMSGWHAT_TAKEPHOTO_SUCCESS = 1;
    private static final int SELFMSGWHAT_TAKEPHOTO_FAILED  = 2;

    public class ACTestMsgHandler extends Handler {
        private static final String TAG = "ACStartMsgHandler";
        private ActivityTestSilentCamera mActivity;

        public ACTestMsgHandler(ActivityTestSilentCamera acstart) {
            super();
            mActivity = acstart;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SELFMSGWHAT_TAKEPHOTO_SUCCESS: {
                    Toast.makeText(getApplicationContext(),
                            "takephoto ok",
                            Toast.LENGTH_SHORT).show();

                    String fn = mTPParam.mPhotoFileDir + "/" + mTPParam.mFileName;
                    Bitmap bm = UtilFun.getRotatedLocalBitmap(fn);
                    if(null != bm) {
                        mIVPhoto.setImageBitmap(bm);
                    }
                    else    {
                        Toast.makeText(getApplicationContext(),
                                "load '" + fn + "' failed!",
                                Toast.LENGTH_SHORT).show();
                    }

                    mBTCapture.setClickable(true);
                    mBTCapture.setTextColor(getResources().getColor(R.color.black));
                }
                break;

                case SELFMSGWHAT_TAKEPHOTO_FAILED:  {
                    Toast.makeText(getApplicationContext(),
                            "takephoto failed",
                            Toast.LENGTH_SHORT).show();

                    mBTCapture.setClickable(true);
                    mBTCapture.setTextColor(getResources().getColor(R.color.black));
                }
                break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_silent_camera);

        mBTCapture = (Button)findViewById(R.id.acbt_capture);
        assert mBTCapture != null;
        mBTCapture.setOnClickListener(this);

        Button mBTLeave = (Button) findViewById(R.id.acbt_leave);
        assert mBTLeave != null;
        mBTLeave.setOnClickListener(this);

        mIVPhoto = (ImageView)findViewById(R.id.aciv_photo);

        mSelfHandler = new ACTestMsgHandler(this);
        mTPParam = null;
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
                mBTCapture.setTextColor(getResources().getColor(R.color.gray));

                String sp = ContextUtil.getInstance().getAppPhotoRootDir();
                mTPParam = new TakePhotoParam(sp, "tmp.jpg", "1");

                //noinspection ConstantConditions
                ContextUtil.getCameraHelper().setTakePhotoCallBack(
                        new SilentCameraHelper.takePhotoCallBack() {
                            @Override
                            public void onTakePhotoSuccess(TakePhotoParam tp) {
                                //noinspection ConstantConditions
                                ContextUtil.getCameraHelper().setTakePhotoCallBack(null);
                                mSelfHandler.sendEmptyMessage(SELFMSGWHAT_TAKEPHOTO_SUCCESS);
                            }

                            @Override
                            public void onTakePhotoFailed(TakePhotoParam tp) {
                                //noinspection ConstantConditions
                                ContextUtil.getCameraHelper().setTakePhotoCallBack(null);
                                mSelfHandler.sendEmptyMessage(SELFMSGWHAT_TAKEPHOTO_FAILED);
                            }
                        }
                );

                //noinspection ConstantConditions
                ContextUtil.getCameraHelper().TakePhoto(mTPParam);
            }
            break;
        }

    }
}
