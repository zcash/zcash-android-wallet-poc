package cash.z.android.wallet.ui.presenter

interface Presenter<ModelT: Any, ViewT: Any> {
    suspend fun start()
}