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
import cash.z.android.wallet.ui.activity.MainActivity
import cash.z.android.wallet.ui.presenter.SendPresenter
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import cash.z.wallet.sdk.ext.safelyConvertToBigDecimal
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import kotlin.math.absoluteValue

/**
 * Fragment for sending Zcash.
 *
 */
class SendFragment : BaseFragment(), SendPresenter.SendView, ScanFragment.BarcodeCallback {

    lateinit var sendPresenter: SendPresenter
    lateinit var binding: FragmentSendBinding

    private val zec = R.string.zec_abbreviation.toAppString()
    private val usd = R.string.usd_abbreviation.toAppString()


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

    override fun submit() {
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


    //
    // ScanFragment.BarcodeCallback implemenation
    //

    override fun onBarcodeScanned(value: String) {
        exitScanMode()
        binding.inputZcashAddress.setText(value)
        validateAddressInput()
    }


    //
    // Internal View Logic
    //

    /**
     * Initialize view logic only. Click listeners, text change handlers and tooltips.
     */
    private fun init() {
        /* Presenter calls */

        binding.imageSwapCurrency.setOnClickListener {
            sendPresenter.toggleCurrency()
        }

        binding.textValueHeader.apply {
            afterTextChanged {
                sendPresenter.headerUpdating(it)
            }
        }

        binding.buttonSendZec.setOnClickListener {
            sendPresenter.sendPressed()
        }

        /* Non-Presenter calls (UI-only logic) */

        binding.textAreaMemo.afterTextChanged {
            binding.textMemoCharCount.text =
                    "${binding.textAreaMemo.text.length} / ${resources.getInteger(R.integer.memo_max_length)}"
        }

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

        binding.dialogSendBackground.setOnClickListener {
            hideSendDialog()
        }
        binding.dialogSubmitButton.setOnClickListener {
            onSendZec()
        }

        binding.imageScanQr.setOnClickListener(::onScanQrCode)

        // allow background taps to dismiss the keyboard and clear focus
        binding.contentFragmentSend.setOnClickListener {
            it?.findFocus()?.clearFocus()
            validateUserInput()
            hideKeyboard()
        }

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

    // TODO: possibly move this behavior to only live in the debug build. Perhaps with a viewholder that I just delegate to. Then inject the holder here.
    private fun onPasteShortcutAddress(view: View) {
        view.context.alert(R.string.send_alert_shortcut_clicked) {
            binding.inputZcashAddress.setText(SampleProperties.wallet.defaultSendAddress)
            validateAddressInput()
            hideKeyboard()
        }
    }

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
    }

    private fun hideSendDialog() {
        setSendEnabled(true)
        binding.groupDialogSend.visibility = View.GONE
    }

    // note: be careful calling this with `true` that should only happen when all conditions have been validated
    private fun setSendEnabled(isEnabled: Boolean) {
        binding.buttonSendZec.isEnabled = isEnabled
    }

    private fun setAddressError(message: String?) {
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

    private fun setAddressLineColor(@ColorRes colorRes: Int = R.color.zcashBlack_12) {
        DrawableCompat.setTint(
            binding.inputZcashAddress.background,
            ContextCompat.getColor(mainActivity, colorRes)
        )
    }

    private fun setAmountError(isError: Boolean) {
        val color = if (isError) R.color.zcashRed else R.color.text_dark
        binding.textAmountBackground.setTextColor(color.toAppColor())
    }


    //
    // Validation
    //

    override fun validateUserInput(): Boolean {
        val allValid = validateAddressInput() && validateAmountInput() && validateMemo()
        setSendEnabled(allValid)
        return  allValid
    }

    /**
     * Validate the memo input and update presenter when valid.
     *
     * @return true when the memo is valid
     */
    private fun validateMemo(): Boolean {
        val memo = binding.textAreaMemo.text.toString()
        return memo.all { it.isLetterOrDigit() }.also { if (it) sendPresenter.memoValidated(memo) }
    }

    /**
     * Validate the address input and update presenter when valid.
     *
     * @return true when the address is valid
     */
    private fun validateAddressInput(): Boolean {
        var isValid = false
        val address = binding.inputZcashAddress.text.toString()
        if (address.isNotEmpty() && address.length < R.integer.z_address_min_length.toAppInt()) setAddressError(R.string.send_error_address_too_short.toAppString())
        else if (address.any { !it.isLetterOrDigit() }) setAddressError(R.string.send_error_address_invalid_char.toAppString())
        else setAddressError(null).also { isValid = true; sendPresenter.addressValidated(address) }
        return isValid
    }

    /**
     * Validate the amount input and update the presenter when valid.
     *
     * @return true when the amount is valid
     */
    private fun validateAmountInput(): Boolean {
        return try {
            val amount = binding.textValueHeader.text.toString().safelyConvertToBigDecimal()!!
            sendPresenter.headerValidated(amount)
            setAmountError(false)
            true
        } catch (t: Throwable) {
            Toaster.short("Invalid ZEC or USD value")
            setSendEnabled(false)
            setAmountError(true)
            false
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
