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

    const val MOCK_LOAD_DURATION = 3_000L
//    const val MOCK_LOAD_DURATION = 30_000L
    const val MOCK_TX_INTERVAL = 20_000L
    const val MOCK_ACTIVE_TX_STATE_CHANGE_INTERVAL = 5_000L


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
            transactionInterval = MOCK_TX_INTERVAL,
            initialLoadDuration = MOCK_LOAD_DURATION,
            activeTransactionUpdateFrequency = MOCK_ACTIVE_TX_STATE_CHANGE_INTERVAL,
            isFirstRun = true
        )
    }
}
