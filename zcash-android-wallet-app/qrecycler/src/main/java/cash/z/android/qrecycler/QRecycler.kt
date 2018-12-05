package cash.z.android.qrecycler

import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import androidx.core.view.doOnLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType.ERROR_CORRECTION
import com.google.zxing.EncodeHintType.MARGIN
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q


class QRecycler {
    fun load(content: String): Builder {
        return Builder(content)
    }

    // TODO: make this call async such that action can be taken once it is complete
    fun encode(builder: Builder) {
        builder.target.doOnLayout { measuredView ->
            val w = measuredView.width
            val h = measuredView.height
            val hints = mapOf(ERROR_CORRECTION to Q, MARGIN to 2)
            val bitMatrix = QRCodeWriter().encode(builder.content, BarcodeFormat.QR_CODE, w, h, hints)
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                val offset = y * w
                for (x in 0 until w) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.TRANSPARENT else Color.WHITE
                }
            }
            // TODO: RECYCLE THIS BITMAP MEMORY!!! Do it in a way that is lifecycle-aware and disposes of the memory when the fragment is off-screen
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            (measuredView as ImageView).setImageBitmap(bitmap)
        }
    }

    inner class Builder(val content: String) {
        lateinit var target: ImageView
        fun into(imageView: ImageView) {
            target = imageView
            encode(this)
        }
    }
}
