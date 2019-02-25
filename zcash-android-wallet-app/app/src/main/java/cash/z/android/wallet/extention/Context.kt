package cash.z.android.wallet.extention

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

internal val NO_ACTION = {}

/**
 * Calls context.alert with the given string.
 */
internal fun Context.alert(
    @StringRes messageResId: Int,
    @StringRes positiveButtonResId: Int = android.R.string.ok,
    @StringRes negativeButtonResId: Int = android.R.string.cancel,
    positiveAction: () -> Unit = NO_ACTION,
    negativeAction: () -> Unit = NO_ACTION
) {
    alert(
        message = getString(messageResId),
        positiveButtonResId = positiveButtonResId,
        negativeButtonResId = negativeButtonResId,
        positiveAction = positiveAction,
        negativeAction = negativeAction
    )
}

/**
 * Show an alert with the given message, if the block exists, it will execute after the user clicks the positive button,
 * while clicking the negative button will abort the block. If no block exists, there will only be a positive button.
 */
internal fun Context.alert(
    message: String,
    @StringRes positiveButtonResId: Int = android.R.string.ok,
    @StringRes negativeButtonResId: Int = android.R.string.cancel,
    positiveAction: (() -> Unit) = NO_ACTION,
    negativeAction: (() -> Unit) = NO_ACTION
) {
    val builder = AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton(positiveButtonResId) { dialog, _ ->
            dialog.dismiss()
            positiveAction()
        }
    if (positiveAction !== NO_ACTION || negativeAction !== NO_ACTION) {
        builder.setNegativeButton(negativeButtonResId) { dialog, _ ->
            dialog.dismiss()
            negativeAction()
        }
    }
    builder.show()
}