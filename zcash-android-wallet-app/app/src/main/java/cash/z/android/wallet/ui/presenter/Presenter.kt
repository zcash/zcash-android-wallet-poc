package cash.z.android.wallet.ui.presenter

import kotlinx.coroutines.CoroutineScope

interface Presenter {
    suspend fun start()
    fun stop()

    /**
     * A presenter collaborates with a scoped view. The presenter lives within that scope,
     * meaning, when the view dies, the presenter dies, too. To achieve this, the presenter's
     * [start] method should be launched from the view's scope via structured concurrency.
     */
    interface PresenterView : CoroutineScope
}