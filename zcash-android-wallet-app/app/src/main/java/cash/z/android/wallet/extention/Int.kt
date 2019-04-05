package cash.z.android.wallet.extention

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import cash.z.android.wallet.ZcashWalletApplication

/**
 * Grab a color out of the application resources, using the default theme
 */
@ColorInt
internal inline fun @receiver:ColorRes Int.toAppColor(): Int {
    return ResourcesCompat.getColor(ZcashWalletApplication.instance.resources, this, ZcashWalletApplication.instance.theme)
}

/**
 * Grab a string from the application resources
 */
internal inline fun @receiver:StringRes Int.toAppString(): String {
    return ZcashWalletApplication.instance.getString(this)}


/**
 * Grab an integer from the application resources
 */
internal inline fun @receiver:IntegerRes Int.toAppInt(): Int {
    return ZcashWalletApplication.instance.resources.getInteger(this)}

