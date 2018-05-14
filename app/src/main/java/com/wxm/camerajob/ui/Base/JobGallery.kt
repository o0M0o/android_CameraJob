package com.wxm.camerajob.ui.Base

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast

import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.QueueProcessingType
import com.wxm.camerajob.utility.UILImageLoader

import org.xutils.x

import java.io.File

import cn.finalteam.galleryfinal.CoreConfig
import cn.finalteam.galleryfinal.FunctionConfig
import cn.finalteam.galleryfinal.GalleryFinal
import cn.finalteam.galleryfinal.PauseOnScrollListener
import cn.finalteam.galleryfinal.ThemeConfig
import cn.finalteam.galleryfinal.model.PhotoInfo


/**
 * gallery for job
 * Created by 123 on 2016/8/14.
 */
class JobGallery {
    private var mAC: Activity? = null

    private val mOnHanlderResultCallback = object : GalleryFinal.OnHanlderResultCallback {
        override fun onHanlderSuccess(reqeustCode: Int, resultList: List<PhotoInfo>?) {
            if (resultList != null) {
                val TAG = "JobGallery"
                Log.i(TAG, "open gallery success")
            }
        }

        override fun onHanlderFailure(requestCode: Int, errorMsg: String) {
            Toast.makeText(mAC, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * open gallery
     * @param ac        for activity
     * @param photodir  path for gallery
     */
    fun OpenGallery(ac: Activity, photodir: String) {
        mAC = ac

        val themeConfig = ThemeConfig.DEFAULT

        val functionConfigBuilder = FunctionConfig.Builder()
        val imageLoader: cn.finalteam.galleryfinal.ImageLoader
        imageLoader = UILImageLoader()
        val pauseOnScrollListener = UILPauseOnScrollListener(false, true)

        functionConfigBuilder.setMutiSelectMaxSize(8)
        functionConfigBuilder.setEnablePreview(true)

        //functionConfigBuilder.setSelected(mPhotoList);//添加过滤集合
        val functionConfig = functionConfigBuilder.build()

        val pp = File(photodir)
        val coreConfig = CoreConfig.Builder(mAC, imageLoader, themeConfig)
                .setFunctionConfig(functionConfig)
                .setPauseOnScrollListener(pauseOnScrollListener)
                .setNoAnimcation(false)
                .setShowPhotoFolder(pp)
                .build()
        GalleryFinal.init(coreConfig)

        val REQUEST_CODE_GALLERY = 1001
        GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY,
                functionConfig, mOnHanlderResultCallback)

        initImageLoader(mAC)
        initFresco()
        x.Ext.init(mAC!!.application)
    }


    private fun initImageLoader(context: Context?) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        val config = ImageLoaderConfiguration.Builder(context!!)
        config.threadPriority(Thread.NORM_PRIORITY - 2)
        config.denyCacheImageMultipleSizesInMemory()
        config.diskCacheFileNameGenerator(Md5FileNameGenerator())
        config.diskCacheSize(50 * 1024 * 1024) // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO)
        config.writeDebugLogs() // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build())
    }


    private fun initFresco() {
        val config = ImagePipelineConfig.newBuilder(mAC!!)
                .setBitmapsConfig(Bitmap.Config.ARGB_8888)
                .build()
        Fresco.initialize(mAC!!, config)
    }
}
