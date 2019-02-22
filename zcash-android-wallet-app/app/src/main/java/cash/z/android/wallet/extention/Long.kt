package cash.z.android.wallet.extention

import android.text.format.DateUtils.SECOND_IN_MILLIS
import android.text.format.DateUtils.getRelativeTimeSpanString

internal inline fun Long.toRelativeTimeString(): CharSequence {
    return getRelativeTimeSpanString(
        this,
        System.currentTimeMillis(),
        SECOND_IN_MILLIS
    )
}
