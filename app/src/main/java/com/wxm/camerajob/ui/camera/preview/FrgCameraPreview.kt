package com.wxm.camerajob.ui.camera.preview

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.RequiresApi
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import com.wxm.camerajob.R
import com.wxm.camerajob.data.define.CameraParam
import com.wxm.camerajob.data.define.PreferencesUtil
import com.wxm.camerajob.ui.base.AutoFitTextureView
import com.wxm.camerajob.ui.utility.dialog.DlgUtility
import com.wxm.camerajob.utility.log.TagLog
import kotterknife.bindView
import wxm.androidutil.FrgUtility.FrgSupportBaseAdv
import wxm.androidutil.type.MySize
import wxm.androidutil.util.UtilFun
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * fragment for camera preview
 * Created by WangXM on 2016/10/14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FrgCameraPreview : FrgSupportBaseAdv() {
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mCameraDevice: CameraDevice? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mPreviewSize: MySize? = null

    private var mCameraId: String? = null
    private val mCameraOpenCloseLock = Semaphore(1)
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null

    private var mFlashSupported: Boolean = false
    private var mState = STATE_PREVIEW

    private var mCPParam: CameraParam? = null

    private val mTextureView: AutoFitTextureView by bindView(R.id.frag_camera_textureview)

    private val mCameraDeviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraOpenCloseLock.release()
            mCameraDevice = camera
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            mCameraOpenCloseLock.release()
            camera.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            camera.close()
            mCameraDevice = null
            activity.finish()
        }
    }

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            closeCamera()
            openCamera(mCPParam!!.mFace,
                    mCPParam!!.mPhotoSize.width, mCPParam!!.mPhotoSize.height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}
    }

    private val mSessionStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            // The camera is already closed
            if (null == mCameraDevice)
                return

            // When the session is ready, we start displaying the preview.
            mCaptureSession = session
            try {
                // Auto focus should be continuous for camera preview.
                mPreviewRequestBuilder!!.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                // Flash is automatically enabled when necessary.
                if (mFlashSupported) {
                    mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AE_MODE,
                            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                }

                // Finally, we start displaying the camera preview.
                val mPreviewRequest = mPreviewRequestBuilder!!.build()
                mCaptureSession!!.setRepeatingRequest(mPreviewRequest,
                        mCaptureCallback, mBackgroundHandler)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            showToast("Failed")
        }
    }


    /**
     * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
     */
    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_PREVIEW -> {
                }// We have nothing to do when the camera preview is working normally.
                STATE_WAITING_LOCK -> {
                }/*
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    else    {
                        captureStillPicture();
                    }
                    */
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                }/*
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    */
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult) {
            process(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult) {
            process(result)
        }
    }

    override fun isUseEventBus(): Boolean = false
    override fun getLayoutID(): Int = R.layout.frg_camera

    override fun initUI(savedInstanceState: Bundle?) {
        mCPParam = PreferencesUtil.loadCameraParam()
        mTextureView.surfaceTextureListener = mSurfaceTextureListener
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable) {
            val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                manager.getCameraCharacteristics(mCameraId!!).get(CameraCharacteristics.LENS_FACING)?.let {
                    openCamera(it, mTextureView.width, mTextureView.height)
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }

        }
        /*
        else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        */
    }

    protected fun leaveActivity() {
        closeCamera()
        stopBackgroundThread()
    }

    /// BEGIN PRIVATE
    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        if (null != mBackgroundThread) {
            mBackgroundThread!!.quitSafely()
            try {
                mBackgroundThread!!.join()
                mBackgroundThread = null
                mBackgroundHandler = null
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Creates a new [CameraCaptureSession] for camera preview.
     */
    private fun createCameraPreviewSession() {
        try {
            mTextureView.surfaceTexture!!.let {
                // We configure the size of default buffer to be the size of camera preview we want.
                it.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)

                // This is the output Surface we need to start preview.
                Surface(it).let {
                    // We set up a CaptureRequest.Builder with the output Surface.
                    mPreviewRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    mPreviewRequestBuilder!!.addTarget(it)

                    // Here, we create a CameraCaptureSession for camera preview.
                    mCameraDevice!!.createCaptureSession(listOf(it),
                            mSessionStateCallback, null)
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    /**
     * Opens the camera specified by
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun openCamera(face: Int, width: Int, height: Int) {
        setUpCameraOutputs(face, width, height)
        if (UtilFun.StringIsNullOrEmpty(mCameraId))
            return

        configureTransform(width, height)
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }

            (activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager)
                    .openCamera(mCameraId!!, mCameraDeviceStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    /**
     * Closes the current [CameraDevice].
     */
    private fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            if (null != mCaptureSession) {
                mCaptureSession!!.close()
                mCaptureSession = null
            }
            if (null != mCameraDevice) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
        }
    }

    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        if (null == mPreviewSize)
            return

        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, mPreviewSize!!.height.toFloat(), mPreviewSize!!.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(viewHeight.toFloat() / mPreviewSize!!.height,
                    viewWidth.toFloat() / mPreviewSize!!.width)
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        mTextureView.setTransform(matrix)
    }


    /**
     * Sets up member variables related to camera.
     *
     * @param face   facing lens or backing lens
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setUpCameraOutputs(face: Int, width: Int, height: Int) {
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            manager.cameraIdList.filter {
                manager.getCameraCharacteristics(it).let {
                    null != it
                            && face == it.get(CameraCharacteristics.LENS_FACING)
                            && null != it.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                }
            }.forEach {
                val characteristics = manager.getCameraCharacteristics(it)
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

                val lsSize = ArrayList<MySize>().apply {
                    addAll(map.getOutputSizes(ImageFormat.JPEG)
                            .filterNotNull().map { MySize(it.width, it.height) })
                }

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val mLargestSize = Collections.max(lsSize, CompareSizesByArea())

                val mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                val swappedDimensions = activity.windowManager.defaultDisplay.rotation.let {
                    when (it) {
                        Surface.ROTATION_0, Surface.ROTATION_180 -> mSensorOrientation == 90 || mSensorOrientation == 270
                        Surface.ROTATION_90, Surface.ROTATION_270 -> mSensorOrientation == 0 || mSensorOrientation == 180
                        else -> {
                            TagLog.e("Display rotation is invalid: $it")
                            false
                        }
                    }
                }

                val displaySize = Point().apply {
                    activity.windowManager.defaultDisplay.getSize(this)
                }

                val rotatedPreviewWidth: Int
                val rotatedPreviewHeight: Int
                var maxPreviewWidth: Int
                var maxPreviewHeight: Int
                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                } else {
                    rotatedPreviewWidth = width
                    rotatedPreviewHeight = height
                    maxPreviewWidth = displaySize.x
                    maxPreviewHeight = displaySize.y
                }

                maxPreviewWidth = Math.min(MAX_PREVIEW_WIDTH, maxPreviewWidth)
                maxPreviewHeight = Math.min(MAX_PREVIEW_HEIGHT, maxPreviewHeight)

                val lsSTSize = map.getOutputSizes(SurfaceTexture::class.java).filterNotNull()
                        .map { MySize(it.width, it.height) }.let {
                            ArrayList<MySize>().apply {
                                addAll(it)
                            }
                        }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(lsSTSize.toTypedArray(),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, mLargestSize)

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                resources.configuration.orientation.let {
                    if (it == Configuration.ORIENTATION_LANDSCAPE) {
                        mTextureView.setAspectRatio(mPreviewSize!!.width, mPreviewSize!!.height)
                    } else {
                        mTextureView.setAspectRatio(mPreviewSize!!.height, mPreviewSize!!.width)
                    }
                }

                // Check if the flash is supported.
                mFlashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                mCameraId = it
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            DlgUtility.showAlert(activity, "Error", getString(R.string.camera_error))
        }
    }

    /**
     * Compares two `Size`s based on their areas.
     */
    private class CompareSizesByArea : Comparator<MySize> {
        override fun compare(lhs: MySize, rhs: MySize): Int {
            // We cast here to ensure the multiplications won't overflow
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }
    }

    /**
     * Shows a [Toast] on the UI thread.
     *
     * @param text The message to show
     */
    private fun showToast(text: String) {
        activity.let {
            it.runOnUiThread({ Toast.makeText(it, text, Toast.LENGTH_SHORT).show() })
        }
    }

    companion object {
        private val LOG_TAG = ::FrgCameraPreview.javaClass.simpleName
        private const val FRAGMENT_DIALOG = "dialog"

        private val ORIENTATIONS = SparseIntArray()
        private const val STATE_PREVIEW = 0
        private const val STATE_WAITING_LOCK = 1
        private const val STATE_WAITING_PRECAPTURE = 2
        private const val STATE_WAITING_NON_PRECAPTURE = 3
        private const val STATE_PICTURE_TAKEN = 4

        /**
         * Max preview width that is guaranteed by Camera2 API
         * Max preview height that is guaranteed by Camera2 API
         */
        private const val MAX_PREVIEW_WIDTH = 1920
        private const val MAX_PREVIEW_HEIGHT = 1080

        fun newInstance(): FrgCameraPreview {
            return FrgCameraPreview()
        }

        /**
         * Given `choices` of `Size`s supported by a camera, choose the smallest one that
         * is at least as large as the respective texture view size, and that is at most as large as the
         * respective max size, and whose aspect ratio matches with the specified value. If such size
         * doesn't exist, choose the largest one that is at most as large as the respective max size,
         * and whose aspect ratio matches with the specified value.
         *
         * @param choices           The list of sizes that the camera supports for the intended output
         * class
         * @param textureViewWidth  The width of the texture view relative to sensor coordinate
         * @param textureViewHeight The height of the texture view relative to sensor coordinate
         * @param maxWidth          The maximum width that can be chosen
         * @param maxHeight         The maximum height that can be chosen
         * @param aspectRatio       The aspect ratio
         * @return The optimal `Size`, or an arbitrary one if none were big enough
         */
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private fun chooseOptimalSize(choices: Array<MySize>, textureViewWidth: Int,
                                      textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: MySize): MySize {
            // Collect the supported resolutions that are at least as big as the preview Surface
            // Collect the supported resolutions that are smaller than the preview Surface
            val bigEnough = ArrayList<MySize>()
            val notBigEnough = ArrayList<MySize>()
            val hw = aspectRatio.height / aspectRatio.width
            choices.forEach {
                if (it.width <= maxWidth && it.height <= maxHeight && it.height == it.width * hw) {
                    if (it.width >= textureViewWidth && it.height >= textureViewHeight) {
                        bigEnough.add(it)
                    } else {
                        notBigEnough.add(it)
                    }
                }
            }

            // Pick the smallest of those big enough. If there is no one big enough, pick the
            // largest of those not big enough.
            return when {
                bigEnough.size > 0 -> Collections.min(bigEnough, CompareSizesByArea())
                notBigEnough.size > 0 -> Collections.max(notBigEnough, CompareSizesByArea())
                else -> {
                    TagLog.e("Couldn't find any suitable preview size")
                    choices[0]
                }
            }
        }
    }
    /// END PRIVATE

}
