package cash.z.android.wallet.di.module

import cash.z.android.qrecycler.QRecycler
import cash.z.wallet.sdk.jni.JniConverter
import dagger.Module
import dagger.Provides

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

    @JvmStatic
    @Provides
    fun provideJniConverter(): JniConverter = JniConverter()
}