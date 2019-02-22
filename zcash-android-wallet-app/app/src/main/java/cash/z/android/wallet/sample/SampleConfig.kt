package cash.z.android.wallet.sample

import cash.z.wallet.sdk.data.SampleSeedProvider
import java.math.BigDecimal
import java.math.MathContext

object AliceWallet {
    const val name = "test.reference.alice"
    val seedProvider = SampleSeedProvider(name)
    val spendingKeyProvider = SampleSpendingKeySharedPref(name)
    const val cacheDbName = "testalice_cache9.db"
    const val dataDbName = "testalice_data10.db"
    const val defaultSendAddress = "ztestsapling1wcp9fu5d3q945nwwyqxtf0dtn6pv22hmjxa39z0034ap734mvxkqz8kug4r2u2df2keekcne322" // bob's address
}

object BobWallet {
    const val name = "test.reference.bob"
    val seedProvider =
        SampleSeedProvider(name)
    val spendingKeyProvider = SampleSpendingKeySharedPref(name)
    const val cacheDbName = "testbob_cache9.db"
    const val dataDbName = "testbob_data.db"
    const val defaultSendAddress = "ztestsapling1yv696xtjn3jykdej2pqx0999eydvvyfphnw97ddk2h5luyedpqzud3r87aq0d7qna3jzjqqdcvw" // alice's address
}

object MyWallet {
    const val name = "mine"
    val seedProvider =
        SampleImportedSeedProvider("295761fce7fdc89fa1095259f5be6375c4a36f7a214767d668f9ef6e17aa6314")
    val spendingKeyProvider = SampleSpendingKeySharedPref(name)
    const val cacheDbName = "wallet_cache1202.db"
    const val dataDbName = "wallet_data1202.db"
    const val defaultSendAddress = "ztestsapling1snmqdnfqnc407pvqw7sld8w5zxx6nd0523kvlj4jf39uvxvh7vn0hs3q38n07806dwwecqwke3t" // dummyseed
}

enum class Servers(val host: String) {
    EMULATOR("10.0.2.2"),
    WLAN("10.0.0.26"),
    BOLT_TESTNET("ec2-34-228-10-162.compute-1.amazonaws.com"),
    ZCASH_TESTNET("lightwalletd.z.cash")
}


// TODO: load most of these properties in later, perhaps from settings
object SampleProperties {
    val COMPACT_BLOCK_SERVER = Servers.ZCASH_TESTNET.host
    const val COMPACT_BLOCK_PORT = 9067
    val wallet = AliceWallet
    // TODO: placeholder until we have a network service for this
    val USD_PER_ZEC = BigDecimal("49.07", MathContext.DECIMAL128)
}