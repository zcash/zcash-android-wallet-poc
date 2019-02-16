package cash.z.android.wallet.extention

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

internal inline fun Context.alert(@StringRes messageResId: Int, crossinline block: () -> Unit = {}) {
    AlertDialog.Builder(this)
        .setMessage(messageResId)
        .setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            block()
        }
        .setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}