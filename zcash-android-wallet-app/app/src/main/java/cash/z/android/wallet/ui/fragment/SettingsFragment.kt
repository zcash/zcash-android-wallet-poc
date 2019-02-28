package cash.z.android.wallet.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentSettingsBinding
import cash.z.android.wallet.extention.Toaster
import cash.z.android.wallet.extention.alert
import cash.z.android.wallet.sample.SampleProperties
import dagger.Module
import dagger.android.ContributesAndroidInjector
import javax.inject.Inject


class SettingsFragment : BaseFragment() {

    @Inject
    lateinit var prefs: SharedPreferences
    lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil
            .inflate<FragmentSettingsBinding>(inflater, R.layout.fragment_settings, container, false)
            .also { binding = it }
            .root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity?.setToolbarShown(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonResetApp.setOnClickListener {
            view.context.alert(R.string.settings_alert_reset_app) {
                Toaster.short("Not Yet Implemented!")
                mainActivity?.navController?.navigateUp()
            }
        }
        binding.includeToolbar.toolbarApplyOrClose.findViewById<ImageView>(R.id.image_close).apply {
            setOnClickListener {
                mainActivity?.navController?.navigateUp()
            }
        }
        binding.includeToolbar.toolbarApplyOrClose.findViewById<ImageView>(R.id.image_apply).apply {
            setOnClickListener {
                val userName = binding.spinnerDemoUser.selectedItem.toString()
                val server = binding.spinnerServers.selectedItem.toString()
                view.context.alert("Are you sure you want to apply these changes?\n\nUser: $userName\nServer: $server\n\nTHIS WILL EXIT THE APP!") {
                    onApplySettings(userName, server)
                    // TODO: handle this whole reset thing better. For now, just aggressively kill the app. A better
                    // approach is to create a custom scope for the synchronizer and then just manage that like any
                    // other subcomponent. In that scenario, we would simply navigate up from this fragment at this
                    // point (after installing a new synchronizer subcomponent)
                    view.postDelayed({
                        mainActivity?.finish()
                        Thread.sleep(1000L) // if you're going to cut a corner, lean into it! sleep FTW!
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }, 2000L)
                }
            }
        }


        binding.spinnerServers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                setCustomServerUiShown(false)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = binding.spinnerDemoUser.selectedItem.toString()
                setCustomServerUiShown(item.startsWith("Custom"))
            }
        }

        binding.spinnerDemoUser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                setCustomUserUiShown(false)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = binding.spinnerDemoUser.selectedItem.toString()
                setCustomUserUiShown(item.startsWith("Custom"))
            }
        }
    }

    private fun setCustomServerUiShown(isShown: Boolean) {
        if (isShown) Toaster.short("Custom servers are not yet implemented")
    }

    private fun setCustomUserUiShown(isShown: Boolean) {
        if (isShown) Toaster.short("Custom users are not yet implemented")
    }

    private fun onApplySettings(userName: String, server: String) {
        AlertDialog.Builder(mainActivity!!).setMessage("Changing everything...").show()
        prefs.edit().apply {
            putString(SampleProperties.PREFS_SERVER_NAME, server)
            putString(SampleProperties.PREFS_WALLET_DISPLAY_NAME, userName)
        }.apply()
    }

}

@Module
abstract class SettingsFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment
}