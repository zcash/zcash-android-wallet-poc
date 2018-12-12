package cash.z.android.wallet.ui.util

import android.text.TextPaint
import android.text.style.RelativeSizeSpan

/**
 * A span used for numbering the parts of an address. It combines a [android.text.style.RelativeSizeSpan],
 * [android.text.style.SuperscriptSpan], and a [android.text.style.ForegroundColorSpan] into one class for efficiency.
 */
class TopAlignedSpan(
    val proportion: Float = 0.625f
) : RelativeSizeSpan(proportion) {

    override fun updateMeasureState(textPaint: TextPaint) {
        updateDrawState(textPaint)
    }

    override fun updateDrawState(textPaint: TextPaint) {
        val initialSize = textPaint.textSize
        val scaledSize = textPaint.textSize * proportion
        val sizeDelta = scaledSize - initialSize + (textPaint.ascent()/2)

        textPaint.textSize = scaledSize
        // shift baseline up by change in textSize and adjust for density since size is in pixels
        textPaint.baselineShift += (sizeDelta/textPaint.density).toInt()
    }
}