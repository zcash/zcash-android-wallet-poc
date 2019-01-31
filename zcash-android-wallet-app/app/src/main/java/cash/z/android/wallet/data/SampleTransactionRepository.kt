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
import kotlin.math.roundToLong
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
        var oldestTimestamp = System.currentTimeMillis() - (4 * DateUtils.WEEK_IN_MILLIS)
        while (isActive) {
            delay(1500L)
            send(createSampleTransaction(oldestTimestamp).also { oldestTimestamp = it.timestamp })
        }
    }

    private fun createSampleTransaction(): WalletTransaction {
        return createSampleTransaction(System.currentTimeMillis() - (4 * DateUtils.WEEK_IN_MILLIS))
    }

    private fun createSampleTransaction(after: Long): WalletTransaction {
        val now = System.currentTimeMillis()
        val delta = now - after
        val window = after + (0.05 * delta).roundToLong()
        val amount = BigDecimal(Random.nextDouble(0.1, 15.0) * arrayOf(-1, 1).random())
        val status = if (amount > BigDecimal.ZERO) WalletTransactionStatus.SENT else WalletTransactionStatus.RECEIVED
        val timestamp = Random.nextLong(after, window)
        return WalletTransaction(
            timestamp.toInt(),
            status,
            timestamp,
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