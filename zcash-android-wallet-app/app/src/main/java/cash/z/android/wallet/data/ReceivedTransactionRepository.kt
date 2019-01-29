package cash.z.android.wallet.data

import androidx.room.Room
import androidx.room.RoomDatabase
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.vo.WalletTransaction
import cash.z.android.wallet.vo.WalletTransactionStatus
import cash.z.wallet.sdk.dao.BlockDao
import cash.z.wallet.sdk.dao.NoteDao
import cash.z.wallet.sdk.dao.TransactionDao
import cash.z.wallet.sdk.db.DerivedDataDb
import cash.z.wallet.sdk.vo.NoteQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.math.BigDecimal

class ReceivedTransactionRepository(val scope: CoroutineScope) : TransactionRepository {

    private var db = injectDb()
    private lateinit var transactions: TransactionDao
    private lateinit var blocks: BlockDao
    private lateinit var notes: NoteDao

    private var existingTransactions = linkedSetOf<WalletTransaction>()
    private var existingBalance = BigDecimal.ZERO
    private var balanceChannel = BroadcastChannel<BigDecimal>(100)

    private fun injectDb() = Room
        .databaseBuilder(ZcashWalletApplication.instance, DerivedDataDb::class.java, "tmp")
        .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
        .fallbackToDestructiveMigration()
        .build()
        .apply {
            transactions = transactionDao()
            blocks = blockDao()
            notes = noteDao()
        }


    /**
     * Just send a sample stream of balances, every so often
     */
    override fun balance() = balanceChannel.openSubscription()

    /**
     * Just send a sample stream of transactions, every so often
     */
    override fun transactions(): ReceiveChannel<WalletTransaction> = scope.produce {
        while (isActive) {
            delay(1500L)
            val newTransactions = checkForNewTransactions()
            newTransactions?.forEach {
                existingTransactions.add(it)
                send(it)
                updateBalance(it)
            }
        }
    }

    private suspend fun updateBalance(tx: WalletTransaction) {
        val multiplier = when (tx.status) {
            WalletTransactionStatus.SENT -> -1.0
            WalletTransactionStatus.RECEIVED -> 1.0
        }
        existingBalance += tx.amount.multiply(BigDecimal(multiplier))
        balanceChannel.send(existingBalance)
    }

    private fun checkForNewTransactions(): Set<WalletTransaction>? {
        val count = notes.count()
        if(count == existingTransactions.size) return null

        val notes = notes.getAll().map { toWalletTransaction(it) }
        return notes.subtract(existingTransactions)
    }

    private fun toWalletTransaction(note: NoteQuery): WalletTransaction {
        return WalletTransaction(
            status = WalletTransactionStatus.RECEIVED,
            amount = BigDecimal(note.value / 1e8),
            timestamp = note.time
        )
    }
}