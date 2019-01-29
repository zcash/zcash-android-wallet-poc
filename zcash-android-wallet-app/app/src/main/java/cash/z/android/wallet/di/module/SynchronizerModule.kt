package cash.z.android.wallet.di.module

import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.di.module.Properties.CACHE_DB_NAME
import cash.z.android.wallet.di.module.Properties.COMPACT_BLOCK_PORT
import cash.z.android.wallet.di.module.Properties.COMPACT_BLOCK_SERVER
import cash.z.android.wallet.di.module.Properties.DATA_DB_NAME
import cash.z.android.wallet.di.module.Properties.OUTPUT_PARAMS_PATH
import cash.z.android.wallet.di.module.Properties.SEED_PROVIDER
import cash.z.android.wallet.di.module.Properties.SPEND_PARAMS_PATH
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
    fun provideTwig(): Twig = if (BuildConfig.DEBUG) TroubleshootingTwig() else SilentTwig()

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
        return CompactBlockProcessor(application, converter, CACHE_DB_NAME, DATA_DB_NAME, logger = twigger)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideRepository(application: ZcashWalletApplication, converter: JniConverter, twigger: Twig): TransactionRepository {
        return PollingTransactionRepository(application, DATA_DB_NAME, 10_000L, converter, twigger)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideWallet(application: ZcashWalletApplication, converter: JniConverter): Wallet {
        return Wallet(converter, application.getDatabasePath(DATA_DB_NAME).absolutePath, SPEND_PARAMS_PATH, OUTPUT_PARAMS_PATH, seedProvider = SEED_PROVIDER)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideJniConverter(): JniConverter = JniConverter()

    @JvmStatic
    @Provides
    @Singleton
    fun provideSynchronizer(
        downloader: CompactBlockStream,
        processor: CompactBlockProcessor,
        repository: TransactionRepository,
        wallet: Wallet,
        twigger: Twig
    ): Synchronizer {
        return Synchronizer(downloader, processor, repository, wallet, twigger)
    }

}


// TODO: load this stuff in, later
object Properties {
    const val COMPACT_BLOCK_SERVER = "10.0.2.2"
    const val COMPACT_BLOCK_PORT = 9067
    const val CACHE_DB_NAME = "wallet_cache.db"
    const val DATA_DB_NAME = "wallet_data.db"
    const val SPEND_PARAMS_PATH = "/data/user/0/cash.z.wallet.sdk.test/databases/sapling-spend.params"
    const val OUTPUT_PARAMS_PATH = "/data/user/0/cash.z.wallet.sdk.test/databases/sapling-output.params"
    val SEED_PROVIDER = SampleSeedProvider("dummyseed")
}