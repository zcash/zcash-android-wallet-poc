package cash.z.android.wallet.ui.util.vision


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import cash.z.android.wallet.ui.util.vision.GraphicOverlay.Graphic

/** Draw camera image to background.  */
class CameraImageGraphic(overlay: GraphicOverlay, private val bitmap: Bitmap) : Graphic(overlay) {

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, Rect(0, 0, canvas.width, canvas.height), null)
    }
}
