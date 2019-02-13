package cash.z.android.wallet.sample

import android.preference.PreferenceManager
import android.util.Log
import cash.z.android.wallet.ZcashWalletApplication
import okio.ByteString
import java.nio.charset.Charset
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import android.R.id.edit
import android.content.Context
import android.content.SharedPreferences
import java.lang.IllegalStateException

@Deprecated(message = InsecureWarning.message)
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

@Deprecated(message = InsecureWarning.message)
class SampleSpendingKeySharedPref(private val fileName: String) : ReadWriteProperty<Any?, String> {

    private fun getPrefs() = ZcashWalletApplication.instance
        .getSharedPreferences(fileName, Context.MODE_PRIVATE)

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        val preferences = getPrefs()

            PreferenceManager.getDefaultSharedPreferences(ZcashWalletApplication.instance)
        return preferences.getString("spending", null)
                ?: throw IllegalStateException(
                    "Spending key was not there when we needed it! Make sure it was saved " +
                            "during the first run of the app, when accounts were created!"
                )
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        Log.e("TWIG", "Spending key is being stored")
        val preferences = getPrefs()
        val editor = preferences.edit()
        editor.putString("spending", value)
        editor.apply()
    }

}

internal object InsecureWarning {
    const val message = "Do not use this because it is insecure and only intended for test code and samples. " +
            "Instead, use the Android Keystore system or a 3rd party library that leverages it."
}