package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cash.z.android.wallet.R
import cash.z.android.wallet.extention.Toaster
import cash.z.android.wallet.extention.afterTextChanged
import cash.z.android.wallet.extention.tryIgnore
import cash.z.android.wallet.ui.activity.MainActivity
import kotlinx.android.synthetic.main.fragment_send.*
import java.text.DecimalFormat


/**
 * Fragment for sending Zcash.
 *
 */
class SendFragment : Fragment() {

    val zecSelected get() = group_zec_selected.visibility == View.VISIBLE

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

        // todo: move these formatters elsewhere
        val zecFormatter = DecimalFormat("#.######")
        val usdFormatter = DecimalFormat("###,###,###.##")
        text_value_header.afterTextChanged {
            tryIgnore {
                val value = text_value_header.text.toString().toDouble()
                text_value_subheader.text = if (zecSelected) {
                    zecFormatter.format(value * MainActivity.USD_PER_ZEC)
                } else {
                    usdFormatter.format(value / MainActivity.USD_PER_ZEC)
                }
            }
        }

        text_area_memo.afterTextChanged {
            text_memo_char_count.text = "${text_area_memo.text.length} / ${resources.getInteger(R.integer.max_memo_length)}"
        }

        button_send_zec.setOnClickListener {
            onSendZec()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onToggleCurrency()
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

    private fun onSendZec() {
        val currency = if(zecSelected) "ZEC" else "USD"
        Toaster.short("sending ${text_value_header.text} $currency...")
    }
}
