package com.wxm.camerajob.ui.job.slide

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import com.wxm.camerajob.R
import kotterknife.bindView
import wxm.androidutil.image.ImageUtil
import wxm.androidutil.improve.let1
import wxm.androidutil.type.MySize
import wxm.androidutil.ui.frg.FrgSupportBaseAdv
import wxm.androidutil.util.UtilFun
import java.util.*

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
    override fun getLayoutID(): Int = R.layout.pg_job_slide

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
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT)
            }
        }

        mTimer.schedule(object : TimerTask() {
            override fun run() {
                if (isVisible) {
                    activity!!.runOnUiThread {
                        ((mGYPhotos.selectedItemPosition + 1) % mLLPhotoFN.size).let {
                            mGYPhotos.setSelection(it)
                            toPosition(it)
                        }
                    }
                }
            }
        }, 300, 1500)

        loadUI(savedInstanceState)
    }

    override fun loadUI(savedInstanceState: Bundle?) {
        toPosition(0)
    }

    /// BEGIN PRIVATE
    /**
     * gallery jump to [position]
     */
    fun toPosition(position: Int) {
        mISPhoto.setImageDrawable(UtilFun.cast_t<Drawable>(mGYPhotos.adapter.getItem(position)))

        mLLPhotoFN[position].let1 { fn ->
            mTVTag.text = String.format(Locale.CHINA, "%d/%d (%s)",
                    position + 1, mLLPhotoFN.size,
                    fn.substring(fn.lastIndexOf("/") + 1, fn.length))
        }
    }
    /// END PRIVATE
    /**
     * for gallery
     */
    internal inner class MainGalleryAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return mLLPhotoFN.size
        }

        override fun getItem(position: Int): Any {
            return BitmapDrawable(resources,
                    ImageUtil.getRotatedLocalBitmap(mLLPhotoFN[position], SHOW_SIZE))
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return ImageView(activity).apply {
                layoutParams = Gallery.LayoutParams(400, ViewGroup.LayoutParams.MATCH_PARENT)
                scaleType = ImageView.ScaleType.FIT_CENTER

                setImageBitmap(ImageUtil.getRotatedLocalBitmap(mLLPhotoFN[position], GALLERY_SIZE)!!)

                val ta = activity!!.obtainStyledAttributes(R.styleable.Gallery)
                setBackgroundResource(ta.getResourceId(
                        R.styleable.Gallery_android_galleryItemBackground, 0))
                ta.recycle()
            }
        }
    }

    companion object {
        private val GALLERY_SIZE = MySize(200, 150)
        private val SHOW_SIZE = MySize(1024, 1024)

        fun newInstance(photos: List<String>): FrgJobSlide {
            return FrgJobSlide().apply { mLLPhotoFN.addAll(photos) }
        }
    }
}

