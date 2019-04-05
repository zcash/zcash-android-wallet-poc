package cash.z.android.wallet.sample

import android.provider.Settings
import cash.z.wallet.sdk.data.SampleSeedProvider
import java.math.BigDecimal
import java.math.MathContext
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty

interface WalletConfig {
    val displayName: String
    val seedName: String
    val seedProvider: ReadOnlyProperty<kotlin.Any?, kotlin.ByteArray>
    val spendingKeyProvider: ReadWriteProperty<Any?, String>
    val cacheDbName: String
    val dataDbName: String
    val defaultSendAddress: String

    companion object {
        fun create(name: String): WalletConfig {
            return object : WalletConfig {
                override val displayName = name
                override val seedName = "test.reference.${name}_${Settings.Secure.ANDROID_ID}".sanitizeName()
                override val seedProvider = SampleSeedProvider(seedName)
                override val spendingKeyProvider = SampleSpendingKeySharedPref(seedName)
                override val cacheDbName = "test_cache_${name.sanitizeName()}.db"
                override val dataDbName = "test_data_${name.sanitizeName()}.db"
                override val defaultSendAddress = BobWallet.defaultSendAddress // send to Alice by default, in other words, behave like Bob, which is the default wallet
            }
        }
    }
}

internal inline fun String.sanitizeName(): String {
    return this.toLowerCase().filter {
        it in 'a'..'z' || it in '0'..'9'
    }
}

object AliceWallet : WalletConfig {
    override val displayName = "Alice"
    override val seedName = "test.reference.$displayName".sanitizeName()
    override val seedProvider = SampleSeedProvider(seedName)
    override val spendingKeyProvider = SampleSpendingKeySharedPref(seedName)
    override val cacheDbName = "test_cache_${displayName.sanitizeName()}.db"
    override val dataDbName = "test_data_${displayName.sanitizeName()}.db"
    override val defaultSendAddress =
        "ztestsapling1wrjqt8et9elq7p0ejlgfpt4j9m7r7d4qlt7cke7ppp7dwrpev3yln30c37mrnzzekceajk66h0n" // bob's address
}

object BobWallet : WalletConfig {
    override val displayName = "Bob"
    override val seedName = "test.reference.$displayName".sanitizeName()
    override val seedProvider = SampleSeedProvider(seedName)
    override val spendingKeyProvider = SampleSpendingKeySharedPref(seedName)
    override val cacheDbName = "test_cache_${displayName.sanitizeName()}.db"
    override val dataDbName = "test_data_${displayName.sanitizeName()}.db"
    override val defaultSendAddress =
        "ztestsapling12pxv67r0kdw58q8tcn8kxhfy9n4vgaa7q8vp0dg24aueuz2mpgv2x7mw95yetcc37efc6q3hewn" // alice's address
}

object CarolWallet : WalletConfig {
    override val displayName = "Carol"
    override val seedName = "test.reference.$displayName".sanitizeName()
    override val seedProvider = SampleSeedProvider(seedName)
    override val spendingKeyProvider = SampleSpendingKeySharedPref(seedName)
    override val cacheDbName = "test_cache_${displayName.sanitizeName()}.db"
    override val dataDbName = "test_data_${displayName.sanitizeName()}.db"
    override val defaultSendAddress =
        "ztestsapling1y480yqw6h7lwmvw9wsn3h2xxg0np93cv8nq0j3m6g8edc79faevq5adrtzyxgsmu9jfc2hdf6al" // dave's address
}

object DaveWallet : WalletConfig {
    override val displayName = "Dave"
    override val seedName = "test.reference.$displayName".sanitizeName()
    override val seedProvider = SampleSeedProvider(seedName)
    override val spendingKeyProvider = SampleSpendingKeySharedPref(seedName)
    override val cacheDbName = "test_cache_${displayName.sanitizeName()}.db"
    override val dataDbName = "test_data_${displayName.sanitizeName()}.db"
    override val defaultSendAddress =
        "ztestsapling1efxqj5256ywqdc3zntfa0hw6yn4f83k2h7fgngwmxr3h3w7zydlencvh30730ez6p8fwg56htgz" // carol's address
}

object MyWallet : WalletConfig {
    override val displayName = "MyWallet"
    override val seedName = "test.reference.$displayName".sanitizeName()
    override val seedProvider = SampleImportedSeedProvider("295761fce7fdc89fa1095259f5be6375c4a36f7a214767d668f9ef6e17aa6314")
    override val spendingKeyProvider = SampleSpendingKeySharedPref(seedName)
    override val cacheDbName = "test_cache_${displayName.sanitizeName()}.db"
    override val dataDbName = "test_data_${displayName.sanitizeName()}.db"
    override val defaultSendAddress =
        "ztestsapling1snmqdnfqnc407pvqw7sld8w5zxx6nd0523kvlj4jf39uvxvh7vn0hs3q38n07806dwwecqwke3t" // dummyseed
}

enum class Servers(val host: String, val displayName: String) {
    LOCALHOST("10.0.0.191", "Localhost"),
    //    WLAN("10.0.0.26"),
    WLAN1("10.0.2.24", "WLAN Conference"),
    WLAN2("192.168.1.235", "WLAN Office"),
    BOLT_TESTNET("ec2-34-228-10-162.compute-1.amazonaws.com", "Bolt Labs Testnet"),
    ZCASH_TESTNET("lightwalletd.z.cash", "Zcash Testnet")
}


// TODO: load most of these properties in later, perhaps from settings
object SampleProperties {

    const val PREFS_WALLET_DISPLAY_NAME = "prefs_wallet_name"
    const val PREFS_SERVER_NAME = "prefs_server_name"

    val COMPACT_BLOCK_SERVER = Servers.ZCASH_TESTNET.host
    const val COMPACT_BLOCK_PORT = 9067
    val wallet = DaveWallet
    // TODO: placeholder until we have a network service for this
    val USD_PER_ZEC = BigDecimal("52.86", MathContext.DECIMAL128)
}