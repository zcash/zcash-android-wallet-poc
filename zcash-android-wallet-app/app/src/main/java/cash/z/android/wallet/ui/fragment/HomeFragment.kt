package cash.z.android.wallet.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnLayout
import cash.z.android.wallet.R
import cash.z.android.wallet.di.module.SanityCheck
import cash.z.android.wallet.ui.activity.MainActivity
import cash.z.wallet.sdk.jni.JniConverter
import com.leinardi.android.speeddial.SpeedDialActionItem
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

/**
 * Fragment representing the home screen of the app. This is the screen most often seen by the user when launching the
 * application.
 */
class HomeFragment : BaseFragment() {
    // TODO: remove this test object. it is currently just used to exercise the rust code
    var converter: JniConverter = JniConverter()

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

        //  TODO remove this test code
        val seed = byteArrayOf(0x77, 0x78, 0x79)
        val result = converter.getAddress("dummyseed".toByteArray())
        text_wallet_message.text = "Your address:\n$result"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initFab(activity!!)
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
            speedDial.addActionItem(it.createItem(activity))
        }

        speedDial.setOnActionSelectedListener { item ->
            HomeFab.fromId(item.id)?.destination?.apply { nav.navigate(this) }
            false
        }
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

        fun createItem(activity: Activity): SpeedDialActionItem =
            SpeedDialActionItem.Builder(id, icon)
                .setFabBackgroundColor(ResourcesCompat.getColor(activity.resources, bgColor, activity.theme))
                .setFabImageTintColor(ResourcesCompat.getColor(activity.resources, R.color.zcashWhite, activity.theme))
                .setLabel(activity.getString(label))
                .setLabelClickable(true)
                .create()

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