package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cash.z.android.qrecycler.QRecycler
import cash.z.android.wallet.R
import cash.z.android.wallet.ui.activity.MainActivity
import cash.z.android.wallet.ui.util.AddressPartNumberSpan
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.jni.JniConverter
import cash.z.wallet.sdk.secure.Wallet
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.android.synthetic.main.fragment_receive.*
import javax.inject.Inject

/**
 * Fragment representing the receive screen of the app. This is the screen used for receiving funds.
 */
class ReceiveFragment : BaseFragment() {

    @Inject
    lateinit var qrecycler: QRecycler

    @Inject
    lateinit var synchronizer: Synchronizer

    lateinit var addressParts: Array<TextView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receive, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressParts = arrayOf(
            text_address_part_1,
            text_address_part_2,
            text_address_part_3,text_address_part_4,
            text_address_part_5,
            text_address_part_6,
            text_address_part_7,
            text_address_part_8
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity?.setToolbarShown(true)
    }
    
    override fun onResume() {
        super.onResume()

        // TODO: replace these with channels. For now just wire the logic together
        onAddressLoaded(loadAddress())
//        converter.scanBlocks()
    }

    private fun onAddressLoaded(address: String) {
        Log.e("TWIG", "onAddressLoaded:  $address")
        qrecycler.load(address)
            .withQuietZoneSize(3)
            .withCorrectionLevel(QRecycler.CorrectionLevel.MEDIUM)
            .into(receive_qr_code)

        address.chunked(address.length/8).forEachIndexed { i, part ->
            setAddressPart(i, part)
        }
    }

    private fun setAddressPart(index: Int, addressPart: String) {
        val thinSpace = "\u2005" // 0.25 em space
        val textSpan = SpannableString("${index + 1}$thinSpace$addressPart")

        textSpan.setSpan(AddressPartNumberSpan(), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        addressParts[index].text = textSpan
    }

    // TODO: replace with tiered load. First check memory reference (textview contents?) then check DB, then load from JNI and write to DB
    private fun loadAddress(): String {
        return synchronizer.address
    }

}

@Module
abstract class ReceiveFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeReceiveFragment(): ReceiveFragment
}
