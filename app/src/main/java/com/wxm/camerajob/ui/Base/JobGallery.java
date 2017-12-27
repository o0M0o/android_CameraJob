package com.wxm.camerajob.ui.Base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.wxm.camerajob.utility.UILImageLoader;

import org.xutils.x;

import java.io.File;
import java.util.List;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.PauseOnScrollListener;
import cn.finalteam.galleryfinal.ThemeConfig;
import cn.finalteam.galleryfinal.model.PhotoInfo;

/**
 * gallery for job
 * Created by 123 on 2016/8/14.
 */
public class JobGallery {
    private Activity mAC;

    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null) {
                String TAG = "JobGallery";
                Log.i(TAG, "open gallery success");
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            Toast.makeText(mAC, errorMsg, Toast.LENGTH_SHORT).show();
        }
    };


    public JobGallery()   {
    }

    /**
     * open gallery
     * @param ac        for activity
     * @param photodir  path for gallery
     */
    public void OpenGallery(Activity ac, String photodir)   {
        mAC = ac;

        ThemeConfig themeConfig = ThemeConfig.DEFAULT;

        FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
        cn.finalteam.galleryfinal.ImageLoader imageLoader;
        imageLoader = new UILImageLoader();
        PauseOnScrollListener pauseOnScrollListener =
                new UILPauseOnScrollListener(false, true);

        functionConfigBuilder.setMutiSelectMaxSize(8);
        functionConfigBuilder.setEnablePreview(true);

        //functionConfigBuilder.setSelected(mPhotoList);//添加过滤集合
        final FunctionConfig functionConfig = functionConfigBuilder.build();

        File pp = new File(photodir);
        CoreConfig coreConfig = new CoreConfig.Builder(mAC, imageLoader, themeConfig)
                .setFunctionConfig(functionConfig)
                .setPauseOnScrollListener(pauseOnScrollListener)
                .setNoAnimcation(false)
                .setShowPhotoFolder(pp)
                .build();
        GalleryFinal.init(coreConfig);

        int REQUEST_CODE_GALLERY = 1001;
        GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY,
                functionConfig, mOnHanlderResultCallback);

        initImageLoader(mAC);
        initFresco();
        x.Ext.init(mAC.getApplication());
    }


    private void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }


    private void initFresco() {
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(mAC)
                .setBitmapsConfig(Bitmap.Config.ARGB_8888)
                .build();
        Fresco.initialize(mAC, config);
    }
}
