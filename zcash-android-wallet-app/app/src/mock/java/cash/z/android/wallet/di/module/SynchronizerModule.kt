package cash.z.android.wallet.di.module

import cash.z.android.qrecycler.QScanner
import cash.z.android.wallet.sample.SampleQrScanner
import cash.z.wallet.sdk.data.MockSynchronizer
import cash.z.wallet.sdk.data.Synchronizer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Module that contributes all the objects necessary for the synchronizer, which is basically everything that has
 * application scope.
 */
@Module
internal object SynchronizerModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideQRScanner(): QScanner  {
        // TODO: make an MLKit scanner
        return SampleQrScanner()
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideSynchronizer(): Synchronizer {
        return MockSynchronizer(
            transactionInterval = 60_000L,
            activeTransactionUpdateFrequency = 18_000L,
            isFirstRun = true
        )
    }
}
