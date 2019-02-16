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
import androidx.navigation.fragment.FragmentNavigatorExtras
import cash.z.android.qrecycler.QScanner
import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentSendBinding
import cash.z.android.wallet.extention.*
import cash.z.android.wallet.sample.SampleProperties
import cash.z.android.wallet.sample.SampleProperties.DEV_MODE
import cash.z.android.wallet.ui.activity.MainActivity
import cash.z.android.wallet.ui.presenter.SendPresenter
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.absoluteValue


/**
 * Fragment for sending Zcash.
 *
 */
class SendFragment : BaseFragment(), SendPresenter.SendView {

    @Inject
    lateinit var qrCodeScanner: QScanner
    lateinit var sendPresenter: SendPresenter
    lateinit var binding: FragmentSendBinding

    private val zecFormatter = DecimalFormat("#.######")
    private val usdFormatter = DecimalFormat("###,###,##0.00")
    private val usdSelected get() = binding.groupUsdSelected.visibility == View.VISIBLE

    private val zec = R.string.zec_abbreviation.toAppString()
    private val usd = R.string.usd_abbreviation.toAppString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val enterTransitionSet = TransitionInflater.from(mainActivity).inflateTransition(R.transition.transition_zec_sent).apply {
//            duration = 3500L
//        }
//
//        this.sharedElementReturnTransition = enterTransitionSet
//        this.sharedElementEnterTransition = enterTransitionSet
//
//        this.allowReturnTransitionOverlap = false
//        allowEnterTransitionOverlap = false

        return DataBindingUtil.inflate<FragmentSendBinding>(
            inflater, R.layout.fragment_send, container, false
        ).let {
            binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).let { mainActivity ->
            mainActivity.setSupportActionBar(view.findViewById(R.id.toolbar))
            mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            mainActivity.supportActionBar?.setTitle(R.string.destination_title_send)
        }
        init()
        initDialog()
    }

    // temporary function until presenter is setup
    private fun init() {
        binding.imageSwapCurrency.setOnClickListener {
            onToggleCurrency()
        }

        binding.textValueHeader.apply {
            afterTextChanged {
                tryIgnore {
                    // only update things if the user is actively editing. in other words, don't update on programmatic changes
                    if (binding.textValueHeader.hasFocus()) {
                        val value = binding.textValueHeader.text.toString().toDouble()
                        binding.textValueSubheader.text = if (usdSelected) {
                            zecFormatter.format(value / SampleProperties.USD_PER_ZEC) + " $zec"
                        } else {
                            if (value == 0.0) "0 $usd"
                            else usdFormatter.format(value * SampleProperties.USD_PER_ZEC) + " $usd"
                        }
                    }
                }
            }
        }

        binding.textAreaMemo.afterTextChanged {
            binding.textMemoCharCount.text =
                    "${binding.textAreaMemo.text.length} / ${resources.getInteger(R.integer.memo_max_length)}"
        }

        binding.buttonSendZec.setOnClickListener {
            showSendDialog()
        }
        binding.buttonSendZec.isEnabled = false

        with(binding.imageScanQr) {
            TooltipCompat.setTooltipText(this, context.getString(R.string.send_tooltip_scan_qr))
        }
        binding.imageAddressShortcut?.apply {
            if (BuildConfig.DEBUG) {
                TooltipCompat.setTooltipText(this, context.getString(R.string.send_tooltip_address_shortcut))
                setOnClickListener(::onPasteShortcutAddress)
            } else {
                visibility = View.GONE
            }
        }
        binding.imageScanQr.setOnClickListener(::onScanQrCode)
        binding.textValueHeader.setText("0")
        binding.textValueSubheader.text =
                mainActivity.resources.getString(R.string.send_subheader_value, if (usdSelected) zec else usd)

        // allow background taps to dismiss the keyboard and clear focus
        binding.contentFragmentSend.setOnClickListener {
            it?.findFocus()?.clearFocus()
            formatUserInput()
            hideKeyboard()
        }

        setSendEnabled(true)
        onToggleCurrency()
    }

    private fun setAddressLineColor(@ColorRes colorRes: Int = R.color.zcashBlack_12) {
        DrawableCompat.setTint(
            binding.inputZcashAddress.background,
            ContextCompat.getColor(mainActivity, colorRes)
        )
    }

    fun formatUserInput() {
        formatAmountInput()
        formatAddressInput()
    }

    private fun formatAmountInput() {
        val value = binding.textValueHeader.text.toString().toDouble().absoluteValue
        binding.textValueHeader.setText(
            when {
                value == 0.0 -> "0"
                usdSelected -> usdFormatter.format(value)
                else -> zecFormatter.format(value)
            }
        )
    }

    private fun formatAddressInput() {
        val address = binding.inputZcashAddress.text
        if(address.isNotEmpty() && address.length < R.integer.z_address_min_length.toAppInt()) setAddressError(R.string.send_error_address_too_short.toAppString())
        else setAddressError(null)
    }

    private fun setAddressError(message: String?) {
        if (message == null) {
            setAddressLineColor()
            binding.textAddressError.text = null
            binding.textAddressError.visibility = View.GONE
            binding.buttonSendZec.isEnabled = true
        } else {
            setAddressLineColor(R.color.zcashRed)
            binding.textAddressError.text = message
            binding.textAddressError.visibility = View.VISIBLE
            binding.buttonSendZec.isEnabled = false
        }
    }

    private fun initDialog() {
        binding.dialogSendBackground.setOnClickListener {
            hideSendDialog()
        }
        binding.dialogSubmitButton.setOnClickListener {
            if (DEV_MODE) submit() else onSendZec()
        }
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
        if(DEV_MODE) showSendDialog()
    }

    override fun onPause() {
        super.onPause()
        sendPresenter.stop()
    }

    override fun submit() {
        submitNoAnimations()
    }

    private fun submitNoAnimations() {
        mainActivity.navController.navigate(
            R.id.nav_home_fragment,
            null,
            null,
            FragmentNavigatorExtras(binding.dialogTextTitle to "transition_active_transaction_title")
        )
    }

    fun submitWithSharedElements() {
        var extras = with(binding) {
            listOf(dialogSendBackground, dialogSendContents, dialogTextTitle, dialogTextAddress)
                .map{ it to it.transitionName }
                .let { FragmentNavigatorExtras(*it.toTypedArray()) }
        }
//        val extras = FragmentNavigatorExtras(
//            binding.dialogSendContents to binding.dialogSendContents.transitionName,
//            binding.dialogTextTitle to getString(R.string.transition_active_transaction_title),
//            binding.dialogTextAddress to getString(R.string.transition_active_transaction_address),
//            binding.dialogSendBackground to getString(R.string.transition_active_transaction_background)
//        )

        mainActivity.navController.navigate(R.id.nav_home_fragment,
            null,
            null,
            extras)
    }

    @SuppressLint("SetTextI18n")
    fun onToggleCurrency() {
        view?.findFocus()?.clearFocus()
        formatUserInput()
        val isInitiallyUsd = usdSelected // hold this value because we modify visibility here and that's what the value is based on
        val subHeaderValue = binding.textValueSubheader.text.toString().substringBefore(' ')
        val currencyLabelAfterToggle = if (isInitiallyUsd) usd else zec // what is selected is about to move to the subheader where the currency is labelled

        binding.textValueSubheader.post {
            binding.textValueSubheader.text = "${binding.textValueHeader.text} $currencyLabelAfterToggle"
            binding.textValueHeader.setText(subHeaderValue)
        }
        if (isInitiallyUsd) {
            binding.groupZecSelected.visibility = View.VISIBLE
            binding.groupUsdSelected.visibility = View.GONE
        } else {
            binding.groupZecSelected.visibility = View.GONE
            binding.groupUsdSelected.visibility = View.VISIBLE
        }
    }

    private fun onScanQrCode(view: View) {
        hideKeyboard()
        val fragment = ScanFragment()
        val ft = childFragmentManager.beginTransaction()
            .add(R.id.camera_placeholder, fragment, "camera_fragment")
            .commit()
//        val intent = Intent(mainActivity, CameraQrScanner::class.java)
//        mainActivity.startActivity(intent)
//        qrCodeScanner.scanBarcode { barcode: Result<String> ->
//            if (barcode.isSuccess) {
//                binding.inputZcashAddress.setText(barcode.getOrThrow())
//                formatAddressInput()
//            } else {
//                Toaster.short("failed to scan QR code")
//            }
//        }
    }

    // TODO: possibly move this behavior to only live in the debug build. Perhaps with a viewholder that I just delegate to. Then inject the holder here.
    private fun onPasteShortcutAddress(view: View) {
        view.context.alert(R.string.send_alert_shortcut_clicked) {
            binding.inputZcashAddress.setText(SampleProperties.wallet.defaultSendAddress)
            setAddressError(null)
            hideKeyboard()
        }
    }

    override fun updateBalance(old: Long, new: Long) {
        val zecBalance = new / 100000000.0
        val usdBalance = zecBalance * SampleProperties.USD_PER_ZEC
        val availableZecFormatter = DecimalFormat("#.########")
        // TODO: use a formatted string resource here
        val availableTextSpan = "${availableZecFormatter.format(zecBalance)} $zec Available".toSpannable()
        availableTextSpan.setSpan(ForegroundColorSpan(R.color.colorPrimary.toAppColor()), availableTextSpan.length - "Available".length, availableTextSpan.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        availableTextSpan.setSpan(StyleSpan(Typeface.BOLD), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textZecValueAvailable.text = availableTextSpan
    }

    private fun onSendZec() {
        setSendEnabled(false)
//        val currency = if(zecSelected) "ZEC" else "USD"
//        Toaster.short("sending ${text_value_header.text} $currency...")

        //TODO: convert and use only zec amount
//        val amount = text_value_header.text.toString().toDouble()
//        val address = input_zcash_address.text.toString()
        val amount = 0.0018
        val address = "ztestsapling1fg82ar8y8whjfd52l0xcq0w3n7nn7cask2scp9rp27njeurr72ychvud57s9tu90fdqgwdt07lg"
        sendPresenter.sendToAddress(amount, address)
    }


    //
    // Internal View Logic
    //

    private fun showSendDialog() {
        hideKeyboard()

        val address = binding.inputZcashAddress.text
        val headerString = binding.textValueHeader.text.toString()
        val subheaderString = binding.textValueSubheader.text.toString().substringBefore(' ')
        val zecString = if(usdSelected) subheaderString else headerString
        val usdString = if(usdSelected) headerString else subheaderString
        val memo = binding.textAreaMemo.text.toString().trim()

        setSendEnabled(false) // partially because we need to lower the button elevation
        binding.dialogTextTitle.text = getString(R.string.send_dialog_title, zecString, zec, usdString)
        binding.dialogTextAddress.text = address
        binding.dialogTextMemoIncluded.visibility = if(memo.isNotEmpty()) View.VISIBLE else View.GONE
        binding.groupDialogSend.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        mainActivity.getSystemService<InputMethodManager>()
            ?.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun hideSendDialog() {
        setSendEnabled(true)
        binding.groupDialogSend.visibility = View.GONE
    }

    private fun setSendEnabled(isEnabled: Boolean) {
        binding.buttonSendZec.isEnabled = isEnabled
        if (isEnabled) {
            binding.buttonSendZec.text = "send $zec"
//            binding.progressSend.visibility = View.GONE
        } else {
            binding.buttonSendZec.text = "sending..."
//            binding.progressSend.visibility = View.VISIBLE
        }
    }
}


@Module
abstract class SendFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeSendFragment(): SendFragment
}
