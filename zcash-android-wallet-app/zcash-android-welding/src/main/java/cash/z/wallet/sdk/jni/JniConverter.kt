package cash.z.wallet.sdk.jni

class JniConverter {

    external fun getMagicInt(value: String): Int

    companion object {
        init {
            System.loadLibrary("zcashwalletsdk")
        }
    }

}