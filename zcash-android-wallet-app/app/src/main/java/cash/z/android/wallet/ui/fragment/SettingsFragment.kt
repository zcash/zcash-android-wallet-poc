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
        mainActivity.setToolbarShown(true)
    }

}

    @Module
abstract class SettingsFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment
}