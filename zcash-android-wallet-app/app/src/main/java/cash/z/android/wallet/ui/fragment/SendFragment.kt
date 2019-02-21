package cash.z.android.wallet.ui.fragment

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.text.toSpannable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentSendBinding
import cash.z.android.wallet.extention.*
import cash.z.android.wallet.sample.SampleProperties
import cash.z.android.wallet.ui.presenter.SendPresenter
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import cash.z.wallet.sdk.ext.safelyConvertToBigDecimal
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.launch

/**
 * Fragment for sending Zcash.
 *
 */
class SendFragment : BaseFragment(), SendPresenter.SendView, ScanFragment.BarcodeCallback {

    private val zec = R.string.zec_abbreviation.toAppString()
    private val usd = R.string.usd_abbreviation.toAppString()

    lateinit var sendPresenter: SendPresenter
    lateinit var binding: FragmentSendBinding


    //
    // Lifecycle
    //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return DataBindingUtil.inflate<FragmentSendBinding>(
            inflater, R.layout.fragment_send, container, false
        ).let {
            binding = it
            it.root
        }
    }

    override fun onAttachFragment(childFragment: Fragment?) {
        super.onAttachFragment(childFragment)
        (childFragment as? ScanFragment)?.barcodeCallback = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity.setToolbarShown(true)
        sendPresenter = SendPresenter(this, mainActivity.synchronizer)
    }

    override fun onResume() {
        super.onResume()
        launch {
            sendPresenter.start()
        }
    }

    override fun onPause() {
        super.onPause()
        sendPresenter.stop()
    }


    //
    // SendView Implementation
    //

    override fun exit() {
        mainActivity.navController.navigate(R.id.nav_home_fragment)
    }

    override fun setHeaders(isUsdSelected: Boolean, headerString: String, subheaderString: String) {
        showCurrencySymbols(isUsdSelected)
        setHeaderValue(headerString)
        setSubheaderValue(subheaderString, isUsdSelected)
    }

    override fun setHeaderValue(value: String) {
        binding.textValueHeader.setText(value)
    }

    @SuppressLint("SetTextI18n") // SetTextI18n lint logic has errors and does not recognize that the entire string contains variables, formatted per locale and loaded from string resources.
    override fun setSubheaderValue(value: String, isUsdSelected: Boolean) {
        val subheaderLabel = if (isUsdSelected) zec else usd
        binding.textValueSubheader.text = "$value $subheaderLabel" //ignore SetTextI18n error here because it is invalid
    }

    override fun showSendDialog(zecString: String, usdString: String, toAddress: String, hasMemo: Boolean) {
        hideKeyboard()
        setSendEnabled(false) // partially because we need to lower the button elevation
        binding.dialogTextTitle.text = getString(R.string.send_dialog_title, zecString, zec, usdString)
        binding.dialogTextAddress.text = toAddress
        binding.dialogTextMemoIncluded.visibility = if(hasMemo) View.VISIBLE else View.GONE
        binding.groupDialogSend.visibility = View.VISIBLE
    }

    override fun updateBalance(new: Long) {
        // TODO: use a formatted string resource here
        val availableTextSpan = "${new.convertZatoshiToZecString(8)} $zec Available".toSpannable()
        availableTextSpan.setSpan(ForegroundColorSpan(R.color.colorPrimary.toAppColor()), availableTextSpan.length - "Available".length, availableTextSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        availableTextSpan.setSpan(StyleSpan(Typeface.BOLD), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textZecValueAvailable.text = availableTextSpan
    }

    override fun setSendEnabled(isEnabled: Boolean) {
        binding.buttonSendZec.isEnabled = isEnabled
    }


    //
    // ScanFragment.BarcodeCallback implemenation
    //

    override fun onBarcodeScanned(value: String) {
        exitScanMode()
        binding.inputZcashAddress.setText(value)
        sendPresenter.inputAddressUpdated(value)
    }


    //
    // Internal View Logic
    //

    /**
     * Initialize view logic only. Click listeners, text change handlers and tooltips.
     */
    private fun init() {

        /* Init - Text Input */

        binding.textValueHeader.apply {
            setSelectAllOnFocus(true)
            afterTextChanged { if (it.isNotEmpty()) sendPresenter.inputHeaderUpdating(it) }
            doOnDoneOrFocusLost { sendPresenter.inputHeaderUpdated(it) }
        }

        binding.inputZcashAddress.apply {
            afterTextChanged { if (it.isNotEmpty()) sendPresenter.inputAddressUpdating(it) }
            doOnDoneOrFocusLost { sendPresenter.inputAddressUpdated(it) }
        }

        binding.textAreaMemo.apply {
            afterTextChanged {
                if (it.isNotEmpty()) sendPresenter.inputMemoUpdating(it)
                binding.textMemoCharCount.text = "${text.length} / ${resources.getInteger(R.integer.memo_max_length)}"
            }
            doOnDoneOrFocusLost { sendPresenter.inputMemoUpdated(it) }
        }

        /* Init - Taps */

        binding.imageSwapCurrency.setOnClickListener {
            // validate the amount before we toggle (or else we lose their uncommitted change)
            sendPresenter.inputHeaderUpdated(binding.textValueHeader.text.toString())
            sendPresenter.inputToggleCurrency()
        }
        binding.buttonSendZec.setOnClickListener{
            exitScanMode()
            sendPresenter.inputSendPressed()
        }

        // allow background taps to dismiss the keyboard and clear focus
        binding.contentFragmentSend.setOnClickListener {
            sendPresenter.invalidate()
            hideKeyboard()
        }

        /* Non-Presenter calls (UI-only logic) */

        binding.imageScanQr.apply {
            TooltipCompat.setTooltipText(this, context.getString(R.string.send_tooltip_scan_qr))
        }

        binding.imageAddressShortcut?.apply {
            if (BuildConfig.DEBUG) {
                visibility = View.VISIBLE
                TooltipCompat.setTooltipText(this, context.getString(R.string.send_tooltip_address_shortcut))
                setOnClickListener(::onPasteShortcutAddress)
            } else {
                visibility = View.GONE
            }
        }
        binding.dialogSendBackground.setOnClickListener { hideSendDialog() }
        binding.dialogSubmitButton.setOnClickListener { onSendZec() }
        binding.imageScanQr.setOnClickListener(::onScanQrCode)
        binding.buttonSendZec.text = getString(R.string.send_button_label, zec)
        setSendEnabled(false)
    }

    private fun showCurrencySymbols(isUsdSelected: Boolean) {
        // visibility has some kind of bug that appears to be related to layout groups. So using alpha instead since our API level is high enough to support that
        if (isUsdSelected) {
            binding.textDollarSymbolHeader.alpha = 1.0f
            binding.imageZecSymbolSubheader.alpha = 1.0f
            binding.imageZecSymbolHeader.alpha = 0.0f
            binding.textDollarSymbolSubheader.alpha = 0.0f
        } else {
            binding.imageZecSymbolHeader.alpha = 1.0f
            binding.textDollarSymbolSubheader.alpha = 1.0f
            binding.textDollarSymbolHeader.alpha = 0.0f
            binding.imageZecSymbolSubheader.alpha = 0.0f
        }
    }

    private fun onScanQrCode(view: View) {
        hideKeyboard()
        val fragment = ScanFragment()
        val ft = childFragmentManager.beginTransaction()
            .add(R.id.camera_placeholder, fragment, "camera_fragment")
            .addToBackStack("camera_fragment_scanning")
            .commit()

        binding.groupHiddenDuringScan.visibility = View.INVISIBLE
        binding.buttonCancelScan.apply {
            visibility = View.VISIBLE
            animate().alpha(1.0f).apply {
                duration = 3000L
            }
            setOnClickListener {
                exitScanMode()
            }
        }
    }

    // TODO: possibly move this behavior to only live in the debug build. Perhaps with a viewholder that I just delegate to. Then inject the holder in this class with production verstion getting an empty implementation that just hides the icon.
    private fun onPasteShortcutAddress(view: View) {
        view.context.alert(R.string.send_alert_shortcut_clicked) {
            val address = SampleProperties.wallet.defaultSendAddress
            binding.inputZcashAddress.setText(address)
            sendPresenter.inputAddressUpdated(address)
            hideKeyboard()
        }
    }

    /**
     * Called after confirmation dialog is affirmed. Begins the process of actually sending ZEC.
     */
    private fun onSendZec() {
        setSendEnabled(false)
        sendPresenter.sendFunds()
    }

    private fun exitScanMode() {
        val cameraFragment = childFragmentManager.findFragmentByTag("camera_fragment")
        if (cameraFragment != null) {
            val ft = childFragmentManager.beginTransaction()
                .remove(cameraFragment)
                .commit()
        }
        binding.buttonCancelScan.visibility = View.GONE
        binding.groupHiddenDuringScan.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        mainActivity.getSystemService<InputMethodManager>()
            ?.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        checkAllInput()
    }

    private fun hideSendDialog() {
        setSendEnabled(true)
        binding.groupDialogSend.visibility = View.GONE
    }

    private fun setAddressLineColor(@ColorRes colorRes: Int = R.color.zcashBlack_12) {
        DrawableCompat.setTint(
            binding.inputZcashAddress.background,
            ContextCompat.getColor(mainActivity, colorRes)
        )
    }


    /* Error handling */

    override fun setAmountError(message: String?) {
        if (message == null) {
            binding.textValueError.visibility = View.GONE
            binding.textValueError.text = null
        } else {
            binding.textValueError.text = message
            binding.textValueError.visibility = View.VISIBLE
            setSendEnabled(false)
        }
    }

    override fun setAddressError(message: String?) {
        if (message == null) {
            setAddressLineColor()
            binding.textAddressError.text = null
            binding.textAddressError.visibility = View.GONE
        } else {
            setAddressLineColor(R.color.zcashRed)
            binding.textAddressError.text = message
            binding.textAddressError.visibility = View.VISIBLE
            setSendEnabled(false)
        }
    }

    override fun setMemoError(message: String?) {
        val validColor = R.color.zcashBlack_12.toAppColor()
        val errorColor = R.color.zcashRed.toAppColor()
        if (message == null) {
            binding.dividerMemo.setBackgroundColor(validColor)
            binding.textMemoCharCount.setTextColor(validColor)
            binding.textAreaMemo.setTextColor(R.color.text_dark.toAppColor())
        } else {
            binding.dividerMemo.setBackgroundColor(errorColor)
            binding.textMemoCharCount.setTextColor(errorColor)
            binding.textAreaMemo.setTextColor(errorColor)
            setSendEnabled(false)
        }
    }

    /**
     * Validate all input. This is essentially the same as extracting a model out of the view and validating it with the
     * presenter. Basically, this needs to happen anytime something is edited, in order to try and enable Send. Right
     * now this method is called 1) any time the model is updated with valid input, 2) anytime the keyboard is hidden,
     * and 3) anytime send is pressed. It also triggers the only logic that can set "requiresValidation" to false.
     */
    override fun checkAllInput(): Boolean {
        with(binding) {
            return sendPresenter.validateAll(
                headerValue = textValueHeader.text.toString(),
                toAddress = inputZcashAddress.text.toString(),
                memo = textAreaMemo.text.toString()
            )
        }
    }


// TODO: come back to this test code later and fix the shared element transitions
//
//    fun submitWithSharedElements() {
//        var extras = with(binding) {
//            listOf(dialogSendBackground, dialogSendContents, dialogTextTitle, dialogTextAddress)
//                .map{ it to it.transitionName }
//                .let { FragmentNavigatorExtras(*it.toTypedArray()) }
//        }
//        val extras = FragmentNavigatorExtras(
//            binding.dialogSendContents to binding.dialogSendContents.transitionName,
//            binding.dialogTextTitle to getString(R.string.transition_active_transaction_title),
//            binding.dialogTextAddress to getString(R.string.transition_active_transaction_address),
//            binding.dialogSendBackground to getString(R.string.transition_active_transaction_background)
//        )
//
//        mainActivity.navController.navigate(R.id.nav_home_fragment,
//            null,
//            null,
//            extras)
//    }


}


@Module
abstract class SendFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeSendFragment(): SendFragment
}
