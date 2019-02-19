package cash.z.android.wallet.ui.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.core.view.GravityCompat
import androidx.core.view.doOnLayout
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.R
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.databinding.ActivityMainBinding
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import cash.z.wallet.sdk.data.Synchronizer
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : BaseActivity() {

    @Inject
    lateinit var synchronizer: Synchronizer

    lateinit var binding: ActivityMainBinding
    lateinit var loadMessages: List<String>

    // used to  manage the drawer and drawerToggle interactions
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navController: NavController
    private val multiStartNavigationUi = MultiStartNavigationUI(listOf(
        R.id.nav_home_fragment,
        R.id.nav_welcome_fragment
    ))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        initAppBar()
        loadMessages = generateFunLoadMessages().shuffled()
        synchronizer.start(this)
    }

    private fun initAppBar() {
        setSupportActionBar(findViewById(R.id.main_toolbar))
//        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setupNavigation()
    }

    override fun onDestroy() {
        super.onDestroy()
        synchronizer.stop()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Let the navController override the default behavior when the drawer icon or back arrow are clicked. This
     * automatically takes care of the drawer toggle behavior. Note that without overriding this method, the up/drawer
     * buttons will not function.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setDrawerLocked(isLocked: Boolean) {
        binding.drawerLayout.setDrawerLockMode(if (isLocked) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    fun openDrawer(view: View) {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    fun setToolbarShown(isShown: Boolean) {
        binding.mainAppBar.visibility = if (isShown) View.VISIBLE else View.INVISIBLE
    }

    fun setupNavigation() {
        // create and setup the navController and appbarConfiguration
        navController = Navigation.findNavController(this, R.id.nav_host_fragment).also { n ->
            appBarConfiguration = AppBarConfiguration(n.graph, binding.drawerLayout).also { a ->
                binding.navView.setupWithNavController(n)
                multiStartNavigationUi.setupActionBarWithNavController(this, n, binding.drawerLayout)
            }
        }
        navController.addOnNavigatedListener { _, _ ->
            // hide the keyboard anytime we change destinations
            getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(binding.navView.windowToken, HIDE_NOT_ALWAYS)
        }

        // remove icon tint so that our colored nav icons show through
        binding.navView.itemIconTintList = null

        binding.navView.doOnLayout {
            binding.navView.findViewById<TextView>(R.id.text_nav_header_subtitle).text = "Version ${BuildConfig.VERSION_NAME}"
        }
    }

    fun nextLoadMessage(index: Int = -1): String {
        return if (index < 0) loadMessages.random() else loadMessages[index]
    }

    companion object {
        init {
            // Enable vector drawable magic
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        // TODO: move these lists, once approved
        fun generateSeriousLoadMessages(): List<String> {
            return listOf(
                "Initializing your shielded address",
                "Connecting to testnet",
                "Downloading historical blocks",
                "Synchronizing to current blockchain",
                "Searching for past transactions",
                "Validating your balance"
            )
        }
        fun generateFunLoadMessages(): List<String> {
            return listOf(
                "Reticulating splines",
                "Making the sausage",
                "Drinking the kool-aid",
                "Learning to spell Lamborghini",
                "Asking Zooko, \"when moon?!\"",
                "Pretending to look busy"
            )
        }
    }
}

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity
}


class MultiStartNavigationUI(private val startDestinations: List<Int>) {
    fun setupActionBarWithNavController(activity: AppCompatActivity, navController: NavController,
                                        drawerLayout: DrawerLayout?) {

        navController.addOnNavigatedListener(ActionBarOnNavigatedListener(
            activity, startDestinations, drawerLayout))
    }

    fun navigateUp(drawerLayout: DrawerLayout?, navController: NavController): Boolean {
        if (drawerLayout != null && startDestinations.contains(navController.currentDestination?.id)) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        } else {
            return navController.navigateUp()
        }
    }

    fun onBackPressed(activity: AppCompatActivity,
                      navController: NavController): Boolean {
        if (startDestinations.contains(navController.currentDestination?.id)) {
            ActivityCompat.finishAfterTransition(activity)
            return true
        }

        return false
    }

    private class ActionBarOnNavigatedListener(
        private val mActivity: AppCompatActivity,
        private val startDestinations: List<Int>,
        private val mDrawerLayout: DrawerLayout?
    ) : NavController.OnNavigatedListener {
        private var mArrowDrawable: DrawerArrowDrawable? = null
        private var mAnimator: ValueAnimator? = null

        override fun onNavigated(controller: NavController, destination: NavDestination) {
            val actionBar = mActivity.supportActionBar

            val title = destination.label
            if (!title.isNullOrEmpty()) {
                actionBar?.title = title
            }

            val isStartDestination = startDestinations.contains(destination.id)
            actionBar?.setDisplayHomeAsUpEnabled(this.mDrawerLayout != null || !isStartDestination)
            setActionBarUpIndicator(mDrawerLayout != null && isStartDestination)
        }


        private fun setActionBarUpIndicator(showAsDrawerIndicator: Boolean) {
            val delegate = mActivity.drawerToggleDelegate
            var animate = true
            if (mArrowDrawable == null) {
                mArrowDrawable = DrawerArrowDrawable(delegate!!.actionBarThemedContext)
                delegate.setActionBarUpIndicator(mArrowDrawable, 0)
                animate = false
            }

            mArrowDrawable?.let {
                val endValue = if (showAsDrawerIndicator) 0.0f else 1.0f

                if (animate) {
                    val startValue = it.progress
                    mAnimator?.cancel()

                    @SuppressLint("ObjectAnimatorBinding")
                    mAnimator = ObjectAnimator.ofFloat(it, "progress", startValue, endValue)
                    mAnimator?.start()
                } else {
                    it.progress = endValue
                }
            }

        }
    }
}