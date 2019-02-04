package cash.z.android.wallet.ui.presenter

import android.util.Log
import cash.z.android.wallet.extention.Toaster
import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.android.wallet.vo.WalletTransaction
import cash.z.android.wallet.vo.WalletTransactionStatus
import cash.z.android.wallet.vo.WalletTransactionStatus.RECEIVED
import cash.z.android.wallet.vo.WalletTransactionStatus.SENT
import cash.z.wallet.sdk.data.ActiveTransaction
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.data.TransactionState
import cash.z.wallet.sdk.vo.NoteQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class HomePresenter(
    private val view: HomeView,
    private val synchronizer: Synchronizer
) : Presenter {

    interface HomeView : PresenterView {
        fun setTransactions(transactions: List<WalletTransaction>)
        fun updateBalance(old: Long, new: Long)
        fun showProgress(progress: Int)
        fun setActiveTransactions(activeTransactionMap: Map<ActiveTransaction, TransactionState>)
    }

    private var balanceJob: Job? = null
    private var transactionJob: Job? = null
    private var progressJob: Job? = null
    private var activeTransactionJob: Job? = null

    override suspend fun start() {
        Log.e("@TWIG-t", "homePresenter starting!")
        with(view) {
            balanceJob = launchBalanceBinder(synchronizer.repository.balance())
            transactionJob = launchTransactionBinder(synchronizer.repository.allTransactions())
            progressJob = launchProgressMonitor(synchronizer.downloader.progress())
            activeTransactionJob = launchActiveTransactionMonitor(synchronizer.activeTransactions())
        }
    }

    override fun stop() {
        Log.e("@TWIG-t", "homePresenter stopping!")

        // using nullsafe 'also' to only set these to null when they weren't already null
        balanceJob?.cancel()?.also { balanceJob = null }
        transactionJob?.cancel()?.also { transactionJob = null }
        progressJob?.cancel()?.also { progressJob = null }
        activeTransactionJob?.cancel()?.also { progressJob = null }
    }

    fun CoroutineScope.launchBalanceBinder(channel: ReceiveChannel<Long>) = launch {
        var old: Long? = null
        Log.e("@TWIG-t", "balance binder starting!")
        for (new in channel) {
        Log.e("@TWIG-t", "polled a balance item")
            bind(old, new).also { old = new }
        }
        Log.e("@TWIG", "balance binder exiting!")
    }

    fun CoroutineScope.launchTransactionBinder(channel: ReceiveChannel<List<NoteQuery>>) = launch {
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

    fun CoroutineScope.launchProgressMonitor(channel: ReceiveChannel<Int>) = launch {
        for (i in channel) {
            bind(i)
        }
        Log.e("@TWIG", "progress monitor exiting!")
    }

    fun CoroutineScope.launchActiveTransactionMonitor(channel: ReceiveChannel<Map<ActiveTransaction, TransactionState>>) = launch {
        Log.e("@TWIG-v", "active transaction monitor starting!")
        for (i in channel) {
            bind(i)
        }
        Log.e("@TWIG-v", "active transaction monitor exiting!")
    }

    private fun bind(old: Long?, new: Long) {
        Log.e("@TWIG-t", "binding balance of $new")
        view.updateBalance(old ?: 0L, new)
    }

    private fun bind(transactions: List<WalletTransaction>) {
        Log.e("@TWIG-t", "binding ${transactions.size} walletTransactions")
        view.setTransactions(transactions)
    }

    private fun bind(progress: Int) {
        view.showProgress(progress)
        if(progress == 100) {
            view.launch {
                // TODO: remove this behavior and pull it down into the synchronizer
                Log.e("@TWIG-t", "triggering manual scan!")
                synchronizer.processor.scanBlocks()
            }
        }
    }

    private fun bind(activeTransactionMap: Map<ActiveTransaction, TransactionState>) {
        Log.e("@TWIG-v", "binding a.t. map of size ${activeTransactionMap.size}")
        if (activeTransactionMap.isNotEmpty()) view.setActiveTransactions(activeTransactionMap)
    }


    fun onCancelActiveTransaction() {
        // TODO: hold a reference to the job and cancel it
        Toaster.short("Cancelled transaction!")
    }

    private fun NoteQuery.toWalletTransaction(timeOverride: Long? = null): WalletTransaction {
        // convert time from seconds to milliseconds
        val timestamp = if (timeOverride == null) time * 1000 else timeOverride * 1000
        Log.e("@TWIG-u", "setting timestamp to $timestamp for value $value")
        return WalletTransaction(height, if (sent) SENT else RECEIVED, timestamp, BigDecimal(value / 1e8))
    }


}

