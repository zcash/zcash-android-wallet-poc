package cash.z.android.wallet.ui.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector


class HistoryFragment : PlaceholderFragment()

@Module
abstract class HistoryFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeHistoryFragment(): HistoryFragment
}