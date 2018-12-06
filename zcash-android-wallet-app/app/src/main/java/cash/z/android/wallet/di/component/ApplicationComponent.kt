package cash.z.android.wallet.di.component

import cash.z.android.wallet.ui.activity.MainActivityModule
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.di.module.ApplicationModule
import cash.z.android.wallet.ui.fragment.HomeFragmentModule
import cash.z.android.wallet.ui.fragment.ReceiveFragmentModule
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
        MainActivityModule::class,
        HomeFragmentModule::class,
        ReceiveFragmentModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<ZcashWalletApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<ZcashWalletApplication>()
}