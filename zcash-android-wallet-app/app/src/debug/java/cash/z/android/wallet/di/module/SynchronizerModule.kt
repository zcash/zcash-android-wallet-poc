package cash.z.android.wallet.di.module

import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.sample.SampleProperties
import cash.z.android.wallet.sample.SampleProperties.COMPACT_BLOCK_PORT
import cash.z.android.wallet.sample.SampleProperties.COMPACT_BLOCK_SERVER
import cash.z.wallet.sdk.data.*
import cash.z.wallet.sdk.jni.JniConverter
import cash.z.wallet.sdk.secure.Wallet
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
    fun provideTwig(): Twig = TroubleshootingTwig() // troubleshoot on debug, silent on release

    @JvmStatic
    @Provides
    @Singleton
    fun provideDownloader(twigger: Twig): CompactBlockStream {
        return CompactBlockStream(COMPACT_BLOCK_SERVER, COMPACT_BLOCK_PORT, twigger)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideProcessor(application: ZcashWalletApplication, converter: JniConverter, twigger: Twig): CompactBlockProcessor {
        return CompactBlockProcessor(application, converter, SampleProperties.wallet.cacheDbName, SampleProperties.wallet.dataDbName, logger = twigger)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideRepository(application: ZcashWalletApplication, converter: JniConverter, twigger: Twig): TransactionRepository {
        return PollingTransactionRepository(application, SampleProperties.wallet.dataDbName, 10_000L, converter, twigger)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideWallet(application: ZcashWalletApplication, converter: JniConverter): Wallet {
        return Wallet(converter, application.getDatabasePath(SampleProperties.wallet.dataDbName).absolutePath, "${application.cacheDir.absolutePath}/params", seedProvider = SampleProperties.wallet.seedProvider, spendingKeyProvider = SampleProperties.wallet.spendingKeyProvider)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideManager(wallet: Wallet, repository: TransactionRepository, downloader: CompactBlockStream, twigger: Twig): ActiveTransactionManager {
        return ActiveTransactionManager(repository, downloader.connection, wallet, twigger)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideJniConverter(): JniConverter {
        return JniConverter().also {
            if (BuildConfig.DEBUG) it.initLogs()
        }
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideSynchronizer(
        downloader: CompactBlockStream,
        processor: CompactBlockProcessor,
        repository: TransactionRepository,
        manager: ActiveTransactionManager,
        wallet: Wallet
    ): Synchronizer {
        return SdkSynchronizer(downloader, processor, repository, manager, wallet, blockPollFrequency = 500_000L)
    }

}
