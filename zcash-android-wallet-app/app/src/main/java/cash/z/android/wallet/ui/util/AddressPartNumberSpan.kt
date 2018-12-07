package cash.z.android.wallet.ui.util

import android.os.Parcel
import android.text.ParcelableSpan
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import androidx.core.content.ContextCompat
import cash.z.android.wallet.R
import cash.z.android.wallet.ZcashWalletApplication

/**
 * A span used for numbering the parts of an address. It combines a [android.text.style.RelativeSizeSpan],
 * [android.text.style.SuperscriptSpan], and a [android.text.style.ForegroundColorSpan] into one class for efficiency.
 */
class AddressPartNumberSpan(
    val proportion: Float = 0.5f,
    val color: Int = ContextCompat.getColor(ZcashWalletApplication.instance, R.color.colorPrimary)
) : MetricAffectingSpan(), ParcelableSpan {
    constructor(parcel: Parcel) : this(parcel.readFloat(), parcel.readInt())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeFloat(proportion)
        dest.writeInt(color)
    }

    override fun getSpanTypeId(): Int = -1

    override fun describeContents() = 0

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.baselineShift += (textPaint.ascent() / 2).toInt()  // from SuperscriptSpan
        textPaint.textSize = textPaint.textSize * proportion  // from RelativeSizeSpan
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.baselineShift += (textPaint.ascent() / 2).toInt()  // from SuperscriptSpan (baseline must shift before resizing or else it will not properly align to the top of the text)
        textPaint.textSize = textPaint.textSize * proportion  // from RelativeSizeSpan
        textPaint.color = color  // from ForegroundColorSpan
    }
}