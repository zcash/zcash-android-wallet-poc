package cash.z.android.wallet.ui.activity

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cash.z.android.wallet.R
import cash.z.android.wallet.di.module.SanityCheck
import com.google.android.material.snackbar.Snackbar
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var sanity: SanityCheck

    // used to  manage the drawer and drawerToggle interactions
    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupNavigation()
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

    private fun setupNavigation() {
        // create and setup the navController and appbarConfiguration
        navController = Navigation.findNavController(this, R.id.nav_host_fragment).also { n ->
            appBarConfiguration = AppBarConfiguration(n.graph, drawer_layout).also { a ->
                nav_view.setupWithNavController(n)
                setupActionBarWithNavController(n, a)
            }
        }

        // remove icon tint so that our colored nav icons show through
        nav_view.itemIconTintList = null

        // counting the fab as navigation-related. So set it up here
        fab.setOnClickListener(::onFabClicked)
        navController.addOnNavigatedListener { _, destination ->
            if (destination.id == R.id.nav_home_fragment) fab.show() else fab.hide()
        }
    }

    private fun onFabClicked(view: View) {
        Snackbar.make(view, if (sanity.stillSane) "Your imaginary ZEC is in flight." else "You've lost your marbles.", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show()
    }
}

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity
}