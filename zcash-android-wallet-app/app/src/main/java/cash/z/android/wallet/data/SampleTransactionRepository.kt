package cash.z.android.wallet.data

import android.text.format.DateUtils
import cash.z.wallet.sdk.dao.WalletTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.math.BigDecimal
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextLong

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
            send(createSampleTransaction(oldestTimestamp).also { oldestTimestamp = it.timeInSeconds * 1000 })
        }
    }

    private fun createSampleTransaction(oldestTimestamp: Long): WalletTransaction {
        // up to 20% of the delta
        val upperBound = System.currentTimeMillis() + Math.round(0.2 * (System.currentTimeMillis() - oldestTimestamp))
        val txId = Random.nextInt(0..(Int.MAX_VALUE - 1))
        val value = Random.nextLong(1L..1_500_000_000L) - 750_000_000L
        val height = Random.nextInt(0..(Int.MAX_VALUE - 1))
        val isSend = value > 0L
        val time = Random.nextLong(oldestTimestamp..upperBound)
        val isMined = Random.nextBoolean()
        return WalletTransaction(
            txId = txId,
            value = value,
            height = height,
            isSend = isSend,
            timeInSeconds = time/1000,
            isMined = isMined
        )
    }

}