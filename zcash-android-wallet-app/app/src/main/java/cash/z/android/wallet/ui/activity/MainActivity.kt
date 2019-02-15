package cash.z.android.wallet.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.view.GravityCompat
import androidx.core.view.doOnLayout
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
import cash.z.android.wallet.sample.SampleProperties.DEV_MODE
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import cash.z.wallet.sdk.data.Synchronizer
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main_first_run.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var synchronizer: Synchronizer

    // used to  manage the drawer and drawerToggle interactions
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(!DEV_MODE)synchronizer.start(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!DEV_MODE)synchronizer.stop()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
//        if(DEV_MODE) navController.navigate(R.id.nav_send_fragment)
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
        drawer_layout.setDrawerLockMode(if (isLocked) DrawerLayout.LOCK_MODE_LOCKED_CLOSED else DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    fun setupNavigation() {
        // create and setup the navController and appbarConfiguration
        navController = Navigation.findNavController(this, R.id.nav_host_fragment).also { n ->
            appBarConfiguration = AppBarConfiguration(n.graph, drawer_layout).also { a ->
                nav_view.setupWithNavController(n)
                setupActionBarWithNavController(n, a)
            }
        }
        navController.addOnNavigatedListener { _, _ ->
            // hide the keyboard anytime we change destinations
            getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(nav_view.windowToken, HIDE_NOT_ALWAYS)
        }

        // remove icon tint so that our colored nav icons show through
        nav_view.itemIconTintList = null

        nav_view.doOnLayout {
            text_nav_header_subtitle.text = "Version ${BuildConfig.VERSION_NAME}"
        }
    }

    companion object {
        init {
            // Enable vector drawable magic
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity
}