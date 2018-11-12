package cash.z.android.wallet.di.component

import cash.z.android.wallet.ui.activity.MainActivityModule
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.di.module.ApplicationModule
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
        MainActivityModule::class,
        AndroidSupportInjectionModule::class,
        ApplicationModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<ZcashWalletApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<ZcashWalletApplication>()
}