package cash.z.android.wallet.vo

import cash.z.android.wallet.R
import androidx.annotation.ColorRes
import java.math.BigDecimal

data class WalletTransaction(val status: WalletTransactionStatus, val timestamp: Long, val amount: BigDecimal)

enum class WalletTransactionStatus(@ColorRes val color: Int) {
    SENT(R.color.colorPrimary),
    RECEIVED(R.color.colorAccent);
}
