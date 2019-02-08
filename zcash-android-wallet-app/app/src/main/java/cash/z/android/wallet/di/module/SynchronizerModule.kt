package cash.z.android.wallet.di.module

import android.util.Log
import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.ZcashWalletApplication
import cash.z.android.wallet.di.module.Properties.CACHE_DB_NAME
import cash.z.android.wallet.di.module.Properties.COMPACT_BLOCK_PORT
import cash.z.android.wallet.di.module.Properties.COMPACT_BLOCK_SERVER
import cash.z.android.wallet.di.module.Properties.DATA_DB_NAME
import cash.z.android.wallet.di.module.Properties.SEED_PROVIDER
import cash.z.android.wallet.di.module.Properties.SPENDING_KEY_PROVIDER
import cash.z.wallet.sdk.data.*
import cash.z.wallet.sdk.jni.JniConverter
import cash.z.wallet.sdk.secure.Wallet
import dagger.Module
import dagger.Provides
import okio.ByteString
import java.nio.charset.Charset
import javax.inject.Singleton
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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
        return Wallet(converter, application.getDatabasePath(DATA_DB_NAME).absolutePath, "${application.cacheDir.absolutePath}/params", seedProvider = SEED_PROVIDER, spendingKeyProvider = SPENDING_KEY_PROVIDER, logger = twigger)
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
        wallet: Wallet,
        twigger: Twig
    ): Synchronizer {
        return Synchronizer(downloader, processor, repository, manager, wallet, logger = twigger)
    }

}


// TODO: load most of these properties in later, perhaps from settings
object Properties {
    val COMPACT_BLOCK_SERVER = Servers.EMULATOR.host
    const val COMPACT_BLOCK_PORT = 9067
    const val CACHE_DB_NAME = "wallet_cache421.db"
    const val DATA_DB_NAME = "wallet_data421.db"
    val SEED_PROVIDER = SampleImportedSeedProvider("295761fce7fdc89fa1095259f5be6375c4a36f7a214767d668f9ef6e17aa6314")
    val SPENDING_KEY_PROVIDER = SampleSpendingKeyProvider2("dummyseed")
}

enum class Servers(val host: String) {
    EMULATOR("10.0.2.2"),
    WLAN("10.0.0.26"),
    BOLT_TESTNET("ec2-34-228-10-162.compute-1.amazonaws.com"),
    ZCASH_TESTNET("lightwalletd.z.cash")
}
class SampleImportedSeedProvider(private val seedHex: String) : ReadOnlyProperty<Any?, ByteArray> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): ByteArray {
        val bytes = ByteString.decodeHex(seedHex).toByteArray()
        val stringBytes = String(bytes, Charset.forName("UTF-8"))
        Log.e("TWIG-x", "byteString: $stringBytes")
        return decodeHex(seedHex).also { Log.e("TWIG-x", "$it") }
    }


    fun decodeHex(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            val d1 = decodeHexDigit(hex[i * 2]) shl 4
            val d2 = decodeHexDigit(hex[i * 2 + 1])
            result[i] = (d1 + d2).toByte()
        }
        return result
    }
    private fun decodeHexDigit(c: Char): Int {
        if (c in '0'..'9') return c - '0'
        if (c in 'a'..'f') return c - 'a' + 10
        if (c in 'A'..'F') return c - 'A' + 10
        throw IllegalArgumentException("Unexpected hex digit: $c")
    }
}


class SampleSpendingKeyProvider2(private val seedValue: String) : ReadOnlyProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        // dynamically generating keyes, based on seed is out of scope for this sample
        return "secret-extended-key-test1q0ks5jkcqqqqpqywf2mh5g2aw5smt252mqscphjr8svrqyvgtgss0av3jh37jc05pngstr6qcqu5x64zuk8entc97pfla68jd7g9fyhwv5l8pdey662qy3lr07w9yddpgwdlwt3tjgzhpszatyw90kpn4zs7feu5cudwnxcpts5k0za96xy0wt59nu7hg3ntalck7gwhn0nuyztmf8yceuhp0fn3wmrtr9mk9v6fhg8hwvsxp0thr4cn9r8pc0w3zh45czmnr7e3mrctlzaq7"
//        return "secret-extended-key-test1q0f0urnmqqqqpqxlree5urprcmg9pdgvr2c88qhm862etv65eu84r9zwannpz4g88299xyhv7wf9xkecag653jlwwwyxrymfraqsnz8qfgds70qjammscxxyl7s7p9xz9w906epdpy8ztsjd7ez7phcd5vj7syx68sjskqs8j9lef2uuacghsh8puuvsy9u25pfvcdznta33qe6xh5lrlnhdkgymnpdug4jm6tpf803cad6tqa9c0ewq9l03fqxatevm97jmuv8u0ccxjews5"
    }
}