/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cash.z.android.cameraview.api21

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import android.util.SparseIntArray
import androidx.annotation.NonNull
import androidx.annotation.RequiresPermission
import cash.z.android.cameraview.CameraView
import cash.z.android.cameraview.base.*
import android.os.HandlerThread



@TargetApi(21)
internal open class Camera2(callback: CameraViewImpl.Callback, preview: PreviewImpl, context: Context) : CameraViewImpl(callback, preview) {

    private val mCameraManager: CameraManager

    var firebaseCallback: CameraView.FirebaseCallback? = null

    private val mCameraDeviceCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(@NonNull camera: CameraDevice) {
            mCamera = camera
            mCallback.onCameraOpened()
            startCaptureSession()
        }

        override fun onClosed(@NonNull camera: CameraDevice) {
            mCallback.onCameraClosed()
        }

        override fun onDisconnected(@NonNull camera: CameraDevice) {
            mCamera = null
        }

        override fun onError(@NonNull camera: CameraDevice, error: Int) {
            Log.e(TAG, "onError: " + camera.id + " (" + error + ")")
            mCamera = null
        }

    }

    private val mSessionCallback = object : CameraCaptureSession.StateCallback() {

        override fun onConfigured(@NonNull session: CameraCaptureSession) {
            if (mCamera == null) {
                return
            }
            mCaptureSession = session
            updateAutoFocus()
            updateFlash()
            try {
                mCaptureSession!!.setRepeatingRequest(
                    mPreviewRequestBuilder!!.build(),
                    mCaptureCallback, backgroundHandler
                )
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Failed to start camera preview because it couldn't access camera", e)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to start camera preview.", e)
            }

        }

        override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {
            Log.e(TAG, "Failed to configure capture session.")
        }

        override fun onClosed(@NonNull session: CameraCaptureSession) {
            if (mCaptureSession != null && mCaptureSession == session) {
                mCaptureSession = null
            }
        }

    }

    private var mCaptureCallback: PictureCaptureCallback = object : PictureCaptureCallback() {

        override fun onPrecaptureRequired() {
            mPreviewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            setState(Camera2.PictureCaptureCallback.STATE_PRECAPTURE)
            try {
                mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), this, backgroundHandler)
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE
                )
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Failed to run precapture sequence.", e)
            }

        }

        override fun onReady() {
            captureStillPicture()
        }

    }

    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->

        reader.acquireNextImage().use { image ->
            val planes = image.planes
            if (planes.isNotEmpty()) {
                System.err.println("camoorah : planes was empty: $firebaseCallback")
                firebaseCallback?.onImageAvailable(image)
                try{ image.close() } catch(t: Throwable){ System.err.println("camoorah : failed to close")}
            } else {
                System.err.println("planes was empty")
            }
        }
    }


    var cameraId: String? = null

    private var mCameraCharacteristics: CameraCharacteristics? = null

    var mCamera: CameraDevice? = null

    var mCaptureSession: CameraCaptureSession? = null

    var mPreviewRequestBuilder: CaptureRequest.Builder? = null

    private var mImageReader: ImageReader? = null

    private val mPreviewSizes = SizeMap()

    private val mPictureSizes = SizeMap()

    private var mFacing: Int = 0

    private var mAspectRatio = Constants.DEFAULT_ASPECT_RATIO

    private var mAutoFocus: Boolean = false

    // Revert
    override var flash: Int = 0
        set(flash) {
            if (this.flash == flash) {
                return
            }
            val saved = this.flash
            field = flash
            if (mPreviewRequestBuilder != null) {
                updateFlash()
                if (mCaptureSession != null) {
                    try {
                        mCaptureSession!!.setRepeatingRequest(
                            mPreviewRequestBuilder!!.build(),
                            mCaptureCallback, backgroundHandler
                        )
                    } catch (e: CameraAccessException) {
                        field = saved
                    }

                }
            }
        }

    private var mDisplayOrientation: Int = 0

    override val isCameraOpened: Boolean
        get() = mCamera != null

    override var facing: Int
        get() = mFacing
        @RequiresPermission(Manifest.permission.CAMERA) set(facing) {
            if (mFacing == facing) {
                return
            }
            mFacing = facing
            if (isCameraOpened) {
                stop()
                start()
            }
        }

    override val supportedAspectRatios: Set<AspectRatio>
        get() = mPreviewSizes.ratios()

    // Revert
    override var autoFocus: Boolean
        get() = mAutoFocus
        set(autoFocus) {
            if (mAutoFocus == autoFocus) {
                return
            }
            mAutoFocus = autoFocus
            if (mPreviewRequestBuilder != null) {
                updateAutoFocus()
                if (mCaptureSession != null) {
                    try {
                        mCaptureSession!!.setRepeatingRequest(
                            mPreviewRequestBuilder!!.build(),
                            mCaptureCallback, backgroundHandler
                        )
                    } catch (e: CameraAccessException) {
                        mAutoFocus = !mAutoFocus
                    }

                }
            }
        }

    init {
        mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mPreview.setCallback(object : PreviewImpl.Callback {
            override fun onSurfaceChanged() {
                startCaptureSession()
            }
        })
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun start(): Boolean {
        if (!chooseCameraIdByFacing()) {
            return false
        }
        startBackgroundThread()
        collectCameraInfo()
        prepareImageReader()
        startOpeningCamera()
        return true
    }

    override fun stop() {
        stopBackgroundThread()
        if (mCaptureSession != null) {
            mCaptureSession!!.close()
            mCaptureSession = null
        }
        if (mCamera != null) {
            mCamera!!.close()
            mCamera = null
        }
        if (mImageReader != null) {
            mImageReader!!.close()
            mImageReader = null
        }
    }

    override val aspectRatio: AspectRatio get() = mAspectRatio

    override fun setAspectRatio(ratio: AspectRatio?): Boolean {
        if (ratio == null || ratio == mAspectRatio ||
            !mPreviewSizes.ratios().contains(ratio)
        ) {
            // TODO: Better error handling
            return false
        }
        mAspectRatio = ratio
        prepareImageReader()
        if (mCaptureSession != null) {
            mCaptureSession!!.close()
            mCaptureSession = null
            startCaptureSession()
        }
        return true
    }

    override fun takePicture() {
        if (mAutoFocus) {
            lockFocus()
        } else {
            captureStillPicture()
        }
    }

    override fun setDisplayOrientation(displayOrientation: Int) {
        mDisplayOrientation = displayOrientation
        mPreview.setDisplayOrientation(mDisplayOrientation)
    }

    /**
     *
     * Chooses a camera ID by the specified camera facing ([.mFacing]).
     *
     * This rewrites [.mCameraId], [.mCameraCharacteristics], and optionally
     * [.mFacing].
     */
    private fun chooseCameraIdByFacing(): Boolean {
        try {
            val internalFacing = INTERNAL_FACINGS.get(mFacing)
            val ids = mCameraManager.cameraIdList
            if (ids.size == 0) { // No camera
                throw RuntimeException("No camera available.")
            }
            for (id in ids) {
                val characteristics = mCameraManager.getCameraCharacteristics(id)
                val level = characteristics.get(
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
                )
                if (level == null || level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    continue
                }
                val internal = characteristics.get(CameraCharacteristics.LENS_FACING)
                    ?: throw NullPointerException("Unexpected state: LENS_FACING null")
                if (internal == internalFacing) {
                    cameraId = id
                    mCameraCharacteristics = characteristics
                    return true
                }
            }
            // Not found
            cameraId = ids[0]
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId!!)
            val level = mCameraCharacteristics!!.get(
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
            )
            if (level == null || level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                return false
            }
            val internal = mCameraCharacteristics!!.get(CameraCharacteristics.LENS_FACING)
                ?: throw NullPointerException("Unexpected state: LENS_FACING null")
            var i = 0
            val count = INTERNAL_FACINGS.size()
            while (i < count) {
                if (INTERNAL_FACINGS.valueAt(i) == internal) {
                    mFacing = INTERNAL_FACINGS.keyAt(i)
                    return true
                }
                i++
            }
            // The operation can reach here when the only camera device is an external one.
            // We treat it as facing back.
            mFacing = Constants.FACING_BACK
            return true
        } catch (e: CameraAccessException) {
            throw RuntimeException("Failed to get a list of camera devices", e)
        }

    }

    /**
     *
     * Collects some information from [.mCameraCharacteristics].
     *
     * This rewrites [.mPreviewSizes], [.mPictureSizes], and optionally,
     * [.mAspectRatio].
     */
    private fun collectCameraInfo() {
        val map = mCameraCharacteristics!!.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        ) ?: throw IllegalStateException("Failed to get configuration map: " + cameraId!!)
        mPreviewSizes.clear()
        for (size in map.getOutputSizes(mPreview.outputClass)) {
            val width = size.width
            val height = size.height
            if (width <= MAX_PREVIEW_WIDTH && height <= MAX_PREVIEW_HEIGHT) {
                mPreviewSizes.add(Size(width, height))
            }
        }
        mPictureSizes.clear()
        collectPictureSizes(mPictureSizes, map)
        for (ratio in mPreviewSizes.ratios()) {
            if (!mPictureSizes.ratios().contains(ratio)) {
                mPreviewSizes.remove(ratio)
            }
        }

        if (!mPreviewSizes.ratios().contains(mAspectRatio)) {
            mAspectRatio = mPreviewSizes.ratios().iterator().next()
        }
    }

    protected open fun collectPictureSizes(sizes: SizeMap, map: StreamConfigurationMap) {
        val outputSizes = map.getOutputSizes(ImageFormat.JPEG)
        for (size in outputSizes) {
            mPictureSizes.add(Size(size.width, size.height))
        }
    }

    private fun prepareImageReader() {
        if (mImageReader != null) {
            mImageReader!!.close()
        }
//        val largest = mPictureSizes.sizes(mAspectRatio).last()
        val previewSize = chooseOptimalSize()
        mImageReader = ImageReader.newInstance(
            previewSize.width / 4, previewSize.height / 4, ImageFormat.YUV_420_888, 2
        )
        mImageReader!!.setOnImageAvailableListener(mOnImageAvailableListener, backgroundHandler)
    }

    /**
     *
     * Starts opening a camera device.
     *
     * The result will be processed in [.mCameraDeviceCallback].
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    private fun startOpeningCamera() {
        try {
            mCameraManager.openCamera(cameraId!!, mCameraDeviceCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            throw RuntimeException("Failed to open camera: " + cameraId!!, e)
        }

    }

    /**
     *
     * Starts a capture session for camera preview.
     *
     * This rewrites [.mPreviewRequestBuilder].
     *
     * The result will be continuously processed in [.mSessionCallback].
     */
    fun startCaptureSession() {
        if (!isCameraOpened || !mPreview.isReady || mImageReader == null) {
            return
        }
        val previewSize = chooseOptimalSize()
        mPreview.setBufferSize(previewSize.width, previewSize.height)
        val surface = mPreview.surface
        try {
            mPreviewRequestBuilder = mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder!!.addTarget(surface)
            mPreviewRequestBuilder!!.addTarget(mImageReader!!.surface)
            mCamera!!.createCaptureSession(
                listOf(surface, mImageReader!!.surface),
                mSessionCallback, backgroundHandler
            )
        } catch (e: CameraAccessException) {
            throw RuntimeException("Failed to start camera session")
        }

    }

    /**
     * Chooses the optimal preview size based on [.mPreviewSizes] and the surface size.
     *
     * @return The picked size for camera preview.
     */
    private fun chooseOptimalSize(): Size {
        val surfaceLonger: Int
        val surfaceShorter: Int
        val surfaceWidth = mPreview.width
        val surfaceHeight = mPreview.height
        if (surfaceWidth < surfaceHeight) {
            surfaceLonger = surfaceHeight
            surfaceShorter = surfaceWidth
        } else {
            surfaceLonger = surfaceWidth
            surfaceShorter = surfaceHeight
        }
        val candidates = mPreviewSizes.sizes(mAspectRatio)

        // Pick the smallest of those big enough
        for (size in candidates) {
            if (size.width >= surfaceLonger && size.height >= surfaceShorter) {
                return size
            }
        }
        // If no size is big enough, pick the largest one.
        return candidates.last()
    }

    /**
     * Updates the internal state of auto-focus to [.mAutoFocus].
     */
    fun updateAutoFocus() {
        if (mAutoFocus) {
            val modes = mCameraCharacteristics!!.get(
                CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES
            )
            // Auto focus is not supported
            if (modes == null || modes.size == 0 ||
                modes.size == 1 && modes[0] == CameraCharacteristics.CONTROL_AF_MODE_OFF
            ) {
                mAutoFocus = false
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_OFF
                )
            } else {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }
        } else {
            mPreviewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_OFF
            )
        }
    }

    /**
     * Updates the internal state of flash to [.mFlash].
     */
    fun updateFlash() {
        when (flash) {
            Constants.FLASH_OFF -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
            Constants.FLASH_ON -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
            Constants.FLASH_TORCH -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_TORCH
                )
            }
            Constants.FLASH_AUTO -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
            Constants.FLASH_RED_EYE -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
        }
    }

    /**
     * Locks the focus as the first step for a still image capture.
     */
    private fun lockFocus() {
        mPreviewRequestBuilder!!.set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CaptureRequest.CONTROL_AF_TRIGGER_START
        )
        try {
            mCaptureCallback.setState(PictureCaptureCallback.STATE_LOCKING)
            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to lock focus.", e)
        }

    }

    /**
     * Captures a still picture.
     */
    fun captureStillPicture() {
        Log.e("camoorah", "capturing  still picture")
        try {
            val captureRequestBuilder = mCamera!!.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE
            )
            captureRequestBuilder.addTarget(mImageReader!!.surface)
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                mPreviewRequestBuilder!!.get(CaptureRequest.CONTROL_AF_MODE)
            )
            when (flash) {
                Constants.FLASH_OFF -> {
                    captureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON
                    )
                    captureRequestBuilder.set(
                        CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF
                    )
                }
                Constants.FLASH_ON -> captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH
                )
                Constants.FLASH_TORCH -> {
                    captureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON
                    )
                    captureRequestBuilder.set(
                        CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_TORCH
                    )
                }
                Constants.FLASH_AUTO -> captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                Constants.FLASH_RED_EYE -> captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
            }
            // Calculate JPEG orientation.
            val sensorOrientation = mCameraCharacteristics!!.get(
                CameraCharacteristics.SENSOR_ORIENTATION
            )!!
            captureRequestBuilder.set(
                CaptureRequest.JPEG_ORIENTATION,
                (sensorOrientation +
                        mDisplayOrientation * (if (mFacing == Constants.FACING_FRONT) 1 else -1) +
                        360) % 360
            )
            // Stop preview and capture a still picture.
            mCaptureSession!!.stopRepeating()
            mCaptureSession!!.capture(captureRequestBuilder.build(),
                object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(
                        @NonNull session: CameraCaptureSession,
                        @NonNull request: CaptureRequest,
                        @NonNull result: TotalCaptureResult
                    ) {
                        unlockFocus()
                    }
                }, backgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Cannot capture a still picture.", e)
        }

    }

    /**
     * Unlocks the auto-focus and restart camera preview. This is supposed to be called after
     * capturing a still picture.
     */
    fun unlockFocus() {
        mPreviewRequestBuilder!!.set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CaptureRequest.CONTROL_AF_TRIGGER_CANCEL
        )
        try {
            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback, backgroundHandler)
            updateAutoFocus()
            updateFlash()
            mPreviewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_IDLE
            )
            mCaptureSession!!.setRepeatingRequest(mPreviewRequestBuilder!!.build(), mCaptureCallback, backgroundHandler)
            mCaptureCallback.setState(PictureCaptureCallback.STATE_PREVIEW)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to restart camera preview.", e)
        }
    }

    var backgroundHandlerThread: HandlerThread? = null
    var backgroundHandler: Handler? = null

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        backgroundHandlerThread = HandlerThread("CameraBackgroundProcessor")
        backgroundHandlerThread?.start()
        backgroundHandler = Handler(backgroundHandlerThread?.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        backgroundHandlerThread?.quitSafely()
        try {
            backgroundHandlerThread?.join()
            backgroundHandlerThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


    /**
     * A [CameraCaptureSession.CaptureCallback] for capturing a still picture.
     */
    private abstract class PictureCaptureCallback internal constructor() : CameraCaptureSession.CaptureCallback() {

        private var mState: Int = 0

        internal fun setState(state: Int) {
            mState = state
        }

        override fun onCaptureProgressed(
            @NonNull session: CameraCaptureSession,
            @NonNull request: CaptureRequest, @NonNull partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            @NonNull session: CameraCaptureSession,
            @NonNull request: CaptureRequest, @NonNull result: TotalCaptureResult
        ) {
            process(result)
        }

        private fun process(@NonNull result: CaptureResult) {
            when (mState) {
                STATE_LOCKING -> {
                    val af = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (af != null) {
                        if (af == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || af == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                            val ae = result.get(CaptureResult.CONTROL_AE_STATE)
                            if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                                setState(STATE_CAPTURING)
                                onReady()
                            } else {
                                setState(STATE_LOCKED)
                                onPrecaptureRequired()
                            }
                        }
                    }
                }
                STATE_PRECAPTURE -> {
                    val ae = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        ae == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED ||
                        ae == CaptureResult.CONTROL_AE_STATE_CONVERGED
                    ) {
                        setState(STATE_WAITING)
                    }
                }
                STATE_WAITING -> {
                    val ae = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (ae == null || ae != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        setState(STATE_CAPTURING)
                        onReady()
                    }
                }
            }
        }

        /**
         * Called when it is ready to take a still picture.
         */
        abstract fun onReady()

        /**
         * Called when it is necessary to run the precapture sequence.
         */
        abstract fun onPrecaptureRequired()

        companion object {

            internal val STATE_PREVIEW = 0
            internal val STATE_LOCKING = 1
            internal val STATE_LOCKED = 2
            internal val STATE_PRECAPTURE = 3
            internal val STATE_WAITING = 4
            internal val STATE_CAPTURING = 5
        }

    }

    companion object {

        private val TAG = "Camera2"

        private val INTERNAL_FACINGS = SparseIntArray()

        init {
            INTERNAL_FACINGS.put(Constants.FACING_BACK, CameraCharacteristics.LENS_FACING_BACK)
            INTERNAL_FACINGS.put(Constants.FACING_FRONT, CameraCharacteristics.LENS_FACING_FRONT)
        }

        /**
         * Max preview width that is guaranteed by Camera2 API
         */
        private val MAX_PREVIEW_WIDTH = 1920

        /**
         * Max preview height that is guaranteed by Camera2 API
         */
        private val MAX_PREVIEW_HEIGHT = 1080
    }

}
