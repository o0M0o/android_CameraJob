package com.wxm.camerajob.ui.activitys;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.data.MySize;
import com.wxm.camerajob.base.utility.ContextUtil;
import com.wxm.camerajob.base.utility.UtilFun;

import java.util.LinkedList;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ActivityCameraJobPhotos extends AppCompatActivity {
    private final static String TAG = "ACCameraJobPhotos";
    private final static MySize SMAIL_IMAGEVIEW_SIZE = new MySize(240, 240);

    private TextView            mTVTip;
    //private ImageView           mIVAll;
    private ImageView           mIVOne;
    private LinkedList<String>  mPhotoFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_camerajob_photos);
        ContextUtil.getInstance().addActivity(this);

        mPhotoFiles = new LinkedList<>();
        loadPhotos();

        //mIVAll = new ImageView(this);
        mIVOne = (ImageView)findViewById(R.id.aciv_cjs_show);
        assert mIVOne != null;
        mIVOne.setScaleType(ImageView.ScaleType.FIT_CENTER);

        mTVTip = (TextView)findViewById(R.id.actv_cjs_text);
        assert mTVTip != null;

        Gallery mGGallery = (Gallery) findViewById(R.id.acg_cjs_gallery);
        assert mGGallery != null;
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
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "screen changed");

/*        Rect rt = new Rect();
        mIVOne.getDrawingRect(rt);
        mSZPreference = new Size(rt.width(), rt.height());*/
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
        mPhotoFiles.clear();
        Intent data = getIntent();
        if(null != data)    {
            String p = data.getStringExtra(GlobalDef.STR_LOAD_PHOTODIR);
            if(!UtilFun.StringIsNullOrEmpty(p))   {
                mPhotoFiles.addAll(UtilFun.getDirFiles(p, "jpg", false));
            }
        }

        for(String fn : mPhotoFiles)    {
            Log.i(TAG, "photo file : " + fn);
        }
    }


    class ImageAdapter
            extends BaseAdapter {
        //每一个gallery中图像的背景资源
        private final int galleryItemBackground;
        private final Context context;

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
            String fn = mPhotoFiles.get(position);

            //将此索引的图像设为imageOne显示
            MySize sz = new MySize(1024, 1024);
            mIVOne.setImageBitmap(UtilFun.getRotatedLocalBitmap(fn, sz));

            int pos = fn.lastIndexOf("/");
            if(pos > 0)
                mTVTip.setText(fn.substring(pos + 1));
            else
                mTVTip.setText(R.string.cn_unknown_photofile);
            return position;
        }

        //这个方法返回的ImageView就是实现拖动效果的图像
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView mIVAll = new ImageView(context);

            //设置图像资源
            //设置图像相对于视图的比例，FIT_XY表示充满X和Y轴
            //设置imageAll中每一个Item的背景资源
            MySize sz = ActivityCameraJobPhotos.SMAIL_IMAGEVIEW_SIZE;
            mIVAll.setLayoutParams(new Gallery.LayoutParams(sz.getWidth(), sz.getHeight()));
            mIVAll.setScaleType(ImageView.ScaleType.FIT_XY);
            mIVAll.setBackgroundResource(galleryItemBackground);
            mIVAll.setImageBitmap(UtilFun.getRotatedLocalBitmap(mPhotoFiles.get(position), sz));
            return mIVAll;
        }
    }
}
