package cash.z.android.wallet.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.core.view.doOnLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.R
import cash.z.android.wallet.ZcashWalletApplication
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import cash.z.wallet.sdk.data.Synchronizer
import kotlinx.coroutines.GlobalScope

class MainActivity : DaggerAppCompatActivity() {

    // used to  manage the drawer and drawerToggle interactions
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navController: NavController
    lateinit var synchronizer: Synchronizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        synchronizer = Synchronizer(ZcashWalletApplication.instance, GlobalScope).also {
//            it.start()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        synchronizer.stop()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
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

    fun setupNavigation() {
        // create and setup the navController and appbarConfiguration
        navController = Navigation.findNavController(this, R.id.nav_host_fragment).also { n ->
            appBarConfiguration = AppBarConfiguration(n.graph, drawer_layout).also { a ->
                nav_view.setupWithNavController(n)
                setupActionBarWithNavController(n, a)
            }
        }

        // remove icon tint so that our colored nav icons show through
        nav_view.itemIconTintList = null

        nav_view.doOnLayout {
            text_nav_header_subtitle.text = "Version ${BuildConfig.VERSION_NAME}"
        }
    }

    companion object {
        // TODO: placeholder until we have a network service for this
        const val USD_PER_ZEC = 56.38
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