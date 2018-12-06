package cash.z.android.wallet

import android.content.res.Resources
import cash.z.android.wallet.di.component.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class ZcashWalletApplication : DaggerApplication() {

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    /**
     * Implement the HasActivityInjector behavior so that dagger knows which [AndroidInjector] to use.
     */
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.builder().create(this)
    }

    companion object {
        lateinit var instance: ZcashWalletApplication
    }
}