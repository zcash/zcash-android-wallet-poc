package cash.z.android.wallet.ui.presenter

import android.util.Log
import cash.z.android.wallet.extention.Toaster
import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.android.wallet.vo.WalletTransaction
import cash.z.android.wallet.vo.WalletTransactionStatus.RECEIVED
import cash.z.android.wallet.vo.WalletTransactionStatus.SENT
import cash.z.wallet.sdk.data.ActiveTransaction
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.data.TransactionState
import cash.z.wallet.sdk.vo.NoteQuery
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.ReceiveChannel
import java.math.BigDecimal
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

    private fun CoroutineScope.launchTransactionBinder(channel: ReceiveChannel<List<NoteQuery>>) = launch {
        Log.e("@TWIG", "transaction binder starting!")
        for (noteQueryList in channel) {
            Log.e("@TWIG", "received ${noteQueryList.size} transactions for presenting")
            bind(noteQueryList.map {
                val time = updateTimeStamp(it)
                it.toWalletTransaction(time)
            })
        }
        Log.e("@TWIG", "transaction binder exiting!")
    }

    private suspend fun updateTimeStamp(noteQuery: NoteQuery) = synchronizer.updateTimeStamp(noteQuery.height)

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
        if (progress == 100) {
            launch {
                // TODO: remove this behavior and pull it down into the synchronizer
                Log.e("@TWIG-t", "triggering manual scan!")
                synchronizer.processor.scanBlocks()
            }
        }
    }

    private fun bind(activeTransactionMap: Map<ActiveTransaction, TransactionState>) = onMain {
        Log.e("@TWIG-v", "binding a.t. map of size ${activeTransactionMap.size}")
        if (activeTransactionMap.isNotEmpty()) view.setActiveTransactions(activeTransactionMap)
    }

    fun onCancelActiveTransaction() {
        // TODO: hold a reference to the job and cancel it
        Toaster.short("Cancelled transaction!")
    }

    private fun onMain(block: () -> Unit) = launch {
        withContext(Main) {
            Log.e("@TWIG-t", "running task on main thread - start ${coroutineContext[Job]} | ${coroutineContext[CoroutineName]}")
            block()
            Log.e("@TWIG-t", "running task on main thread - complete")
        }
    }

    private fun NoteQuery.toWalletTransaction(timeOverride: Long? = null): WalletTransaction {
        // convert time from seconds to milliseconds
        val timestamp = if (timeOverride == null) time * 1000 else timeOverride * 1000
        Log.e("@TWIG-u", "setting timestamp to $timestamp for value $value")
        return WalletTransaction(height, if (sent) SENT else RECEIVED, timestamp, BigDecimal(value / 1e8))
    }
}

