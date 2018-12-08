package cash.z.android.wallet.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cash.z.android.wallet.R
import cash.z.android.wallet.extention.toAppColor
import cash.z.android.wallet.extention.toAppString
import cash.z.android.wallet.ui.activity.MainActivity
import cash.z.android.wallet.ui.adapter.TransactionAdapter
import cash.z.android.wallet.ui.util.TopAlignedSpan
import cash.z.android.wallet.vo.WalletTransaction
import cash.z.android.wallet.vo.WalletTransactionStatus
import com.leinardi.android.speeddial.SpeedDialActionItem
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.android.synthetic.main.fragment_home.*
import java.math.BigDecimal
import kotlin.random.Random

/**
 * Fragment representing the home screen of the app. This is the screen most often seen by the user when launching the
 * application.
 */
class HomeFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).let { mainActivity ->
            mainActivity.setSupportActionBar(toolbar)
            mainActivity.setupNavigation()
            mainActivity.supportActionBar?.setTitle(R.string.destination_title_home)
        }
        fullItems = arrayOf(text_balance_usd,text_balance_includes_info,text_transaction_header,recycler_transactions,text_balance_zec,image_zec_symbol_balance,image_zec_symbol_balance_shadow)
        emptyItems = arrayOf(text_balance_zec_info,image_empty_wallet,text_wallet_message,text_wallet_message_collapsed,text_balance_zec_empty,image_zec_symbol_balance_empty,guideline_zec_usd_spacer_empty,image_zec_symbol_balance_shadow_empty)

        image_logo.setOnClickListener {
            toggleViews(!empty)
        }

//        (view as MotionLayout).setShowPaths(true)
        (view as MotionLayout).setTransitionListener (object: MotionLayout.TransitionListener {
            override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {

            }
            override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                toggleViews(empty)
            }
        })

//        image_logo.setOnClickListener {
//            empty = !empty
//
//
//            // empty items
//           .forEach { view ->
//                view.apply {
//                    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
//                }
//            }
//
//            // Full items
//            arrayOf(text_balance_usd,text_balance_includes_info,text_transaction_header,recycler_transactions).forEach { view ->
//                view.apply {
//                    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
//                }
//            }

//            with(group_empty_view_items) {
//                visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
//            }
//            // toggle empty
//            with(group_full_view_items) {
//                visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
//            }
//        }

        // TODO: pull from DB, for now just exercise UI
        setUsdValue("$5,459.32")
    }

    override fun onResume() {
        super.onResume()
        view!!.postDelayed( {toggleViews(true)}, 50)
    }
    var empty = true
    lateinit var emptyItems: Array<View>
    lateinit var fullItems: Array<View>

    internal fun toggleViews(isEmpty: Boolean) {
        empty = isEmpty
        if(empty) {
//            text_balance_zec.text = "0"
            emptyItems.forEach { it.visibility = View.VISIBLE }
            fullItems.forEach { it.visibility= View.GONE }
        } else {
//            text_balance_zec.text = "35.021"
            emptyItems.forEach { it.visibility = View.GONE }
            fullItems.forEach { it.visibility= View.VISIBLE }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initFab(activity!!)

        recycler_transactions.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recycler_transactions.adapter = TransactionAdapter(createDummyTransactions(60))
    }

    /**
     * Initialize the Fab button and all its action items
     *
     * @param activity a helper parameter that forces this method to be called after the activity is created and not null
     */
    private fun initFab(activity: Activity) {
        val speedDial = sd_fab
        val nav = (activity as MainActivity).navController

        HomeFab.values().forEach {
            speedDial.addActionItem(it.createItem())
        }

        speedDial.setOnActionSelectedListener { item ->
            HomeFab.fromId(item.id)?.destination?.apply { nav.navigate(this) }
            false
        }
    }

    private val createItem: HomeFab.() -> SpeedDialActionItem = {
        SpeedDialActionItem.Builder(id, icon)
            .setFabBackgroundColor(bgColor.toAppColor())
            .setFabImageTintColor(R.color.zcashWhite.toAppColor())
            .setLabel(label.toAppString())
            .setLabelClickable(true)
            .create()
    }

    fun setUsdValue(value: String) {
        val textSpan = SpannableString(value)
        textSpan.setSpan(TopAlignedSpan(), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textSpan.setSpan(TopAlignedSpan(), value.length - 3, value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_balance_usd.text = textSpan
    }

    /**
     * Defines the basic properties of each FAB button for use while initializing the FAB
     */
    enum class HomeFab(
        @IdRes val id:Int,
        @DrawableRes val icon:Int,
        @ColorRes val bgColor:Int,
        @StringRes val label:Int,
        @IdRes val destination:Int
    ) {
        /* ordered by when they need to be added to the speed dial (i.e. reverse display order) */
        REQUEST(
            R.id.fab_request,
            R.drawable.ic_receipt_24dp,
            R.color.icon_request,
            R.string.destination_menu_label_request,
            R.id.nav_request_fragment
        ),
        RECEIVE(
            R.id.fab_receive,
            R.drawable.ic_qrcode_24dp,
            R.color.icon_receive,
            R.string.destination_menu_label_receive,
            R.id.nav_receive_fragment
        ),
        SEND(
            R.id.fab_send,
            R.drawable.ic_menu_send,
            R.color.icon_send,
            R.string.destination_menu_label_send,
            R.id.nav_send_fragment
        );

        companion object {
            fun fromId(id: Int): HomeFab? = values().firstOrNull { it.id == id }
        }
    }

}

@Module
abstract class HomeFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeHomeFragment(): HomeFragment
}


//TODO: delete this test code

internal fun createDummyTransactions(size: Int): MutableList<WalletTransaction> {
    val transactions = mutableListOf<WalletTransaction>()
    repeat(size) {
        transactions.add(createDummyTransaction())
    }
    return transactions
}

internal fun createDummyTransaction(): WalletTransaction {
    val now = System.currentTimeMillis()
    val before = now - (4 * DateUtils.WEEK_IN_MILLIS)
    return WalletTransaction(
        WalletTransactionStatus.values().random(),
        Random.nextLong(before, now),
        BigDecimal(Random.nextDouble(0.1, 15.0) * arrayOf(-1, 1).random())
    )
}