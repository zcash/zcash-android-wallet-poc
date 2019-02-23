package cash.z.android.wallet.ui.presenter

import cash.z.android.wallet.di.annotation.FragmentScope
import cash.z.android.wallet.ui.fragment.HistoryFragment
import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.wallet.sdk.dao.WalletTransaction
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.data.twig
import dagger.Binds
import dagger.Module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class HistoryPresenter @Inject constructor(
    private val view: HistoryFragment,
    private var synchronizer: Synchronizer
) : Presenter {

    private var job: Job? = null

    interface HistoryView : PresenterView {
        fun setTransactions(transactions: List<WalletTransaction>)
    }

    override suspend fun start() {
        job?.cancel()
        job = Job()
        twig("historyPresenter starting!")
        view.launchTransactionBinder(synchronizer.allTransactions())
    }

    override fun stop() {
        twig("historyPresenter stopping!")
        job?.cancel()?.also { job = null }
    }

    private fun CoroutineScope.launchTransactionBinder(channel: ReceiveChannel<List<WalletTransaction>>) = launch {
        twig("transaction binder starting!")
        for (walletTransactionList in channel) {
            twig("received ${walletTransactionList.size} transactions for presenting")
            bind(walletTransactionList)
        }
        twig("transaction binder exiting!")
    }


    //
    // View Callbacks on Main Thread
    //

    private fun bind(transactions: List<WalletTransaction>) {
        twig("binding ${transactions.size} walletTransactions")
        view.setTransactions(transactions.sortedByDescending {
            if (!it.isMined && it.isSend) Long.MAX_VALUE else it.timeInSeconds
        })
    }

}


@Module
abstract class HistoryPresenterModule {
    @Binds
    @FragmentScope
    abstract fun providePresenter(historyPresenter: HistoryPresenter): Presenter
}