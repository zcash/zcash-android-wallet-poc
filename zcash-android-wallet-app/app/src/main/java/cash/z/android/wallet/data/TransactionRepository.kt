package cash.z.android.wallet.data

import kotlinx.coroutines.channels.ReceiveChannel
import java.math.BigDecimal

interface TransactionRepository {
    fun balance(): ReceiveChannel<BigDecimal>
}