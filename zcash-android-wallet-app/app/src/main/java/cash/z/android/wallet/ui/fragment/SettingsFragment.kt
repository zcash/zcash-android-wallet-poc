package cash.z.android.wallet.ui.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector


class SettingsFragment : PlaceholderFragment()

@Module
abstract class SettingsFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment
}