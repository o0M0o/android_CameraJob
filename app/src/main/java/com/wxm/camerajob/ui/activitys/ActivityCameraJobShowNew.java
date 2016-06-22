package com.wxm.camerajob.ui.activitys;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.utility.UtilFun;

import java.util.LinkedList;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ActivityCameraJobShowNew extends AppCompatActivity {
    private final static String TAG = "CameraJobShowNew";
    private final static Size   SMAIL_IMAGEVIEW_SIZE = new Size(240, 240);

    private ImageView mIVOne;
    private ImageView mIVAll;
    private Gallery   mGGallery;

    private LinkedList<String> mPhotoFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_job_show_new);
        loadPhotos();

        mIVOne = (ImageView)findViewById(R.id.aciv_cjs_show);
        mGGallery = (Gallery)findViewById(R.id.acg_cjs_gallery);

        mGGallery.setAdapter(new ImageAdapter(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acmenu_camerajobshow_actbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meuitem_camerajobshow_leave : {
                Intent data = new Intent();
                setResult(GlobalDef.INTRET_NOTCARE, data);
                finish();
            }
            break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }


    private void loadPhotos()   {
        Intent data = getIntent();
        if(null != data)    {
            String p = data.getStringExtra(GlobalDef.STR_LOAD_PHOTODIR);
            if((null != p) && (!p.isEmpty()))   {
                mPhotoFiles = UtilFun.getDirFiles(p, "jpg", false);
            }
        }

        if(null == mPhotoFiles)
            mPhotoFiles = new LinkedList<>();

        for(String fn : mPhotoFiles)    {
            Log.i(TAG, "photo file : " + fn);
        }
    }


    class ImageAdapter
            extends BaseAdapter {
        //每一个gallery中图像的背景资源
        private int galleryItemBackground;
        private Context context;

        public ImageAdapter(Context context) {
            this.context = context;

            TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
            galleryItemBackground = a.getResourceId(
                    R.styleable.Gallery1_android_galleryItemBackground, 0);
            a.recycle();
        }

        public int getCount() {
            return mPhotoFiles.size();
        }

        public Object getItem(int position) {
            return position;
        }

        //这个方法获得是呈现在用户面前的图像下标
        public long getItemId(int position) {
            //将此索引的图像设为imageOne显示
            mIVOne.setImageBitmap(UtilFun.getRotatedLocalBitmap(mPhotoFiles.get(position)));
            mIVOne.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return position;
        }

        //这个方法返回的ImageView就是实现拖动效果的图像
        public View getView(int position, View convertView, ViewGroup parent) {
            mIVAll= new ImageView(context);
            //设置图像资源
            mIVAll.setImageBitmap(UtilFun.getRotatedLocalBitmap(mPhotoFiles.get(position)));

            Size sz = ActivityCameraJobShowNew.SMAIL_IMAGEVIEW_SIZE;
            mIVAll.setLayoutParams(new Gallery.LayoutParams(sz.getWidth(), sz.getHeight()));
            //设置图像相对于视图的比例，FIT_XY表示充满X和Y轴
            mIVAll.setScaleType(ImageView.ScaleType.FIT_XY);
            //设置imageAll中每一个Item的背景资源
            mIVAll.setBackgroundResource(galleryItemBackground);
            return mIVAll;
        }
    }
}
