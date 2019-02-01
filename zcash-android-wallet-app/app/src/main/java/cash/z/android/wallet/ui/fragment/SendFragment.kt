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
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import cash.z.android.wallet.R
import cash.z.android.wallet.extention.Toaster
import cash.z.android.wallet.extention.afterTextChanged
import cash.z.android.wallet.extention.toAppColor
import cash.z.android.wallet.extention.tryIgnore
import cash.z.android.wallet.ui.activity.MainActivity
import cash.z.android.wallet.ui.presenter.SendPresenter
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat


/**
 * Fragment for sending Zcash.
 *
 */
class SendFragment : BaseFragment(), SendPresenter.SendView {

    lateinit var sendPresenter: SendPresenter


    private val zecFormatter = DecimalFormat("#.######")
    private val usdFormatter = DecimalFormat("###,###,###.##")
    private val zecSelected get() = group_zec_selected.visibility == View.VISIBLE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).let { mainActivity ->
            mainActivity.setSupportActionBar(view.findViewById(R.id.toolbar))
            mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            mainActivity.supportActionBar?.setTitle(R.string.destination_title_send)
        }
        initPresenter()
    }

    // temporary function until presenter is setup
    private fun initPresenter() {
        image_swap_currency.setOnClickListener {
            onToggleCurrency()
        }

        text_value_header.afterTextChanged {
            tryIgnore {
                val value = text_value_header.text.toString().toDouble()
                text_value_subheader.text = if (zecSelected) {
                    usdFormatter.format(value * MainActivity.USD_PER_ZEC)
                } else {
                    zecFormatter.format(value / MainActivity.USD_PER_ZEC)
                }
            }
        }

        text_area_memo.afterTextChanged {
            text_memo_char_count.text = "${text_area_memo.text.length} / ${resources.getInteger(R.integer.max_memo_length)}"
        }

        button_send_zec.setOnClickListener {
//            onSendZec()
            onSendSuccess()
        }

//        onBalanceUpdated(12.82129334)
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

    override fun onSendSuccess() {
        setSendEnabled(true)

        val enterTransitionSet = TransitionSet()
        enterTransitionSet.addTransition(TransitionInflater.from(mainActivity).inflateTransition(android.R.transition.explode))
        enterTransitionSet.duration = 1000L
        enterTransitionSet.startDelay = 10L
        this.sharedElementEnterTransition = enterTransitionSet
        this.sharedElementReturnTransition = enterTransition


//        mainActivity.navController.navigateUp()
        val extras = FragmentNavigatorExtras(
            transition_active_transaction_bg to getString(R.string.transition_active_transaction))

        mainActivity.navController.navigate(R.id.nav_home_fragment,
            null,
            null,
            extras)
    }

    override fun onSendFailure() {
        setSendEnabled(true)
        Toaster.short("Sending FAILED!")
    }

    fun onToggleCurrency() {
        val headerValue = text_value_header.text
        text_value_header.setText(text_value_subheader.text)
        text_value_subheader.text = headerValue
        if (zecSelected) {
            group_zec_selected.visibility = View.GONE
            group_usd_selected.visibility = View.VISIBLE
        } else {
            group_zec_selected.visibility = View.VISIBLE
            group_usd_selected.visibility = View.GONE
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
        text_zec_value_available.text = availableTextSpan
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

    fun setSendEnabled(isEnabled: Boolean) {
        button_send_zec.isEnabled = isEnabled
        if (isEnabled) {
            button_send_zec.text = "send zec"
            progress_send.visibility = View.GONE
        } else {
            button_send_zec.text = "sending..."
            progress_send.visibility = View.VISIBLE
        }
    }
}


@Module
abstract class SendFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeSendFragment(): SendFragment
}
