package cash.z.android.wallet.extention

import android.widget.Toast
import cash.z.android.wallet.ZcashWalletApplication

// For now, Toast is still a java class so we cannot write static extensions (per https://youtrack.jetbrains.com/issue/KT-11968)
// This is a quick workaround.
internal class Toaster {
    companion object {
        fun short(message: String) =
            Toast.makeText(ZcashWalletApplication.instance, message, Toast.LENGTH_SHORT).show()
        fun long(message: String) =
            Toast.makeText(ZcashWalletApplication.instance, message, Toast.LENGTH_LONG).show()
    }
}