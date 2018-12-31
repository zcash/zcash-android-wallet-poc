package cash.z.android.wallet.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.math.BigDecimal

class SampleTransactionRepository(val scope: CoroutineScope) : TransactionRepository {

    /**
     * Just send a sample stream of balances, every so often
     */
    override fun balance() = scope.produce {
        var currentBalance = 0.0
        while(isActive) {
            send(BigDecimal(currentBalance))
            delay(500)
            currentBalance += 0.1
        }
    }
}