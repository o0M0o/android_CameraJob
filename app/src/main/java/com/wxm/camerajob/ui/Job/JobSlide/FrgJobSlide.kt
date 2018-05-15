package com.wxm.camerajob.ui.Job.JobSlide

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
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

import butterknife.BindView
import butterknife.ButterKnife
import wxm.androidutil.FrgUtility.FrgUtilityBase
import wxm.androidutil.type.MySize
import wxm.androidutil.util.ImageUtil
import wxm.androidutil.util.UtilFun

/**
 * slide fragment for job
 * Created by WangXM on 2016/10/14.
 */
class FrgJobSlide : FrgUtilityBase() {

    private var mTimer: Timer? = null
    private val mLLPhotoFN = LinkedList<String>()

    @BindView(R.id.gy_photos)
    internal var mGYPhotos: Gallery? = null

    @BindView(R.id.is_photo)
    internal var mISPhoto: ImageSwitcher? = null

    @BindView(R.id.tv_tag)
    internal var mTVTag: TextView? = null

    protected fun leaveActivity() {
        mTimer!!.cancel()
        super.leaveActivity()
    }

    protected fun inflaterView(inflater: LayoutInflater, container: ViewGroup, bundle: Bundle): View {
        LOG_TAG = "FrgJobSlide"
        val rootView = inflater.inflate(R.layout.vw_job_slide, container, false)
        ButterKnife.bind(this, rootView)
        return rootView
    }

    protected fun initUiComponent(view: View) {
        // set timer
        // 使用定时器定时全面刷新显示
        mTimer = Timer()
        mTimer!!.schedule(object : TimerTask() {
            override fun run() {
                getActivity().runOnUiThread({
                    if (isDetached() || isHidden())
                        return@getActivity ().runOnUiThread

                    val sz = mLLPhotoFN.size
                    val cur_pos = mGYPhotos!!.selectedItemPosition
                    val max_pos = sz - 1

                    val next_pos = if (cur_pos + 1 <= max_pos) cur_pos + 1 else 0
                    mGYPhotos!!.setSelection(next_pos)
                    toPostion(next_pos)
                })
            }
        }, 300, 1500)
    }

    protected fun loadUI() {
        val home_context = getActivity()

        // get tag
        val position = 0
        val fn = mLLPhotoFN[position]
        val l_pos = fn.lastIndexOf("/") + 1
        mTVTag!!.text = String.format(Locale.CHINA, "%d/%d (%s)",
                position + 1, mLLPhotoFN.size, fn.substring(l_pos, fn.length))

        // 设置动画效果
        mISPhoto!!.inAnimation = AnimationUtils.loadAnimation(home_context,
                android.R.anim.fade_in)
        mISPhoto!!.outAnimation = AnimationUtils.loadAnimation(home_context,
                android.R.anim.fade_out)

        // 为imageSwitcher设置ViewFactory对象
        mISPhoto!!.setFactory {
            // 初始化一个ImageView对象
            // 设置保持纵横比居中缩放图像
            // 设置imageView的宽高
            val imageView = ImageView(home_context)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

            imageView
        }

        //初始化一个MainGalleryAdapter对象
        val adapter = MainGalleryAdapter()
        mGYPhotos!!.adapter = adapter
        mGYPhotos!!.setSelection(position)
        mGYPhotos!!.setOnItemClickListener { parent, view, position1, id ->
            //在ImageSwitcher中显示选中的图片
            toPostion(position1)
        }
    }

    /// BEGIN PRIVATE

    /**
     * 相册跳转到新位置
     * @param position  新位置
     */
    fun toPostion(position: Int) {
        val cur_d = UtilFun.cast_t<Drawable>(mGYPhotos!!.adapter.getItem(position))
        mISPhoto!!.setImageDrawable(cur_d)

        val fn = mLLPhotoFN[position]
        val l_pos = fn.lastIndexOf("/") + 1
        mTVTag!!.text = String.format(Locale.CHINA, "%d/%d (%s)",
                position + 1, mLLPhotoFN.size, fn.substring(l_pos, fn.length))
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
            val bp = ImageUtil.getRotatedLocalBitmap(mLLPhotoFN[position], SHOW_SIZE)
            return BitmapDrawable(getResources(), bp)
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
            //初始化一个ImageView对象
            //设置缩放方式
            //设置ImageView的宽高
            //设置IamgeView显示的图片
            val imageView = ImageView(getActivity())
            imageView.layoutParams = Gallery.LayoutParams(400, ViewGroup.LayoutParams.MATCH_PARENT)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER

            //int w = imageView.getWidth();
            //int h = imageView.getHeight();
            val bm = ImageUtil.getRotatedLocalBitmap(mLLPhotoFN[position], GALLERY_SIZE)
            imageView.setImageBitmap(bm)

            /*
             * 设置ImageView背景，这里背景使用的是android提供的一种背景风格
             * 在values/attr.xml文件中需要一下内容
             *  <declare-styleable paraName="Gallery">
             *      <attr paraName="android:galleryItemBackground" />
             *  </declare-styleable>
             */
            val typedArray = getActivity()
                    .obtainStyledAttributes(R.styleable.Gallery)
            val mGalleryItemBackground = typedArray.getResourceId(
                    R.styleable.Gallery_android_galleryItemBackground, 0)
            imageView.setBackgroundResource(mGalleryItemBackground)
            typedArray.recycle()

            //返回ImageView对象
            return imageView
        }
    }

    companion object {
        private val GALLERY_SIZE = MySize(200, 150)
        private val SHOW_SIZE = MySize(1000, 750)

        fun newInstance(photos: List<String>): FrgJobSlide {
            val ret = FrgJobSlide()
            ret.mLLPhotoFN.addAll(photos)

            return ret
        }
    }
}

