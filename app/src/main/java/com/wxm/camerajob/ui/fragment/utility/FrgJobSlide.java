package com.wxm.camerajob.ui.fragment.utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.wxm.camerajob.R;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wxm.andriodutillib.FrgUtility.FrgUtilityBase;
import cn.wxm.andriodutillib.type.MySize;
import cn.wxm.andriodutillib.util.ImageUtil;
import cn.wxm.andriodutillib.util.UtilFun;

/**
 * 任务 slide fragment
 * Created by 123 on 2016/10/14.
 */
public class FrgJobSlide extends FrgUtilityBase {

    private final static int  MSG_TYPE_TO_NEXT_PHOTO = 1;
    private final static MySize    GALLERY_SIZE = new MySize(200, 150);
    private final static MySize    SHOW_SIZE    = new MySize(1000, 750);

    private Timer                   mTimer;
    private FrgJobSlideMsgHandler   mSelfHandler;

    private LinkedList<String>  mLLPhotoFN = new LinkedList<>();

    @BindView(R.id.gy_photos)
    Gallery         mGYPhotos;

    @BindView(R.id.is_photo)
    ImageSwitcher   mISPhoto;

    @BindView(R.id.tv_tag)
    TextView        mTVTag;

    public static FrgJobSlide newInstance(List<String> photos) {
        FrgJobSlide ret =  new FrgJobSlide();
        ret.mLLPhotoFN.addAll(photos);

        return ret;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        if (null != view) {
            init_ui();


        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LOG_TAG = "FrgJobSlide";
        View rootView = inflater.inflate(R.layout.vw_job_show, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    protected void initUiComponent(View view) {

        // set timer
        // 使用定时器定时全面刷新显示
        mSelfHandler = new FrgJobSlideMsgHandler();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mSelfHandler.sendEmptyMessage(MSG_TYPE_TO_NEXT_PHOTO);
            }
        }, 300, 1500);
    }

    @Override
    protected void initUiInfo() {

    }

    /// BEGIN PRIVATE

    /**
     * 初始化UI
     */
    private void init_ui() {
        final Context home_context = getContext();

        // get tag
        int position = 0;
        String fn = mLLPhotoFN.get(position);
        int l_pos = fn.lastIndexOf("/") + 1;
        mTVTag.setText(String.format(Locale.CHINA, "%d/%d (%s)",
                    position + 1, mLLPhotoFN.size(), fn.substring(l_pos, fn.length())));

        // 设置动画效果
        mISPhoto.setInAnimation(AnimationUtils.loadAnimation(home_context,
                                    android.R.anim.fade_in));
        mISPhoto.setOutAnimation(AnimationUtils.loadAnimation(home_context,
                                    android.R.anim.fade_out));

        // 为imageSwitcher设置ViewFactory对象
        mISPhoto.setFactory(() -> {
            // 初始化一个ImageView对象
            // 设置保持纵横比居中缩放图像
            // 设置imageView的宽高
            ImageView imageView = new ImageView(home_context);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

            return imageView;
        });

        //初始化一个MainGalleryAdapter对象
        MainGalleryAdapter adapter = new MainGalleryAdapter();
        mGYPhotos.setAdapter(adapter);
        mGYPhotos.setSelection(position);
        mGYPhotos.setOnItemClickListener((parent, view, position1, id) -> {
            //在ImageSwitcher中显示选中的图片
            toPostion(position1);
        });
    }

    /**
     * 相册跳转到新位置
     * @param position  新位置
     */
    public void toPostion(int position) {
        Drawable cur_d = UtilFun.cast_t(mGYPhotos.getAdapter().getItem(position));
        mISPhoto.setImageDrawable(cur_d);

        String fn = mLLPhotoFN.get(position);
        int l_pos = fn.lastIndexOf("/") + 1;
        mTVTag.setText(String.format(Locale.CHINA, "%d/%d (%s)",
                    position + 1, mLLPhotoFN.size(), fn.substring(l_pos, fn.length())));
    }
    /// END PRIVATE


    /**
     * 定义Gallery的数据适配器MainGalleryAdapter
     */
    class MainGalleryAdapter extends BaseAdapter {

        /**
         * 获得数量
         */
        @Override
        public int getCount() {
            return mLLPhotoFN.size();
        }

        /**
         * 获得当前选项
         */
        @Override
        public Object getItem(int position) {
            Bitmap bp = ImageUtil.getRotatedLocalBitmap(mLLPhotoFN.get(position), SHOW_SIZE);
            return new BitmapDrawable(getResources(), bp);
        }

        /**
         * 获得当前选项的id
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 获得当前选项的视图
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //初始化一个ImageView对象
            //设置缩放方式
            //设置ImageView的宽高
            //设置IamgeView显示的图片
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new Gallery.LayoutParams(400, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            //int w = imageView.getWidth();
            //int h = imageView.getHeight();
            Bitmap bm = ImageUtil.getRotatedLocalBitmap(mLLPhotoFN.get(position), GALLERY_SIZE);
            imageView.setImageBitmap(bm);

            /**
             * 设置ImageView背景，这里背景使用的是android提供的一种背景风格
             * 在values/attr.xml文件中需要一下内容
             *  <declare-styleable name="Gallery">
             *      <attr name="android:galleryItemBackground" />
             *  </declare-styleable>
             */
            TypedArray typedArray = getActivity()
                    .obtainStyledAttributes(R.styleable.Gallery);
            int mGalleryItemBackground = typedArray.getResourceId(
                    R.styleable.Gallery_android_galleryItemBackground, 0);
            imageView.setBackgroundResource(mGalleryItemBackground);
            typedArray.recycle();

            //返回ImageView对象
            return imageView;
        }
    }

    /**
     * activity msg handler
     * Created by wxm on 2016/8/13.
     */
    private class FrgJobSlideMsgHandler extends Handler {
        private static final String TAG = "FrgJobSlideMsgHandler";

        FrgJobSlideMsgHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            if(isDetached() || isHidden())
                return;

            switch (msg.what) {
                case MSG_TYPE_TO_NEXT_PHOTO : {
                    int sz = mLLPhotoFN.size();
                    int cur_pos = mGYPhotos.getSelectedItemPosition();
                    int max_pos = sz - 1;

                    int next_pos = cur_pos + 1 <= max_pos ? cur_pos + 1 : 0;
                    mGYPhotos.setSelection(next_pos);
                    toPostion(next_pos);
                }
                break;

                default:
                    Log.e(TAG, String.format("msg(%s) can not process", msg.toString()));
                    break;
            }
        }
    }
}
