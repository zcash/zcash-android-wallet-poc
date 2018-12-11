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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cash.z.android.wallet.R
import cash.z.android.wallet.extention.toAppColor
import cash.z.android.wallet.extention.toAppString
import cash.z.android.wallet.extention.tryIgnore
import cash.z.android.wallet.ui.activity.MainActivity
import cash.z.android.wallet.ui.adapter.TransactionAdapter
import cash.z.android.wallet.ui.util.TopAlignedSpan
import cash.z.android.wallet.vo.WalletTransaction
import cash.z.android.wallet.vo.WalletTransactionStatus
import com.leinardi.android.speeddial.SpeedDialActionItem
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.include_home_content.*
import kotlinx.android.synthetic.main.include_home_header.*
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
            mainActivity.setSupportActionBar(home_toolbar)
            mainActivity.setupNavigation()
            mainActivity.supportActionBar?.setTitle(R.string.destination_title_home)
        }
        headerFullViews = arrayOf(text_balance_usd, text_balance_includes_info, text_balance_zec, image_zec_symbol_balance_shadow, image_zec_symbol_balance)
        headerEmptyViews = arrayOf(text_balance_zec_info, text_balance_zec_empty, image_zec_symbol_balance_shadow_empty, image_zec_symbol_balance_empty)

        // TODO: remove this test behavior
        image_logo.setOnClickListener {
            toggle(!empty)
        }
    }

    override fun onResume() {
        super.onResume()
        view!!.postDelayed( {toggle(false)}, delay *  2L)
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

    fun setUsdValue(value: Double) {
        val valueString = String.format("$ %,.2f",value)
        val hairSpace = "\u200A"
//        val adjustedValue = "$$hairSpace$valueString"
        val textSpan = SpannableString(valueString)
        textSpan.setSpan(TopAlignedSpan(), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textSpan.setSpan(TopAlignedSpan(), valueString.length - 3, valueString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_balance_usd.text = textSpan
    }

    fun setZecValue(value: Double) {
        text_balance_zec.text = if(value == 0.0) "0" else String.format("%.3f",value)
//        // bugfix: there is a bug in motionlayout that causes text to flicker as it is resized because the last character doesn't fit. Padding both sides with a thin space works around this bug.
//        val hairSpace = "\u200A"
//        val adjustedValue = "$hairSpace$valueString$hairSpace"
//        text_balance_zec.text = adjustedValue
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



// ---------------------------------------------------------------------------------------------------------------------
// TODO: Delete these test functions
// ---------------------------------------------------------------------------------------------------------------------

    var empty = false
    val delay = 20L
    lateinit var headerEmptyViews: Array<View>
    lateinit var headerFullViews: Array<View>

    fun shrink(): Double {
        return text_balance_zec.text.toString().trim().toDouble() - Random.nextDouble(5.0)
    }
    fun grow(): Double {
        return text_balance_zec.text.toString().trim().toDouble() + Random.nextDouble(5.0)
    }
    fun reduceValue() {
        shrink().let {
            if(it < 0) { setZecValue(0.0); toggleViews(empty); forceRedraw() }
            else view?.postDelayed({
                setZecValue(it)
                setUsdValue(it*75.0)
                reduceValue()
            }, delay)
        }
    }
    fun increaseValue(target: Double) {
        grow().let {
            if(it > target) { setZecValue(target); setUsdValue(target*75.0); toggleViews(empty) }
            else view?.postDelayed({
                setZecValue(it)
                setUsdValue(it*75.0)
                increaseValue(target)
                if (headerFullViews[0].parent == null || headerEmptyViews[0].parent != null) toggleViews(false)
                forceRedraw()
            }, delay)
        }
    }
    fun forceRedraw() {
        view?.postDelayed({
            container_home_header.progress = container_home_header.progress - 0.1f
        }, delay * 2)
    }
    internal fun toggle(isEmpty: Boolean) {
        toggleValues(isEmpty)
    }
    internal fun toggleViews(isEmpty: Boolean) {
        if(isEmpty) {
            view?.postDelayed({
                group_empty_view_items.visibility = View.VISIBLE
                group_full_view_items.visibility = View.GONE
                headerFullViews.forEach { container_home_header.removeView(it) }
                headerEmptyViews.forEach {
                    tryIgnore {
                        container_home_header.addView(it)
                    }
                }
            }, delay)
        } else {
            view?.postDelayed({
                group_empty_view_items.visibility = View.GONE
                group_full_view_items.visibility = View.VISIBLE
                headerEmptyViews.forEach { container_home_header.removeView(it) }
                headerFullViews.forEach {
                    tryIgnore {
                        container_home_header.addView(it)
                    }
                }
            }, delay)
        }
    }

    internal fun toggleValues(isEmpty: Boolean) {
        empty = isEmpty
        if(empty) {
            reduceValue()
        } else {
            increaseValue(Random.nextDouble(20.0, 100.0))
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
    val amount = BigDecimal(Random.nextDouble(0.1, 15.0) * arrayOf(-1, 1).random())
    val status = if(amount > BigDecimal.ZERO) WalletTransactionStatus.SENT else WalletTransactionStatus.RECEIVED
    return WalletTransaction(
        status,
        Random.nextLong(before, now),
        amount
    )
}

