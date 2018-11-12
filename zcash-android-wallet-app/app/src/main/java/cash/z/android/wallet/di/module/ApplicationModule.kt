package cash.z.android.wallet.di.module

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Module that contributes all the objects with application scope. Anything that should live globally belongs here.
 */
@Module
class ApplicationModule {
    @Singleton
    @Provides
    fun provideSanity(): SanityCheck = SanityCheck(true)
}