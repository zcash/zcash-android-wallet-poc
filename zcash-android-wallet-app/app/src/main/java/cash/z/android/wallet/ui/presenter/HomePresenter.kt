package cash.z.android.wallet.ui.presenter

import cash.z.android.wallet.data.TransactionRepository
import cash.z.android.wallet.ui.fragment.HomeFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import java.math.BigDecimal

class HomePresenter(
    private val view: HomeFragment,
    private val repository: TransactionRepository
) : Presenter<TransactionRepository, HomeFragment> {

    lateinit var balanceJob: Job

    override suspend fun start() {
        balanceJob = view.scope.launchBalanceBinder(repository.balance())
    }

    fun stop() {
        balanceJob.cancel()
    }

    fun CoroutineScope.launchBalanceBinder(modelChannel: ReceiveChannel<BigDecimal>) = launch {
        var oldModel: BigDecimal? = null
        for (model in modelChannel) {
            bind(model, oldModel).also{ oldModel = model }
        }
    }

    fun bind(newModel: BigDecimal, oldModel: BigDecimal?) {
        view.setZecValue(newModel.toDouble())
    }
}
