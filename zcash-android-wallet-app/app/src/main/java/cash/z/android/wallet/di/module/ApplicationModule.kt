package cash.z.android.wallet.di.module

import cash.z.android.qrecycler.QRecycler
import cash.z.android.wallet.ui.fragment.HomeFragment
import cash.z.android.wallet.ui.presenter.HomePresenter
import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.jni.JniConverter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Module that contributes all the objects with application scope. Anything that should live globally belongs here.
 */
@Module
internal object ApplicationModule {
    @JvmStatic
    @Provides
    fun provideSanity(): SanityCheck = SanityCheck(true)

    @JvmStatic
    @Provides
    fun provideQRecycler(): QRecycler = QRecycler()

}