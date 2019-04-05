package cash.z.android.wallet.ui.activity

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.view.GravityCompat
import androidx.core.view.doOnLayout
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.ActivityMainBinding
import cash.z.android.wallet.sample.WalletConfig
import cash.z.wallet.sdk.data.Synchronizer
import dagger.Module
import dagger.android.ContributesAndroidInjector
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var synchronizer: Synchronizer

    @Inject
    lateinit var walletConfig: WalletConfig

    lateinit var binding: ActivityMainBinding
    lateinit var loadMessages: List<String>

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initAppBar()
        loadMessages = generateFunLoadMessages().shuffled()
        synchronizer.start(this)
    }

    private fun initAppBar() {
        setSupportActionBar(findViewById(R.id.main_toolbar))
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
                setupActionBarWithNavController(n, binding.drawerLayout)
            }
        }
        navController.addOnDestinationChangedListener { _, _, _ ->
            // hide the keyboard anytime we change destinations
            getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(binding.navView.windowToken, HIDE_NOT_ALWAYS)
        }

        // remove icon tint so that our colored nav icons show through
        binding.navView.itemIconTintList = null

        binding.navView.doOnLayout {
            binding.navView.findViewById<TextView>(R.id.text_nav_header_subtitle).text = "Version ${BuildConfig.VERSION_NAME} (${walletConfig.displayName})"
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
