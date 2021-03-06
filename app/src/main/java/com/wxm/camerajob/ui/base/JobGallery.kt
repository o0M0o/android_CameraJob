package com.wxm.camerajob.ui.base

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import cn.finalteam.galleryfinal.CoreConfig
import cn.finalteam.galleryfinal.FunctionConfig
import cn.finalteam.galleryfinal.GalleryFinal
import cn.finalteam.galleryfinal.ThemeConfig
import cn.finalteam.galleryfinal.model.PhotoInfo
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.QueueProcessingType
import org.xutils.x
import wxm.androidutil.improve.let1
import wxm.androidutil.log.TagLog
import java.io.File


/**
 * gallery for job
 * Created by WangXM on 2016/8/14.
 */
class JobGallery {
    companion object {
        private const val REQUEST_CODE_GALLERY = 1001
    }

    private lateinit var mHolder: Activity

    private val mResultCallback = object : GalleryFinal.OnHanlderResultCallback {
        override fun onHanlderSuccess(reqeustCode: Int, resultList: List<PhotoInfo>?) {
            if (resultList != null) {
                TagLog.i("open gallery success")
            }
        }

        override fun onHanlderFailure(requestCode: Int, errorMsg: String) {
            Toast.makeText(mHolder, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * open gallery
     * @param ac        for activity
     * @param photoDir  path for gallery
     */
    fun openGallery(ac: Activity, photoDir: String) {
        mHolder = ac

        //funConfigBuilder.setSelected(mPhotoList);//添加过滤集合
        CoreConfig.Builder(mHolder, UILImageLoader(), ThemeConfig.DEFAULT)
                .setPauseOnScrollListener(UILPauseOnScrollListener(false, true))
                .setNoAnimcation(false)
                .setShowPhotoFolder(File(photoDir))
                .build().let {
                    GalleryFinal.init(it)
                }

        GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY,
                FunctionConfig.Builder().apply {
                    setMutiSelectMaxSize(8)
                    setEnablePreview(true)
                }.build(), mResultCallback)

        initImageLoader(mHolder)
        initFresco()
        x.Ext.init(mHolder.application)
    }


    private fun initImageLoader(context: Context?) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration.Builder(context!!).apply {
            threadPriority(Thread.NORM_PRIORITY - 2)
            denyCacheImageMultipleSizesInMemory()
            diskCacheFileNameGenerator(Md5FileNameGenerator())
            diskCacheSize(50 * 1024 * 1024) // 50 MiB
            tasksProcessingOrder(QueueProcessingType.LIFO)
            writeDebugLogs() // Remove for release app
        }.let1 {
            // Initialize ImageLoader with configuration.
            ImageLoader.getInstance().init(it.build())
        }
    }


    private fun initFresco() {
        ImagePipelineConfig.newBuilder(mHolder)
                .setBitmapsConfig(Bitmap.Config.ARGB_8888)
                .build().let {
                    Fresco.initialize(mHolder, it)
                }
    }
}
