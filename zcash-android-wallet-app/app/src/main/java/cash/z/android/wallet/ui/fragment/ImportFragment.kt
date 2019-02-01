package cash.z.android.wallet.ui.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector


class ImportFragment : PlaceholderFragment()

@Module
abstract class ImportFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeImportFragment(): ImportFragment
}