package cash.z.android.wallet.ui.presenter

import android.util.Log
import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.entity.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
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
        Log.e("@TWIG-v", "sendPresenter starting!")
        with(view) {
            balanceJob = launchBalanceBinder(synchronizer.repository.balance())
        }
    }

    override fun stop() {
        Log.e("@TWIG-v", "sendPresenter stopping!")
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
        //TODO: prehaps grab the activity scope or let the sycnchronizer have scope and make that function not suspend
        // also, we need to handle cancellations. So yeah, definitely do this differently
        GlobalScope.launch {
            val zatoshi = Math.round(value * 1e8)
            synchronizer.sendToAddress(zatoshi, toAddress)
        }
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
