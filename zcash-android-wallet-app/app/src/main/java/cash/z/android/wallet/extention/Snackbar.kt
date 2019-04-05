package cash.z.android.wallet.extention

import android.view.View
import cash.z.android.wallet.R
import com.google.android.material.snackbar.Snackbar

/**
 * Show a snackbar with an "OK" button
 */
internal inline fun Snackbar?.showOk(view: View, message: String): Snackbar {
    return if (this == null) {
        Snackbar.make(view, "$message", Snackbar.LENGTH_INDEFINITE)
            .setAction(view.context.getString(R.string.ok_allcaps)){/*auto-close*/}
    } else {
        setText(message)
    }.also {
        if (!it.isShownOrQueued) it.show()
    }
}
