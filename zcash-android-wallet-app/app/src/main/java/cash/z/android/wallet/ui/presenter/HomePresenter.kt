package cash.z.android.wallet.ui.presenter

import android.util.Log
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.ui.fragment.HomeFragment
import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.wallet.sdk.dao.WalletTransaction
import cash.z.wallet.sdk.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.ReceiveChannel
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class HomePresenter @Inject constructor(
    private val view: HomeFragment,
    private val synchronizer: Synchronizer
) : Presenter, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    interface HomeView : PresenterView {
        fun setTransactions(transactions: List<WalletTransaction>)
        fun updateBalance(old: Long, new: Long)
        fun setActiveTransactions(activeTransactionMap: Map<ActiveTransaction, TransactionState>)
        fun onCancelledTooLate()
    }

    override suspend fun start() {
        twig("homePresenter starting!")
        launchBalanceBinder(synchronizer.balance())
        launchTransactionBinder(synchronizer.allTransactions())
        launchActiveTransactionMonitor(synchronizer.activeTransactions())
    }

    override fun stop() {
        twig("homePresenter stopping!")
        job.cancel()
    }

    private fun CoroutineScope.launchBalanceBinder(channel: ReceiveChannel<Long>) = launch {
        var old: Long? = null
        twig("balance binder starting!")
        for (new in channel) {
            twig("polled a balance item")
            bind(old, new).also { old = new }
        }
        twig("balance binder exiting!")
    }

    private fun CoroutineScope.launchTransactionBinder(channel: ReceiveChannel<List<WalletTransaction>>) = launch {
        twig("transaction binder starting!")
        for (walletTransactionList in channel) {
            twig("received ${walletTransactionList.size} transactions for presenting")
            bind(walletTransactionList)
        }
        twig("transaction binder exiting!")
    }

    private fun CoroutineScope.launchActiveTransactionMonitor(channel: ReceiveChannel<Map<ActiveTransaction, TransactionState>>) = launch {
        twig("active transaction monitor starting!")
        for (i in channel) {
            bind(i)
        }
        twig("active transaction monitor exiting!")
    }


    //
    // View Callbacks on Main Thread
    //

    private fun bind(old: Long?, new: Long) = onMain {
        twig("binding balance of $new")
        view.updateBalance(old ?: 0L, new)
    }


    private fun bind(transactions: List<WalletTransaction>) = onMain {
        twig("binding ${transactions.size} walletTransactions")
        view.setTransactions(transactions.sortedByDescending {
            if (!it.isMined && it.isSend) Long.MAX_VALUE else it.timeInSeconds
        })
    }

    private fun bind(activeTransactionMap: Map<ActiveTransaction, TransactionState>) = onMain {
        twig("binding a.t. map of size ${activeTransactionMap.size}")
        if (activeTransactionMap.isNotEmpty()) view.setActiveTransactions(activeTransactionMap)
    }

    fun onCancelActiveTransaction(transaction: ActiveSendTransaction) {
        twig("requesting to cancel send for transaction ${transaction.internalId}")
        val isTooLate = !synchronizer.cancelSend(transaction)
        if (isTooLate) {
            view.onCancelledTooLate()
        }
    }

    private fun onMain(block: () -> Unit) = launch {
        withContext(Main) {
            twig("running task on main thread - start ${coroutineContext[Job]} | ${coroutineContext[CoroutineName]}")
            block()
            twig("running task on main thread - complete")
        }
    }

}

