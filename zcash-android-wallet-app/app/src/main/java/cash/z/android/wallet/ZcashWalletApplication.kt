package cash.z.android.wallet

import cash.z.android.wallet.di.component.DaggerApplicationComponent
import com.facebook.stetho.Stetho
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class ZcashWalletApplication : DaggerApplication() {

    override fun onCreate() {
        instance = this
        super.onCreate()
        Stetho.initializeWithDefaults(this)
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