/*
 * Copyright (C) 2019 Electric Coin Company
 *
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * This file has been modified by Electric Coin Company to translate it into Kotlin and add support for Firebase vision.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cash.z.android.cameraview.base

import android.view.View

internal abstract class CameraViewImpl(protected val mCallback: Callback, protected val mPreview: PreviewImpl) {

    val view: View
        get() = mPreview.view

    internal abstract val isCameraOpened: Boolean

    internal abstract var facing: Int

    internal abstract val supportedAspectRatios: Set<AspectRatio>

    internal abstract val aspectRatio: AspectRatio

    internal abstract var autoFocus: Boolean

    internal abstract var flash: Int

    /**
     * @return `true` if the implementation was able to start the camera session.
     */
    internal abstract fun start(): Boolean

    internal abstract fun stop()

    /**
     * @return `true` if the aspect ratio was changed.
     */
    internal abstract fun setAspectRatio(ratio: AspectRatio?): Boolean

    internal abstract fun takePicture()

    internal abstract fun setDisplayOrientation(displayOrientation: Int)

    internal interface Callback {

        fun onCameraOpened()

        fun onCameraClosed()

        fun onPictureTaken(data: ByteArray)

    }

}
