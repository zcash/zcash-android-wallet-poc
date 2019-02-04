package cash.z.android.wallet.di.module

import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.di.module.Properties.CACHE_DB_NAME
import cash.z.android.wallet.di.module.Properties.COMPACT_BLOCK_PORT
import cash.z.android.wallet.di.module.Properties.COMPACT_BLOCK_SERVER
import cash.z.android.wallet.di.module.Properties.DATA_DB_NAME
import cash.z.android.wallet.di.module.Properties.SEED_PROVIDER
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
    fun provideWallet(application: ZcashWalletApplication, converter: JniConverter, twigger: Twig): Wallet {
        return Wallet(converter, application.getDatabasePath(DATA_DB_NAME).absolutePath, "${application.cacheDir.absolutePath}/params", seedProvider = SEED_PROVIDER, logger = twigger)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideManager(twigger: Twig): ActiveTransactionManager {
        return ActiveTransactionManager(twigger)
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
        manager: ActiveTransactionManager,
        wallet: Wallet,
        twigger: Twig
    ): Synchronizer {
        return Synchronizer(downloader, processor, repository, manager, wallet, logger = twigger)
    }

}


// TODO: load most of these properties in later, perhaps from settings
object Properties {
    val COMPACT_BLOCK_SERVER = Servers.ZCASH_TESTNET.host
    const val COMPACT_BLOCK_PORT = 9067
    const val CACHE_DB_NAME = "wallet_cache19.db"
    const val DATA_DB_NAME = "wallet_data19.db"
    val SEED_PROVIDER = SampleSeedProvider("dummyseed")
}

enum class Servers(val host: String) {
    EMULATOR("10.0.2.2"),
    WLAN("10.0.0.26"),
    BOLT_TESTNET("ec2-34-228-10-162.compute-1.amazonaws.com"),
    ZCASH_TESTNET("lightwalletd.z.cash")
}
