package cash.z.android.wallet.ui.presenter

import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.data.Twig
import cash.z.wallet.sdk.data.twig
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ProgressPresenter @Inject constructor(
    private val view: ProgressView,
    private var synchronizer: Synchronizer
) : Presenter {

    private var job: Job? = null

    interface ProgressView : PresenterView {
        fun showProgress(progress: Int)
    }


    //
    // LifeCycle
    //

    override suspend fun start() {
        job?.cancel()
        job = Job()
        Twig.sprout("ProgressPresenter")
        twig("starting")
        view.launchProgressMonitor(synchronizer.progress())
    }

    override fun stop() {
        Twig.clip("ProgressPresenter")
        twig("stopping")
        job?.cancel()?.also { job = null }
    }

    private fun CoroutineScope.launchProgressMonitor(channel: ReceiveChannel<Int>) = launch {
        twig("progress monitor starting on thread ${Thread.currentThread().name}!")
        for (i in channel) {
            bind(i)
        }
        // "receive" and send 100, whenever the channel is closed for send
        bind(100)
        twig("progress monitor exiting!")
    }

    private fun bind(progress: Int) = view.launch {
        twig("binding progress of $progress on thread ${Thread.currentThread().name}!")
        view.showProgress(progress)
    }
}