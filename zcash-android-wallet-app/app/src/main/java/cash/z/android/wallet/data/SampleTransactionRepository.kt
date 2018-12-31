package cash.z.android.wallet.data

import android.text.format.DateUtils
import cash.z.android.wallet.vo.WalletTransaction
import cash.z.android.wallet.vo.WalletTransactionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.math.BigDecimal
import kotlin.random.Random

class SampleTransactionRepository(val scope: CoroutineScope) : TransactionRepository {
    /**
     * Just send a sample stream of balances, every so often
     */
    override fun balance() = scope.produce {
        var currentBalance = 0.0
        while (isActive) {
            send(BigDecimal(currentBalance))
            delay(500)
            currentBalance += 0.1
        }
    }

    /**
     * Just send a sample stream of transactions, every so often
     */
    override fun transactions(): ReceiveChannel<WalletTransaction> = scope.produce {
        while (isActive) {
            send(createSampleTransaction())
            delay(1500L)
        }
    }

    private fun createSampleTransaction(): WalletTransaction {
        val now = System.currentTimeMillis()
        val before = now - (4 * DateUtils.WEEK_IN_MILLIS)
        val amount = BigDecimal(Random.nextDouble(0.1, 15.0) * arrayOf(-1, 1).random())
        val status = if (amount > BigDecimal.ZERO) WalletTransactionStatus.SENT else WalletTransactionStatus.RECEIVED
        return WalletTransaction(
            status,
            Random.nextLong(before, now),
            amount
        )
    }

    private fun createSampleTransactions(size: Int): MutableList<WalletTransaction> {
        val transactions = mutableListOf<WalletTransaction>()
        repeat(size) {
            transactions.add(createSampleTransaction())
        }
        return transactions
    }
}