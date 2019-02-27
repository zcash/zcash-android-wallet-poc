package cash.z.android.wallet.di.module

import android.content.SharedPreferences
import android.preference.PreferenceManager
import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.sample.*
import cash.z.android.wallet.sample.SampleProperties.COMPACT_BLOCK_PORT
import cash.z.android.wallet.sample.SampleProperties.COMPACT_BLOCK_SERVER
import cash.z.android.wallet.sample.SampleProperties.PREFS_SERVER_NAME
import cash.z.android.wallet.sample.SampleProperties.PREFS_WALLET_DISPLAY_NAME
import cash.z.wallet.sdk.data.*
import cash.z.wallet.sdk.jni.JniConverter
import cash.z.wallet.sdk.secure.Wallet
import dagger.Module
import dagger.Provides
import javax.inject.Named
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
    fun providePrefs(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ZcashWalletApplication.instance)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideWalletConfig(prefs: SharedPreferences): WalletConfig {
        val walletName = prefs.getString(PREFS_WALLET_DISPLAY_NAME, null)
        twig("FOUND WALLET DISPLAY NAME : $walletName")
        return when(walletName) {
            AliceWallet.displayName -> AliceWallet
            BobWallet.displayName, null -> BobWallet // Default wallet
            CarolWallet.displayName -> CarolWallet
            DaveWallet.displayName -> DaveWallet
            else -> WalletConfig.create(walletName)
        }
    }

    @JvmStatic
    @Provides
    @Singleton
    @Named(PREFS_SERVER_NAME)
    fun provideServer(prefs: SharedPreferences): String {
        val serverName = prefs.getString(PREFS_SERVER_NAME, null)
        // in theory, the actual stored value itself could be null so provide the default this way to be safe
        val server = Servers.values().firstOrNull { it.displayName == serverName }?.host ?: COMPACT_BLOCK_SERVER //TODO: validate that this is a hostname or IP. For now use default, instead
        twig("FOUND SERVER DISPLAY NAME : $serverName ($server)")
        return server
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideTwig(): Twig = TroubleshootingTwig() // troubleshoot on debug, silent on release

    @JvmStatic
    @Provides
    @Singleton
    fun provideDownloader(@Named(PREFS_SERVER_NAME) server: String, twigger: Twig): CompactBlockStream {
        return CompactBlockStream(server, COMPACT_BLOCK_PORT, twigger)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideProcessor(application: ZcashWalletApplication, converter: JniConverter, walletConfig: WalletConfig, twigger: Twig): CompactBlockProcessor {
        return CompactBlockProcessor(application, converter, walletConfig.cacheDbName, walletConfig.dataDbName, logger = twigger)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideRepository(application: ZcashWalletApplication, walletConfig: WalletConfig, converter: JniConverter): TransactionRepository {
        return PollingTransactionRepository(application, walletConfig.dataDbName, 10_000L)
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideWallet(application: ZcashWalletApplication, walletConfig: WalletConfig, converter: JniConverter): Wallet {
        return Wallet(
            context = application,
            converter = converter,
            dataDbPath = application.getDatabasePath(walletConfig.dataDbName).absolutePath,
            paramDestinationDir =  "${application.cacheDir.absolutePath}/params",
            seedProvider = walletConfig.seedProvider,
            spendingKeyProvider = walletConfig.spendingKeyProvider
        )
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
        return SdkSynchronizer(
            downloader,
            processor,
            repository,
            manager,
            wallet,
            batchSize = 100,
            blockPollFrequency = 50_000L
        )
    }

}
