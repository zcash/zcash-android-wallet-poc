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

package cash.z.android.cameraview

import cash.z.android.qrecycler.R
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.os.ParcelableCompat
import androidx.core.os.ParcelableCompatCreatorCallbacks
import androidx.core.view.ViewCompat
import cash.z.android.cameraview.api21.Camera2
import cash.z.android.cameraview.base.AspectRatio
import cash.z.android.cameraview.base.CameraViewImpl
import cash.z.android.cameraview.base.Constants
import cash.z.android.cameraview.base.PreviewImpl
import com.google.android.cameraview.Camera2Api23
import com.google.android.cameraview.TextureViewPreview
import java.lang.IllegalStateException
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

open class CameraView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr) {

    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)

    internal lateinit var  mImpl: CameraViewImpl

    private var mCallbacks: CallbackBridge?

    private var mAdjustViewBounds: Boolean = false

    private var mDisplayOrientationDetector: DisplayOrientationDetector?

    /**
     * @return `true` if the camera is opened.
     */
    val isCameraOpened: Boolean
        get() = mImpl.isCameraOpened

    /**
     * @return True when this CameraView is adjusting its bounds to preserve the aspect ratio of
     * camera.
     * @see .setAdjustViewBounds
     */
    /**
     * @param adjustViewBounds `true` if you want the CameraView to adjust its bounds to
     * preserve the aspect ratio of camera.
     * @see .getAdjustViewBounds
     */
    var adjustViewBounds: Boolean
        get() = mAdjustViewBounds
        set(adjustViewBounds) {
            if (mAdjustViewBounds != adjustViewBounds) {
                mAdjustViewBounds = adjustViewBounds
                requestLayout()
            }
        }

    /**
     * Gets the direction that the current camera faces.
     *
     * @return The camera facing.
     */
    /**
     * Chooses camera by the direction it faces.
     *
     * @param facing The camera facing. Must be either [.FACING_BACK] or
     * [.FACING_FRONT].
     */
    var facing: Int
        @Facing
        get() = mImpl.facing
        set(@Facing facing) {
            mImpl.facing = facing
        }

    /**
     * Gets all the aspect ratios supported by the current camera.
     */
    val supportedAspectRatios: Set<AspectRatio>
        get() = mImpl.supportedAspectRatios

    /**
     * Gets the current aspect ratio of camera.
     *
     * @return The current [AspectRatio]. Can be `null` if no camera is opened yet.
     */
    /**
     * Sets the aspect ratio of camera.
     *
     * @param ratio The [AspectRatio] to be set.
     */
    var aspectRatio: AspectRatio?
        @Nullable
        get() = mImpl.aspectRatio
        set(@NonNull ratio) {
            if (mImpl.setAspectRatio(ratio)) {
                requestLayout()
            }
        }

    /**
     * Returns whether the continuous auto-focus mode is enabled.
     *
     * @return `true` if the continuous auto-focus mode is enabled. `false` if it is
     * disabled, or if it is not supported by the current camera.
     */
    /**
     * Enables or disables the continuous auto-focus mode. When the current camera doesn't support
     * auto-focus, calling this method will be ignored.
     *
     * @param autoFocus `true` to enable continuous auto-focus mode. `false` to
     * disable it.
     */
    var autoFocus: Boolean
        get() = mImpl.autoFocus
        set(autoFocus) {
            mImpl.autoFocus = autoFocus
        }

    /**
     * Gets the current flash mode.
     *
     * @return The current flash mode.
     */
    /**
     * Sets the flash mode.
     *
     * @param flash The desired flash mode.
     */
    var flash: Int
        @Flash
        get() = mImpl.flash
        set(@Flash flash) {
            mImpl.flash = flash
        }

    /** Direction the camera faces relative to device screen.  */
    @IntDef(FACING_BACK, FACING_FRONT)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class Facing

    /** The mode for for the camera device's flash control  */
    @IntDef(FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO, FLASH_RED_EYE)
    annotation class Flash

    init {
        if (isInEditMode) {
            mCallbacks = null
            mDisplayOrientationDetector = null
        } else {
            // Internal setup
            val preview = createPreviewImpl(context)
            mCallbacks = CallbackBridge()
            if (Build.VERSION.SDK_INT < 23) {
                mImpl = Camera2(mCallbacks!!, preview, context)
            } else {
                mImpl = Camera2Api23(mCallbacks!!, preview, context)
            }
            // Attributes
            val a = context.obtainStyledAttributes(
                attrs, R.styleable.CameraView, defStyleAttr,
                R.style.Widget_CameraView
            )
            mAdjustViewBounds = a.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false)
            facing = a.getInt(R.styleable.CameraView_facing, FACING_BACK)
            var aspectRatioString = a.getString(R.styleable.CameraView_aspectRatio)
            if (aspectRatioString != null) {
                aspectRatio = AspectRatio.parse(aspectRatioString)
            } else {
                aspectRatio = Constants.DEFAULT_ASPECT_RATIO
            }
            autoFocus = a.getBoolean(R.styleable.CameraView_autoFocus, true)
            flash = a.getInt(R.styleable.CameraView_flash, Constants.FLASH_AUTO)
            a.recycle()
            // Display orientation detector
            mDisplayOrientationDetector = object : DisplayOrientationDetector(context) {
                override fun onDisplayOrientationChanged(displayOrientation: Int) {
                    mImpl.setDisplayOrientation(displayOrientation)
                }
            }
        }
    }

    @NonNull
    private fun createPreviewImpl(context: Context): PreviewImpl {
        return TextureViewPreview(context, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            mDisplayOrientationDetector!!.enable(ViewCompat.getDisplay(this)!!)
        }
    }

    override fun onDetachedFromWindow() {
        if (!isInEditMode) {
            mDisplayOrientationDetector!!.disable()
        }
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isInEditMode) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        // Handle android:adjustViewBounds
        if (mAdjustViewBounds) {
            if (!isCameraOpened) {
                mCallbacks!!.reserveRequestLayoutOnOpen()
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                return
            }
            val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
            if (widthMode == View.MeasureSpec.EXACTLY && heightMode != View.MeasureSpec.EXACTLY) {
                val ratio = aspectRatio!!
                var height = (View.MeasureSpec.getSize(widthMeasureSpec) * ratio!!.toFloat()) as Int
                if (heightMode == View.MeasureSpec.AT_MOST) {
                    height = Math.min(height, View.MeasureSpec.getSize(heightMeasureSpec))
                }
                super.onMeasure(
                    widthMeasureSpec,
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                )
            } else if (widthMode != View.MeasureSpec.EXACTLY && heightMode == View.MeasureSpec.EXACTLY) {
                val ratio = aspectRatio!!
                var width = (View.MeasureSpec.getSize(heightMeasureSpec) * ratio!!.toFloat()) as Int
                if (widthMode == View.MeasureSpec.AT_MOST) {
                    width = Math.min(width, View.MeasureSpec.getSize(widthMeasureSpec))
                }
                super.onMeasure(
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    heightMeasureSpec
                )
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        // Measure the TextureView
        val width = measuredWidth
        val height = measuredHeight
        var ratio = aspectRatio
        if (mDisplayOrientationDetector!!.lastKnownDisplayOrientation % 180 == 0) {
            ratio = ratio!!.inverse()
        }
        assert(ratio != null)
        if (height < width * ratio!!.y / ratio!!.x) {
            mImpl.view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(
                    width * ratio!!.y / ratio!!.x,
                    View.MeasureSpec.EXACTLY
                )
            )
        } else {
            mImpl.view.measure(
                View.MeasureSpec.makeMeasureSpec(
                    height * ratio!!.x / ratio!!.y,
                    View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = SavedState(super.onSaveInstanceState())
        state.facing = facing
        state.ratio = aspectRatio
        state.autoFocus = autoFocus
        state.flash = flash
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val ss = state as SavedState?
        super.onRestoreInstanceState(ss!!.getSuperState())
        facing = ss.facing
        aspectRatio = ss.ratio
        autoFocus = ss.autoFocus
        flash = ss.flash
    }

    /**
     * Open a camera device and start showing camera preview. This is typically called from
     * [Activity.onResume].
     */
    fun start() {
        if (!mImpl.start()) {
            throw IllegalStateException("failed to start even though we're on API 21+")
//            //store the state ,and restore this state after fall back o Camera1
//            val state = onSaveInstanceState()
//            // Camera2 uses legacy hardware layer; fall back to Camera1
//            mImpl = Camera1(mCallbacks, createPreviewImpl(context))
//            onRestoreInstanceState(state)
//            mImpl.start()
        }
    }

    /**
     * Stop camera preview and close the device. This is typically called from
     * [Activity.onPause].
     */
    fun stop() {
        mImpl.stop()
    }

    /**
     * Add a new callback.
     *
     * @param callback The [Callback] to add.
     * @see .removeCallback
     */
    fun addCallback(@NonNull callback: Callback) {
        mCallbacks!!.add(callback)
    }

    /**
     * Remove a callback.
     *
     * @param callback The [Callback] to remove.
     * @see .addCallback
     */
    fun removeCallback(@NonNull callback: Callback) {
        mCallbacks!!.remove(callback)
    }

    /**
     * Take a picture. The result will be returned to
     * [Callback.onPictureTaken].
     */
    fun takePicture() {
        mImpl.takePicture()
    }

    private inner class CallbackBridge internal constructor() : CameraViewImpl.Callback {

        private val mCallbacks = ArrayList<Callback>()

        private var mRequestLayoutOnOpen: Boolean = false

        fun add(callback: Callback) {
            mCallbacks.add(callback)
        }

        fun remove(callback: Callback) {
            mCallbacks.remove(callback)
        }

        override fun onCameraOpened() {
            if (mRequestLayoutOnOpen) {
                mRequestLayoutOnOpen = false
                requestLayout()
            }
            for (callback in mCallbacks) {
                callback.onCameraOpened(this@CameraView)
            }
        }

        override fun onCameraClosed() {
            for (callback in mCallbacks) {
                callback.onCameraClosed(this@CameraView)
            }
        }

        override fun onPictureTaken(data: ByteArray) {
            for (callback in mCallbacks) {
                callback.onPictureTaken(this@CameraView, data)
            }
        }

        fun reserveRequestLayoutOnOpen() {
            mRequestLayoutOnOpen = true
        }
    }

    protected class SavedState : View.BaseSavedState {

        @Facing
        internal var facing: Int = 0

        internal var ratio: AspectRatio? = null

        internal var autoFocus: Boolean = false

        @Flash
        internal var flash: Int = 0

        constructor(source: Parcel) : this(source, AspectRatio::class.java.classLoader!!)

        constructor(source: Parcel, loader: ClassLoader) : super(source) {
            facing = source.readInt()
            ratio = source.readParcelable(loader)
            autoFocus = source.readByte().toInt() != 0
            flash = source.readInt()
        }

        constructor(parcelable: Parcelable) : super(parcelable)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(facing)
            out.writeParcelable(ratio, 0)
            out.writeByte((if (autoFocus) 1 else 0).toByte())
            out.writeInt(flash)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

    }

    /**
     * Callback for monitoring events about [CameraView].
     */
    abstract class Callback {

        /**
         * Called when camera is opened.
         *
         * @param cameraView The associated [CameraView].
         */
        fun onCameraOpened(cameraView: CameraView) {}

        /**
         * Called when camera is closed.
         *
         * @param cameraView The associated [CameraView].
         */
        fun onCameraClosed(cameraView: CameraView) {}

        /**
         * Called when a picture is taken.
         *
         * @param cameraView The associated [CameraView].
         * @param data       JPEG data.
         */
        fun onPictureTaken(cameraView: CameraView, data: ByteArray) {}
    }

    companion object {

        /** The camera device faces the opposite direction as the device's screen.  */
        const val FACING_BACK = Constants.FACING_BACK

        /** The camera device faces the same direction as the device's screen.  */
        const val FACING_FRONT = Constants.FACING_FRONT

        /** Flash will not be fired.  */
        const val FLASH_OFF = Constants.FLASH_OFF

        /** Flash will always be fired during snapshot.  */
        const val FLASH_ON = Constants.FLASH_ON

        /** Constant emission of light during preview, auto-focus and snapshot.  */
        const val FLASH_TORCH = Constants.FLASH_TORCH

        /** Flash will be fired automatically when required.  */
        const val FLASH_AUTO = Constants.FLASH_AUTO

        /** Flash will be fired in red-eye reduction mode.  */
        const val FLASH_RED_EYE = Constants.FLASH_RED_EYE
    }

}
