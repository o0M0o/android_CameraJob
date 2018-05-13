package com.wxm.camerajob.utility

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable

import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.ImageSize
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware

import cn.finalteam.galleryfinal.widget.GFImageView

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
        val options = DisplayImageOptions.Builder().cacheOnDisk(false)
                .cacheInMemory(false).bitmapConfig(mImageConfig)
                .build()

        val imageSize = ImageSize(width, height)
        ImageLoader.getInstance().displayImage("file://$path",
                ImageViewAware(imageView), options, imageSize, null, null)
    }

    override fun clearMemoryCache() {}
}
