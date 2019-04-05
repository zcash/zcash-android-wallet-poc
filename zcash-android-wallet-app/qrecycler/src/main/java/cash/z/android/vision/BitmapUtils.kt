package cash.z.android.wallet.ui.util.vision

// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


import android.graphics.*
import android.hardware.Camera.CameraInfo
import android.util.Log
import androidx.annotation.Nullable
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/** Utils functions for bitmap conversions.  */
object BitmapUtils {

    // Convert NV21 format byte buffer to bitmap.
    @Nullable
    fun getBitmap(data: ByteBuffer, metadata: FrameMetadata): Bitmap? {
        data.rewind()
        val imageInBuffer = ByteArray(data.limit())
        data.get(imageInBuffer, 0, imageInBuffer.size)
        try {
            val image = YuvImage(
                imageInBuffer, ImageFormat.NV21, metadata.width, metadata.height, null
            )
            if (image != null) {
                val stream = ByteArrayOutputStream()
                image.compressToJpeg(Rect(0, 0, metadata.width, metadata.height), 80, stream)

                val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())

                stream.close()
                return rotateBitmap(
                    bmp,
                    metadata.rotation,
                    metadata.cameraFacing
                )
            }
        } catch (e: Exception) {
            Log.e("VisionProcessorBase", "Error: " + e.message)
        }

        return null
    }

    // Rotates a bitmap if it is converted from a bytebuffer.
    private fun rotateBitmap(bitmap: Bitmap, rotation: Int, facing: Int): Bitmap {
        val matrix = Matrix()
        var rotationDegree = 0
        when (rotation) {
            FirebaseVisionImageMetadata.ROTATION_90 -> rotationDegree = 90
            FirebaseVisionImageMetadata.ROTATION_180 -> rotationDegree = 180
            FirebaseVisionImageMetadata.ROTATION_270 -> rotationDegree = 270
            else -> {
            }
        }

        // Rotate the image back to straight.}
        matrix.postRotate(rotationDegree.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
