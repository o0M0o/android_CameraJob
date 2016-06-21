package com.wxm.camerajob.ui.activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.TakePhotoParam;
import com.wxm.camerajob.base.utility.ContextUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ActivityTestSilentCamera extends AppCompatActivity implements View.OnClickListener {

    private Button  mBTCapture;
    private Button  mBTLeave;
    private ImageView   mIVPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_silent_camera);

        mBTCapture = (Button)findViewById(R.id.acbt_capture);
        mBTLeave = (Button)findViewById(R.id.acbt_leave);
        mBTCapture.setOnClickListener(this);
        mBTLeave.setOnClickListener(this);

        mIVPhoto = (ImageView)findViewById(R.id.aciv_photo);
    }

    /**
     * 加载本地图片
     * http://bbs.3gstdy.com
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

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
                TakePhotoParam tp = new TakePhotoParam("tmp.jpg", "1");
                if(ContextUtil.getInstance().mSCHHandler.TakePhoto(tp)) {
                    Toast.makeText(getApplicationContext(),
                            "takephoto ok",
                            Toast.LENGTH_SHORT).show();

                    String fn = tp.mPhotoFileDir + "/" + tp.mFileName;
                    Bitmap bm = getLoacalBitmap(fn);
                    if(null != bm) {
                        mIVPhoto.setImageBitmap(bm);
                        Toast.makeText(getApplicationContext(),
                                "load '" + fn + "' ok!",
                                Toast.LENGTH_SHORT).show();
                    }
                    else    {
                        Toast.makeText(getApplicationContext(),
                                "load '" + fn + "' failed!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else    {
                    Toast.makeText(getApplicationContext(),
                            "takephoto failed",
                            Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }

    }
}
