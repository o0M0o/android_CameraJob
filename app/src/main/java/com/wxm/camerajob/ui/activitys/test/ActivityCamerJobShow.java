package com.wxm.camerajob.ui.activitys.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.utility.UtilFun;

import java.util.LinkedList;

public class ActivityCamerJobShow extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory {
    private final String    TAG = "ActivityCamerJobShow";
    private Gallery         mGallery;
    private ImageSwitcher   mISSwitch;

    private LinkedList<String>  mPhotoFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camer_job_show);
        loadPhotos();

        mISSwitch = (ImageSwitcher)findViewById(R.id.acis_switch);
        mGallery = (Gallery)findViewById(R.id.acg_gallery);

        mISSwitch.setFactory(this);
        mISSwitch.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        mISSwitch.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));

        mGallery.setAdapter(new ImageAdapter(this));
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
    }

    @Override
    public View makeView() {
        return null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
        mISSwitch.setImageDrawable(Drawable.createFromPath(mPhotoFiles.get(position)));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }


    private class ImageAdapter extends BaseAdapter {
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mPhotoFiles.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);

            mISSwitch.setImageDrawable(Drawable.createFromPath(mPhotoFiles.get(position)));
            i.setAdjustViewBounds(true);
            i.setLayoutParams(new Gallery.LayoutParams(
                    Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.WRAP_CONTENT));
            //i.setBackgroundResource(R.drawable.e);
            return i;
        }

        private Context mContext;

    }
}
