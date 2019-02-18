package cash.z.android.wallet.ui.presenter

import android.util.Log
import cash.z.android.wallet.sample.SampleProperties
import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.ext.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import java.math.BigDecimal

class SendPresenter(
    private val view: SendView,
    private val synchronizer: Synchronizer
) : Presenter {

    interface SendView : PresenterView {
        fun updateBalance(old: Long, new: Long)
        fun setHeaders(isUsdSelected: Boolean, headerString: String, subheaderString: String)
        fun setHeaderValue(usdString: String)
        fun setSubheaderValue(usdString: String, isUsdSelected: Boolean)
        fun showSendDialog(zecString: String, usdString: String, toAddress: String, hasMemo: Boolean)
        fun validateUserInput(): Boolean
        fun submit()
    }

    private var balanceJob: Job? = null
    var sendUiModel = SendUiModel()

    //
    // LifeCycle
    //

    override suspend fun start() {
        Log.e("@TWIG-v", "sendPresenter starting!")
        // set the currency to zec and update the view, intializing everything to zero
        toggleCurrency()
        with(view) {
            balanceJob = launchBalanceBinder(synchronizer.balance())
        }
    }

    override fun stop() {
        Log.e("@TWIG-v", "sendPresenter stopping!")
        balanceJob?.cancel()?.also { balanceJob = null }
    }

    fun CoroutineScope.launchBalanceBinder(channel: ReceiveChannel<Long>) = launch {
        Log.e("@TWIG-v", "send balance binder starting!")
        for (new in channel) {
            Log.e("@TWIG-v", "send polled a balance item")
            bind(new)
        }
        Log.e("@TWIG-v", "send balance binder exiting!")
    }


    //
    // Public API
    //

    fun sendFunds() {
        //TODO: prehaps grab the activity scope or let the sycnchronizer have scope and make that function not suspend
        // also, we need to handle cancellations. So yeah, definitely do this differently
        GlobalScope.launch {
            synchronizer.sendToAddress(sendUiModel.zecValue!!, sendUiModel.toAddress)
        }
        view.submit()
    }

    /**
     * Called when the user has tapped on the button for toggling currency, swapping zec for usd
     */
    fun toggleCurrency() {
        view.validateUserInput()
        sendUiModel = sendUiModel.copy(isUsdSelected = !sendUiModel.isUsdSelected)
        with(sendUiModel) {
            view.setHeaders(
                isUsdSelected = isUsdSelected,
                headerString = if (isUsdSelected) usdValue.toUsdString() else zecValue.convertZatoshiToZecString(),
                subheaderString = if (isUsdSelected) zecValue.convertZatoshiToZecString() else usdValue.toUsdString()
            )
        }
    }

    /**
     * As the user is typing the header string, update the subheader string. Do not modify our own internal model yet.
     * Internal model is only updated after [headerValidated] is called.
     */
    fun headerUpdating(headerValue: String) {
        headerValue.safelyConvertToBigDecimal()?.let { headerValueAsDecimal ->
            val subheaderValue = headerValueAsDecimal.convertCurrency(SampleProperties.USD_PER_ZEC, sendUiModel.isUsdSelected)

            // subheader string contains opposite currency of the selected one. so if usd is selected, format the subheader as zec
            val subheaderString = if(sendUiModel.isUsdSelected) subheaderValue.toZecString() else subheaderValue.toUsdString()

            view.setSubheaderValue(subheaderString, sendUiModel.isUsdSelected)
        }
    }

    fun sendPressed() {
        with(sendUiModel) {
            view.showSendDialog(
                zecString = zecValue.convertZatoshiToZecString(),
                usdString = usdValue.toUsdString(),
                toAddress = toAddress,
                hasMemo = !memo.isBlank()
            )
        }
    }

    fun headerValidated(amount: BigDecimal) {
        with(sendUiModel) {
            if (isUsdSelected) {
                val headerString = amount.toUsdString()
                val usdValue = amount
                val zecValue = amount.convertUsdToZec(SampleProperties.USD_PER_ZEC)
                val subheaderString = zecValue.toZecString()
                sendUiModel = sendUiModel.copy(zecValue = zecValue.convertZecToZatoshi(), usdValue = usdValue)
                view.setHeaders(sendUiModel.isUsdSelected, headerString, subheaderString)
            } else {
                val headerString = amount.toZecString()
                val zecValue = amount
                val usdValue = amount.convertZecToUsd(SampleProperties.USD_PER_ZEC)
                val subheaderString = usdValue.toUsdString()
                sendUiModel = sendUiModel.copy(zecValue = zecValue.convertZecToZatoshi(), usdValue = usdValue)
                println("calling setHeaders with $headerString  $subheaderString")
                view.setHeaders(sendUiModel.isUsdSelected, headerString, subheaderString)
            }
        }
    }

    fun addressValidated(address: String) {
        sendUiModel = sendUiModel.copy(toAddress = address)
    }

    /**
     * After the user has typed a memo, validated by the UI, then update the model.
     *
     * assert: this method is only called after the memo input has been validated by the UI
     */
    fun memoValidated(sanitizedValue: String) {
        sendUiModel = sendUiModel.copy(memo = sanitizedValue)
    }

    fun bind(newZecBalance: Long) {
        if (newZecBalance >= 0) {
            Log.e("@TWIG-v", "binding balance of $newZecBalance")
            val old = sendUiModel.zecValue
            sendUiModel = sendUiModel.copy(zecValue = newZecBalance)
            view.updateBalance(old ?: 0L, newZecBalance)
        }
    }

    data class SendUiModel(
        val isUsdSelected: Boolean = true,
        val zecValue: Long? = null,
        val usdValue: BigDecimal = BigDecimal.ZERO,
        val toAddress: String = "",
        val memo: String = ""
    )
}