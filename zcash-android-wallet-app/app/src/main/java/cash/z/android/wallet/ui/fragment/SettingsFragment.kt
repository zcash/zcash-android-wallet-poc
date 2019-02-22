package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cash.z.android.wallet.R
import dagger.Module
import dagger.android.ContributesAndroidInjector
import cash.z.android.wallet.databinding.FragmentSettingsBinding
import cash.z.android.wallet.extention.Toaster
import cash.z.android.wallet.extention.alert


class SettingsFragment : BaseFragment() {
    lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil
            .inflate<FragmentSettingsBinding>(inflater, R.layout.fragment_settings, container, false)
            .also { binding = it }
            .root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity?.setToolbarShown(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonResetApp.setOnClickListener {
            view.context.alert(R.string.settings_alert_reset_app) {
                Toaster.short("Not Yet Implemented!")
                mainActivity?.navController?.navigateUp()
            }
        }
    }

}

    @Module
abstract class SettingsFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment
}