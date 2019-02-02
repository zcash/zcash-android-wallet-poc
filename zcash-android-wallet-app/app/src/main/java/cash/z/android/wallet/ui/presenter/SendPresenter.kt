package cash.z.android.wallet.ui.presenter

import android.util.Log
import cash.z.android.wallet.extention.Toaster
import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.vo.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

class SendPresenter(
    private val view: SendView,
    private val synchronizer: Synchronizer
) : Presenter {

    interface SendView : PresenterView {
        fun updateBalance(old: Long, new: Long)
        fun submit()
    }

    private var balanceJob: Job? = null

    override suspend fun start() {
        Log.e("@TWIG-v", "homePresenter starting!")
        with(view) {
            balanceJob = launchBalanceBinder(synchronizer.repository.balance())
        }
    }

    override fun stop() {
        balanceJob?.cancel()?.also { balanceJob = null }
    }

    fun CoroutineScope.launchBalanceBinder(channel: ReceiveChannel<Long>) = launch {
        var old: Long? = null
        Log.e("@TWIG-v", "send balance binder starting!")
        for (new in channel) {
            Log.e("@TWIG-v", "send polled a balance item")
            bind(old, new).also { old = new }
        }
        Log.e("@TWIG-v", "send balance binder exiting!")
    }

    fun sendToAddress(value: Double, toAddress: String) {
        //TODO: pull this logic into SDK
        view.launch {
            val zatoshi = Math.round(value * 1e8)
            val transactionId: Long = synchronizer.wallet.sendToAddress(zatoshi, toAddress)
            val transactionRaw: ByteArray? = findTransaction(transactionId)?.raw
            if (transactionRaw != null) {
                try {
                    Log.e("@TWIG", "attempting to submit transaction $transactionId")
                    synchronizer.downloader.connection.submitTransaction(transactionRaw)
                    Log.e("@TWIG", "successfully submitted")
                    Toaster.short("Successfully sent $value ZEC")
//                    view.onSendSuccess()  -- use CompleteableDeferred here, instead
                } catch (t: Throwable) {
                    Log.e("@TWIG", "submit failed due to $t")
                    try{synchronizer.repository.deleteTransactionById(transactionId)}catch(t:Throwable){}
//                    view.onSendFailure()  -- use CompleteableDeferred here, instead
                }
            } else {
                Log.e("@TWIG", "calling send did not seem to result in a transaction being created")
                try{synchronizer.repository.deleteTransactionById(transactionId)}catch(t:Throwable){}
//                view.onSendFailure()  -- use CompleteableDeferred here, instead
            }
        }
    }

    fun onDialogConfirm() {
        view.submit()
    }

    private suspend fun findTransaction(txId: Long): Transaction? {
        return if (txId < 0) null else synchronizer.repository.findTransactionById(txId)
    }

    fun bind(old: Long?, new: Long) {
        Log.e("@TWIG-v", "binding balance of $new")
        view.updateBalance(old ?: 0L, new)
    }
}
