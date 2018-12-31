package cash.z.android.wallet.ui.presenter

import cash.z.android.wallet.data.TransactionRepository
import cash.z.android.wallet.ui.fragment.HomeFragment
import cash.z.android.wallet.vo.WalletTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import java.math.BigDecimal

class HomePresenter(
    private val view: HomeFragment,
    private val repository: TransactionRepository
) : Presenter<TransactionRepository, HomeFragment> {

    private lateinit var balanceJob: Job
    private lateinit var transactionJob: Job

    override suspend fun start() {
        balanceJob = view.scope.launchBalanceBinder(repository.balance())
        transactionJob = view.scope.launchTransactionBinder(repository.transactions())
    }

    fun stop() {
        balanceJob.cancel()
        transactionJob.cancel()
    }

    fun CoroutineScope.launchBalanceBinder(modelChannel: ReceiveChannel<BigDecimal>) = launch {
        var oldModel: BigDecimal? = null
        for (model in modelChannel) {
            bind(model, oldModel).also{ oldModel = model }
        }
    }

    fun CoroutineScope.launchTransactionBinder(transactionChannel: ReceiveChannel<WalletTransaction>) = launch {
        for (tx in transactionChannel) {
            bind(tx)
        }
    }

    fun bind(newModel: BigDecimal, oldModel: BigDecimal?) {
        view.updateBalance(newModel.toDouble(), oldModel?.toDouble() ?: 0.0)
    }

    fun bind(transaction: WalletTransaction) {
        view.addTransaction(transaction)
    }
}
