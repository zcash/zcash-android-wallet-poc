package cash.z.android.wallet.di.component

import cash.z.android.wallet.ui.activity.MainActivityModule
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.di.module.ApplicationModule
import cash.z.android.wallet.di.module.SynchronizerModule
import cash.z.android.wallet.ui.fragment.*
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * The application's main component, defining the roots of the object graph for all dependencies that live within the
 * ApplicationScope.
 */
@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        SynchronizerModule::class,
        MainActivityModule::class,

        // Injected Fragments
        AboutFragmentModule::class,
        HistoryFragmentModule::class,
        HomeFragmentModule::class,
        WelcomeFragmentModule::class,
        ReceiveFragmentModule::class,
        RequestFragmentModule::class,
        SendFragmentModule::class,
        ScanFragmentModule::class,
        SettingsFragmentModule::class,
        WelcomeFragmentModule::class,
        FirstrunFragmentModule::class,
        SyncFragmentModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<ZcashWalletApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<ZcashWalletApplication>()
}