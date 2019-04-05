package cash.z.android.wallet.ui.presenter

import cash.z.android.wallet.R
import cash.z.android.wallet.di.annotation.FragmentScope
import cash.z.android.wallet.extention.toAppString
import cash.z.android.wallet.sample.SampleProperties
import cash.z.android.wallet.ui.fragment.SendFragment
import cash.z.android.wallet.ui.presenter.Presenter.PresenterView
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.data.Twig
import cash.z.wallet.sdk.data.twig
import cash.z.wallet.sdk.ext.*
import cash.z.wallet.sdk.secure.Wallet
import dagger.Binds
import dagger.Module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

class SendPresenter @Inject constructor(
    private val view: SendFragment,
    private val synchronizer: Synchronizer
) : Presenter {

    interface SendView : PresenterView {
        fun updateAvailableBalance(new: Long)
        fun setHeaders(isUsdSelected: Boolean, headerString: String, subheaderString: String)
        fun setHeaderValue(usdString: String)
        fun setSubheaderValue(usdString: String, isUsdSelected: Boolean)
        fun showSendDialog(zecString: String, usdString: String, toAddress: String, hasMemo: Boolean)
        fun exit()

        // error handling
        fun setAmountError(message: String?)
        fun setAddressError(message: String?)
        fun setMemoError(message: String?)
        fun setSendEnabled(isEnabled: Boolean)
        fun checkAllInput(): Boolean
    }

    /**
     * We require the user to send more than this amount. Right now, we just use the miner's fee as a minimum but other
     * lower bounds may also be useful for validation.
     */
    private val minersFee = 10_000L
    private var balanceJob: Job? = null
    private var requiresValidation = false
    var sendUiModel = SendUiModel()

    // TODO: find the best set of characters here. Possibly add something to the rust layer to help with this.
    private val validMemoChars = " \t\n\r.?!,\"':;-_=+@#%*"



    //
    // LifeCycle
    //

    override suspend fun start() {
        Twig.sprout("SendPresenter")
        twig("sendPresenter starting!")
        // set the currency to zec and update the view, initializing everything to zero
        inputToggleCurrency()
        balanceJob?.cancel()
        balanceJob = Job()
        balanceJob = view.launchBalanceBinder(synchronizer.balance())
    }

    override fun stop() {
        twig("sendPresenter stopping!")
        Twig.clip("SendPresenter")
        balanceJob?.cancel()?.also { balanceJob = null }
    }

    fun CoroutineScope.launchBalanceBinder(channel: ReceiveChannel<Wallet.WalletBalance>) = launch {
        twig("send balance binder starting!")
        for (new in channel) {
            twig("send polled a balance item")
            bind(new)
        }
        twig("send balance binder exiting!")
    }


    //
    // Public API
    //

    fun sendFunds() {
        //TODO: prehaps grab the activity scope or let the sycnchronizer have scope and make that function not suspend
        // also, we need to handle cancellations. So yeah, definitely do this differently
        GlobalScope.launch {
            twig("Process: cash.z.android.wallet. checking....")
            twig("Process: cash.z.android.wallet. is it null??? $sendUiModel")
            synchronizer.sendToAddress(sendUiModel.zatoshiValue!!, sendUiModel.toAddress)
        }
        view.exit()
    }


    //
    // User Input
    //

    /**
     * Called when the user has tapped on the button for toggling currency, swapping zec for usd
     */
    fun inputToggleCurrency() {
        // tricky: this is not really a model update, instead it is a byproduct of using `isUsdSelected` for the
        // currency instead of strong types. There are several todo's to fix that. if we update the model here then
        // the UI will think the user took action and display errors prematurely.
        sendUiModel = sendUiModel.copy(isUsdSelected = !sendUiModel.isUsdSelected)
        with(sendUiModel) {
            view.setHeaders(
                isUsdSelected = isUsdSelected,
                headerString = if (isUsdSelected) usdValue.toUsdString() else zatoshiValue.convertZatoshiToZecString(),
                subheaderString = if (isUsdSelected) zatoshiValue.convertZatoshiToZecString() else usdValue.toUsdString()
            )
        }
    }

    /**
     * As the user is typing the header string, update the subheader string. Do not modify our own internal model yet.
     * Internal model is only modified after [headerUpdated] is called (with valid data).
     */
    fun inputHeaderUpdating(headerValue: String) {
        headerValue.safelyConvertToBigDecimal()?.let { headerValueAsDecimal ->
            val subheaderValue = headerValueAsDecimal.convertCurrency(SampleProperties.USD_PER_ZEC, sendUiModel.isUsdSelected)

            // subheader string contains opposite currency of the selected one. so if usd is selected, format the subheader as zec
            val subheaderString = if(sendUiModel.isUsdSelected) subheaderValue.toZecString() else subheaderValue.toUsdString()

            view.setSubheaderValue(subheaderString, sendUiModel.isUsdSelected)
        }
    }

    /**
     * As the user updates the address, update the error that gets displayed in real-time
     *
     * @param addressValue the address that the user has typed, so far
     */
    fun inputAddressUpdating(addressValue: String) {
        validateAddress(addressValue, true)
    }

    /**
     * As the user updates the memo, update the error that gets displayed in real-time
     *
     * @param memoValue the memo that the user has typed, so far
     */
    fun inputMemoUpdating(memoValue: String) {
        // treat the memo a little differently because it is more likely for the user to go back and edit invalid chars
        // and we want the send button to be active the moment that happens
        if(validateMemo(memoValue)) {
            updateModel(sendUiModel.copy(memo = memoValue))
        }
    }

    /**
     * Called when the user has completed their update to the header value, typically on focus change.
     *
     * @return true when the given amount is parsable, positive and less than the available amount.
     */
    fun inputHeaderUpdated(amountString: String): Boolean {
        if (!validateAmount(amountString)) return false

        // either USD or ZEC -- TODO: use strong typing (and polymorphism) instead of isUsdSelected checks
        val amount = amountString.safelyConvertToBigDecimal()!! // we've already validated this as not null and it's immutable
        with(sendUiModel) {
            if (isUsdSelected) {
                // amount represents USD
                val headerString = amount.toUsdString()
                val zatoshiValue = amount.convertUsdToZec(SampleProperties.USD_PER_ZEC).convertZecToZatoshi()
                val subheaderString = amount.convertUsdToZec(SampleProperties.USD_PER_ZEC).toUsdString()
                updateModel(sendUiModel.copy(zatoshiValue = zatoshiValue, usdValue = amount))
                view.setHeaders(sendUiModel.isUsdSelected, headerString, subheaderString)
            } else {
                // amount represents ZEC
                val headerString = amount.toZecString()
                val usdValue = amount.convertZecToUsd(SampleProperties.USD_PER_ZEC)
                val subheaderString = usdValue.toUsdString()
                updateModel(sendUiModel.copy(zatoshiValue = amount.convertZecToZatoshi(), usdValue = usdValue))
                twig("calling setHeaders with $headerString  $subheaderString")
                view.setHeaders(sendUiModel.isUsdSelected, headerString, subheaderString)
            }
        }
        return true
    }

    /**
     * Called when the user has updated the toAddress, typically on focus change.
     *
     * @return true when the given address' length and content are valid
     */
    fun inputAddressUpdated(newAddress: String): Boolean {
        if (!validateAddress(newAddress)) return false
        updateModel(sendUiModel.copy(toAddress = newAddress))
        return true
    }

    /**
     * Called when the user has updated the memo field, typically after pressing the 'done' key.
     *
     * @return true when the given memo's content does not contain invalid characters
     */
    fun inputMemoUpdated(newMemo: String): Boolean {
        if (!validateMemo(newMemo)) return false
        updateModel(sendUiModel.copy(memo = newMemo))
        return true
    }

    /**
     * Called after the user has pressed the send button and should be shown a confirmation dialog, next.
     *
     * @return true when all input fields contained valid data
     */
    fun inputSendPressed(): Boolean {
        // double sanity check. Make sure view and model agree and are each valid and if not, highlight the error.
        if (!view.checkAllInput() || !validateAll()) return false

        with(sendUiModel) {
            view.showSendDialog(
                zecString = zatoshiValue.convertZatoshiToZecString(),
                usdString = usdValue.toUsdString(),
                toAddress = toAddress,
                hasMemo = !memo.isBlank()
            )
        }
        return true
    }

    fun bind(balanceInfo: Wallet.WalletBalance) {
        val available = balanceInfo.available
        if (available >= 0) {
            twig("binding balance of $available")
            view.updateAvailableBalance(available)
            updateModel(sendUiModel.copy(availableBalance = available))
        }
    }

    fun updateModel(newModel: SendUiModel) {
        sendUiModel = newModel.apply { hasBeenUpdated = true }
        // now that we have new data, check and see if we can clear errors and re-enable the send button
        if (requiresValidation) validateAll()
    }

    //
    // Validation
    //

    /**
     * Called after any user interaction. This is a potential time that errors should be shown, but only if data has
     * already been entered. The view should call this method on focus change.
     */
    fun invalidate() {
        requiresValidation = true
    }

    /**
     * Validates the given memo, ensuring that it does not contain unsupported characters. For now, we're very
     * restrictive until we define more clear requirements for the values that can safely be entered in this field
     * without introducing security risks.
     *
     * @param memo the memo to consider for validation
     *
     * @return true when the memo contains valid characters, which includes being blank
     */
    private fun validateMemo(memo: String): Boolean {
        return if (memo.all { it.isLetterOrDigit() || it in validMemoChars }) {
            view.setMemoError(null)
            true
        } else {
            view.setMemoError("Only letters and numbers are allowed in memo at this time")
            requiresValidation = true
            false
        }
    }

    /**
     * Validates the given address
     *
     * @param toAddress the address to consider for validation
     * @param ignoreLength whether to ignore the length while validating, this is helpful when the user is still
     * actively typing the address
     */
    private fun validateAddress(toAddress: String, ignoreLength: Boolean = false): Boolean {
        // TODO: later expose a method in the synchronizer for validating addresses.
        //  Right now it's not available so we do it ourselves
        return if (!ignoreLength && sendUiModel.hasBeenUpdated && toAddress.length < 20) {// arbitrary length for now
            view.setAddressError(R.string.send_error_address_too_short.toAppString())
            requiresValidation = true
            false
        } else if (!toAddress.startsWith("zt") && !toAddress.startsWith("zs")) {
            view.setAddressError(R.string.send_error_address_invalid_contents.toAppString())
            requiresValidation = true
            false
        } else if (toAddress.any { !it.isLetterOrDigit() }) {
            view.setAddressError(R.string.send_error_address_invalid_char.toAppString())
            requiresValidation = true
            false
        } else {
            view.setAddressError(null)
            true
        }
    }

    /**
     * Validates the given amount, calling the related `showError` methods on the view, when appropriate
     *
     * @param amount the amount to consider for validation, for now this can be either USD or ZEC. In the future we will
     * will separate those into types.
     *
     * @return true when the given amount is valid and all errors have been cleared on the view
     */
    private fun validateAmount(amountString: String): Boolean {
        if (!sendUiModel.hasBeenUpdated) return true // don't mark zero as bad until the model has been updated

        var amount = amountString.safelyConvertToBigDecimal()
        // no need to convert when we know it's null
        return if (amount == null ) {
            validateZatoshiAmount(null)
        } else {
            val zecAmount =
                if (sendUiModel.isUsdSelected) amount.convertUsdToZec(SampleProperties.USD_PER_ZEC) else amount
            validateZatoshiAmount(zecAmount.convertZecToZatoshi())
        }
    }

    private fun validateZatoshiAmount(zatoshiValue: Long?): Boolean {
        return if (zatoshiValue == null || zatoshiValue <= minersFee) {
            view.setAmountError("Please specify a larger amount")
            requiresValidation = true
            false
        } else if (sendUiModel.availableBalance != null
            && zatoshiValue >= sendUiModel.availableBalance!!) {
            view.setAmountError("Exceeds available balance of " +
                    "${sendUiModel.availableBalance.convertZatoshiToZecString(3)}")
            requiresValidation = true
            false
        } else {
            view.setAmountError(null)
            true
        }
    }

    fun validateAll(): Boolean {
        with(sendUiModel) {
            val isValid = validateZatoshiAmount(zatoshiValue)
                    && validateAddress(toAddress)
                    && validateMemo(memo)
            requiresValidation = !isValid
            view.setSendEnabled(isValid)
            return isValid
        }
    }


    data class SendUiModel(
        val availableBalance: Long? = null,
        var hasBeenUpdated: Boolean = false,
        val isUsdSelected: Boolean = true,
        val zatoshiValue: Long? = null,
        val usdValue: BigDecimal = BigDecimal.ZERO,
        val toAddress: String = "",
        val memo: String = ""
    )
}


@Module
abstract class SendPresenterModule {
    @Binds
    @FragmentScope
    abstract fun providePresenter(sendPresenter: SendPresenter): Presenter
}