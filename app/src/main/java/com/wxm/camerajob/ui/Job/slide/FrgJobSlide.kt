package com.wxm.camerajob.ui.Job.slide

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.Gallery
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.TextView

import com.wxm.camerajob.R

import java.util.LinkedList
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

import kotterknife.bindView
import wxm.androidutil.FrgUtility.FrgSupportBaseAdv
import wxm.androidutil.type.MySize
import wxm.androidutil.util.UtilFun
import wxm.androidutil.ImageUtility.ImageUtil

/**
 * slide fragment for job
 * Created by WangXM on 2016/10/14.
 */
class FrgJobSlide : FrgSupportBaseAdv() {
    private val mTimer: Timer = Timer()
    private val mLLPhotoFN = LinkedList<String>()

    private val mGYPhotos: Gallery by bindView(R.id.gy_photos)
    private val mISPhoto: ImageSwitcher by bindView(R.id.is_photo)
    private val mTVTag: TextView by bindView(R.id.tv_tag)

    override fun onDetach() {
        super.onDetach()
        mTimer.cancel()
    }

    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.vw_job_slide

    override fun initUI(savedInstanceState: Bundle?) {
        // 设置动画效果
        mISPhoto.inAnimation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in)
        mISPhoto.outAnimation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_out)

        mGYPhotos.adapter = MainGalleryAdapter()
        mGYPhotos.setOnItemClickListener { _, _, position1, _ ->
            toPosition(position1)
        }

        // 为imageSwitcher设置ViewFactory对象
        mISPhoto.setFactory {
            // 初始化一个ImageView对象
            // 设置保持纵横比居中缩放图像
            // 设置imageView的宽高
            ImageView(activity).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT)
            }
        }

        mTimer.schedule(object : TimerTask() {
            override fun run() {
                activity.runOnUiThread({
                    if (!isVisible)
                        return@runOnUiThread

                    val nextPos = (mGYPhotos.selectedItemPosition + 1) % mLLPhotoFN.size
                    mGYPhotos.setSelection(nextPos)
                    toPosition(nextPos)
                })
            }
        }, 300, 1500)

        loadUI(savedInstanceState)
    }

    override fun loadUI(savedInstanceState: Bundle?) {
        toPosition(0)
    }

    /// BEGIN PRIVATE
    /**
     * 相册跳转到新位置
     * @param position  新位置
     */
    fun toPosition(position: Int) {
        mISPhoto.setImageDrawable(UtilFun.cast_t<Drawable>(mGYPhotos.adapter.getItem(position)))

        val fn = mLLPhotoFN[position]
        (fn.lastIndexOf("/") + 1).let {
            mTVTag.text = String.format(Locale.CHINA, "%d/%d (%s)",
                    position + 1, mLLPhotoFN.size, fn.substring(it, fn.length))
        }
    }
    /// END PRIVATE
    /**
     * 定义Gallery的数据适配器MainGalleryAdapter
     */
    internal inner class MainGalleryAdapter : BaseAdapter() {
        /**
         * 获得数量
         */
        override fun getCount(): Int {
            return mLLPhotoFN.size
        }

        /**
         * 获得当前选项
         */
        override fun getItem(position: Int): Any {
            return BitmapDrawable(resources,
                    ImageUtil.getRotatedLocalBitmap(mLLPhotoFN[position], SHOW_SIZE))
        }

        /**
         * 获得当前选项的id
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * 获得当前选项的视图
         */
        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            return ImageView(activity).apply {
                layoutParams = Gallery.LayoutParams(400, ViewGroup.LayoutParams.MATCH_PARENT)
                scaleType = ImageView.ScaleType.FIT_CENTER

                //int w = imageView.getWidth();
                //int h = imageView.getHeight();
                val bm = ImageUtil.getRotatedLocalBitmap(mLLPhotoFN[position], GALLERY_SIZE)
                setImageBitmap(bm)

                val typedArray = activity.obtainStyledAttributes(R.styleable.Gallery)
                val mGalleryItemBackground = typedArray.getResourceId(
                        R.styleable.Gallery_android_galleryItemBackground, 0)
                setBackgroundResource(mGalleryItemBackground)
                typedArray.recycle()
            }
        }
    }

    companion object {
        private val GALLERY_SIZE = MySize(200, 150)
        private val SHOW_SIZE = MySize(1000, 750)

        fun newInstance(photos: List<String>): FrgJobSlide {
            return FrgJobSlide().apply { mLLPhotoFN.addAll(photos) }
        }
    }
}

