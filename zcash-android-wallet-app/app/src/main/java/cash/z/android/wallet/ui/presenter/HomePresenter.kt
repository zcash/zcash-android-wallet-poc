package cash.z.android.wallet.ui.presenter

import android.util.Log
import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.wallet.sdk.dao.WalletTransaction
import cash.z.wallet.sdk.data.ActiveSendTransaction
import cash.z.wallet.sdk.data.ActiveTransaction
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.data.TransactionState
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.coroutines.CoroutineContext

class HomePresenter(
    private val view: HomeView,
    private val synchronizer: Synchronizer
) : Presenter, CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    interface HomeView : PresenterView {
        fun setTransactions(transactions: List<WalletTransaction>)
        fun updateBalance(old: Long, new: Long)
        fun showProgress(progress: Int)
        fun setActiveTransactions(activeTransactionMap: Map<ActiveTransaction, TransactionState>)
        fun onCancelledTooLate()
    }

    override suspend fun start() {
        Log.e("@TWIG-t", "homePresenter starting!")
        launchBalanceBinder(synchronizer.repository.balance())
        launchTransactionBinder(synchronizer.repository.allTransactions())
        launchProgressMonitor(synchronizer.downloader.progress())
        launchActiveTransactionMonitor(synchronizer.activeTransactions())
    }

    override fun stop() {
        Log.e("@TWIG-t", "homePresenter stopping!")
        job.cancel()
    }

    private fun CoroutineScope.launchBalanceBinder(channel: ReceiveChannel<Long>) = launch {
        var old: Long? = null
        Log.e("@TWIG-t", "balance binder starting!")
        for (new in channel) {
            Log.e("@TWIG-t", "polled a balance item")
            bind(old, new).also { old = new }
        }
        Log.e("@TWIG", "balance binder exiting!")
    }

    private fun CoroutineScope.launchTransactionBinder(channel: ReceiveChannel<List<WalletTransaction>>) = launch {
        Log.e("@TWIG", "transaction binder starting!")
        for (walletTransactionList in channel) {
            Log.e("@TWIG", "received ${walletTransactionList.size} transactions for presenting")
            bind(walletTransactionList)
        }
        Log.e("@TWIG", "transaction binder exiting!")
    }

    private fun CoroutineScope.launchProgressMonitor(channel: ReceiveChannel<Int>) = launch {
        Log.e("@TWIG", "progress monitor starting on thread ${Thread.currentThread().name}!")
        for (i in channel) {
            bind(i)
        }
        Log.e("@TWIG", "progress monitor exiting!")
    }

    private fun CoroutineScope.launchActiveTransactionMonitor(channel: ReceiveChannel<Map<ActiveTransaction, TransactionState>>) = launch {
        Log.e("@TWIG-v", "active transaction monitor starting!")
        for (i in channel) {
            bind(i)
        }
        Log.e("@TWIG-v", "active transaction monitor exiting!")
    }


    //
    // View Callbacks on Main Thread
    //

    private fun bind(old: Long?, new: Long) = onMain {
        Log.e("@TWIG-t", "binding balance of $new")
        view.updateBalance(old ?: 0L, new)
    }


    private fun bind(transactions: List<WalletTransaction>) = onMain {
        Log.e("@TWIG-t", "binding ${transactions.size} walletTransactions")
        view.setTransactions(transactions)
    }

    private fun bind(progress: Int) = onMain {
        view.showProgress(progress)
    }

    private fun bind(activeTransactionMap: Map<ActiveTransaction, TransactionState>) = onMain {
        Log.e("@TWIG-v", "binding a.t. map of size ${activeTransactionMap.size}")
        if (activeTransactionMap.isNotEmpty()) view.setActiveTransactions(activeTransactionMap)
    }

    fun onCancelActiveTransaction(transaction: ActiveSendTransaction) {
        Log.e("@TWIG", "requesting to cancel send for transaction ${transaction.internalId}")
        val isTooLate = !synchronizer.cancelSend(transaction)
        if (isTooLate) {
            view.onCancelledTooLate()
        }
    }

    private fun onMain(block: () -> Unit) = launch {
        withContext(Main) {
            Log.e("@TWIG-t", "running task on main thread - start ${coroutineContext[Job]} | ${coroutineContext[CoroutineName]}")
            block()
            Log.e("@TWIG-t", "running task on main thread - complete")
        }
    }

}

