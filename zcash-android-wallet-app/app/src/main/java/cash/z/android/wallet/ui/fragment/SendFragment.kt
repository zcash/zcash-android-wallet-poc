package cash.z.android.wallet.ui.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpannable
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.FragmentNavigatorExtras
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentSendBinding
import cash.z.android.wallet.extention.afterTextChanged
import cash.z.android.wallet.extention.toAppColor
import cash.z.android.wallet.extention.tryIgnore
import cash.z.android.wallet.ui.activity.MainActivity
import cash.z.android.wallet.ui.presenter.SendPresenter
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.launch
import java.text.DecimalFormat


/**
 * Fragment for sending Zcash.
 *
 */
class SendFragment : BaseFragment(), SendPresenter.SendView {

    lateinit var sendPresenter: SendPresenter
    lateinit var binding: FragmentSendBinding

    private val zecFormatter = DecimalFormat("#.######")
    private val usdFormatter = DecimalFormat("###,###,###.##")
    private val zecSelected get() = binding.groupZecSelected.visibility == View.VISIBLE

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).let { mainActivity ->
            mainActivity.setSupportActionBar(view.findViewById(R.id.toolbar))
            mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            mainActivity.supportActionBar?.setTitle(R.string.destination_title_send)
        }
        initPresenter()
        initDialog()
    }

    // temporary function until presenter is setup
    private fun initPresenter() {
        binding.imageSwapCurrency.setOnClickListener {
            onToggleCurrency()
        }

        binding.textValueHeader.afterTextChanged {
            tryIgnore {
                val value = binding.textValueHeader.text.toString().toDouble()
                binding.textValueSubheader.text = if (zecSelected) {
                    usdFormatter.format(value * MainActivity.USD_PER_ZEC)
                } else {
                    zecFormatter.format(value / MainActivity.USD_PER_ZEC)
                }
            }
        }

        binding.textAreaMemo.afterTextChanged {
            binding.textMemoCharCount.text = "${binding.textAreaMemo.text.length} / ${resources.getInteger(R.integer.max_memo_length)}"
        }

        binding.buttonSendZec.setOnClickListener {
            showSendDialog()
        }

    }

    private fun initDialog() {
        binding.dialogSendBackground.setOnClickListener {
            hideSendDialog()
        }
        binding.dialogSubmitButton.setOnClickListener {
            sendPresenter.onDialogConfirm()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sendPresenter = SendPresenter(this, mainActivity.synchronizer)
        onToggleCurrency()
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

    override fun submit(){
        val extras = FragmentNavigatorExtras(
            binding.dialogSendContents to getString(R.string.transition_active_transaction_background),
            binding.dialogTextTitle to getString(R.string.transition_active_transaction_title),
            binding.dialogTextAddress to getString(R.string.transition_active_transaction_address)
        )

        mainActivity.navController.navigate(R.id.nav_home_fragment,
            null,
            null,
            extras)
    }

    fun onToggleCurrency() {
        val headerValue = binding.textValueHeader.text
        binding.textValueHeader.setText(binding.textValueSubheader.text)
        binding.textValueSubheader.text = headerValue
        if (zecSelected) {
            binding.groupZecSelected.visibility = View.GONE
            binding.groupUsdSelected.visibility = View.VISIBLE
        } else {
            binding.groupZecSelected.visibility = View.VISIBLE
            binding.groupUsdSelected.visibility = View.GONE
        }
    }

    override fun updateBalance(old: Long, new: Long) {
        val zecBalance = new / 100000000.0
        val usdBalance = zecBalance * MainActivity.USD_PER_ZEC
        val availableZecFormatter = DecimalFormat("#.########")
        // TODO: use a formatted string resource here
        val availableTextSpan = "${availableZecFormatter.format(zecBalance)} ZEC Available".toSpannable()
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
        setSendEnabled(false) // partially because we need to lower the button elevation
        binding.groupDialogSend.visibility = View.VISIBLE
    }

    private fun hideSendDialog() {
        setSendEnabled(true)
        binding.groupDialogSend.visibility = View.GONE
    }

    private fun setSendEnabled(isEnabled: Boolean) {
        binding.buttonSendZec.isEnabled = isEnabled
        if (isEnabled) {
            binding.buttonSendZec.text = "send zec"
            binding.progressSend.visibility = View.GONE
        } else {
            binding.buttonSendZec.text = "sending..."
            binding.progressSend.visibility = View.VISIBLE
        }
    }
}


@Module
abstract class SendFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeSendFragment(): SendFragment
}
