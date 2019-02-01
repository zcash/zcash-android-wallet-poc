package cash.z.android.wallet.ui.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector


class AboutFragment : PlaceholderFragment()

@Module
abstract class AboutFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeAboutFragment(): AboutFragment
}