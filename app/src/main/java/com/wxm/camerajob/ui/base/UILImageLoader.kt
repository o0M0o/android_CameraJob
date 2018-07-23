package com.wxm.camerajob.ui.base

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import cn.finalteam.galleryfinal.widget.GFImageView
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageSize
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware
import wxm.androidutil.improve.let1

/**
 * Desction :   image loader
 * Author   :   pengjianbo
 * Date     :   15/10/10 下午5:52
 */
class UILImageLoader @JvmOverloads constructor(private val mImageConfig: Bitmap.Config = Bitmap.Config.RGB_565)
    : cn.finalteam.galleryfinal.ImageLoader {
    override fun displayImage(activity: Activity, path: String,
                              imageView: GFImageView, defaultDrawable: Drawable,
                              width: Int, height: Int) {
        DisplayImageOptions.Builder().cacheOnDisk(false)
                .cacheInMemory(false).bitmapConfig(mImageConfig)
                .build().let1 {
                    ImageLoader.getInstance().displayImage("file://$path",
                            ImageViewAware(imageView), it, ImageSize(width, height), null, null)
                }
    }

    override fun clearMemoryCache() {}
}
