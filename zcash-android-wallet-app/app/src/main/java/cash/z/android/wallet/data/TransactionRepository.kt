package cash.z.android.wallet.data

import cash.z.wallet.sdk.dao.WalletTransaction
import kotlinx.coroutines.channels.ReceiveChannel
import java.math.BigDecimal

interface TransactionRepository {
    fun balance(): ReceiveChannel<BigDecimal>
    fun transactions(): ReceiveChannel<WalletTransaction>
}