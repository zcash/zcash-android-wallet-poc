package cash.z.android.wallet.sample

import cash.z.wallet.sdk.data.SampleSeedProvider

object AliceWallet {
    const val name = "test.reference.alice"
    val seedProvider = SampleSeedProvider(name)
    val spendingKeyProvider = SampleSpendingKeySharedPref(name)
    const val cacheDbName = "testalice_cache.db"
    const val dataDbName = "testalice_data.db"
}

object BobWallet {
    const val name = "test.reference.bob"
    val seedProvider =
        SampleSeedProvider(name)
    val spendingKeyProvider = SampleSpendingKeySharedPref(name)
    const val cacheDbName = "testalice_cache.db"
    const val dataDbName = "testalice_data.db"
}

object MyWallet {
    const val name = "mine"
    val seedProvider =
        SampleImportedSeedProvider("295761fce7fdc89fa1095259f5be6375c4a36f7a214767d668f9ef6e17aa6314")
    val spendingKeyProvider = SampleSpendingKeySharedPref(name)
    const val cacheDbName = "wallet_cache1202.db"
    const val dataDbName = "wallet_data1202.db"
}

enum class Servers(val host: String) {
    EMULATOR("10.0.2.2"),
    WLAN("10.0.0.26"),
    BOLT_TESTNET("ec2-34-228-10-162.compute-1.amazonaws.com"),
    ZCASH_TESTNET("lightwalletd.z.cash")
}


// TODO: load most of these properties in later, perhaps from settings
object SampleProperties {
    val COMPACT_BLOCK_SERVER = Servers.EMULATOR.host
    const val COMPACT_BLOCK_PORT = 9067
    val wallet = AliceWallet
}