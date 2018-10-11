package cash.z.wallet.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cash.z.wallet.sdk.jni.JniConverter
import cash.z.wallet.sdk.proto.WalletData
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var converter: JniConverter = JniConverter()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_send -> {
                message.setText(R.string.title_send)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_receive -> {
                message.setText(R.string.title_recieve)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_invoice -> {
                val homeText = "${getString(R.string.title_invoice)}\n${getString(R.string.sdk_test_message)}\n${checkJni()}"
                message.setText(homeText)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun checkJni(): String {
        try {
            val walletDataArray = WalletData("Kevin", 42, "email@hotmail.com").encode()
            val result = converter.sendComplexData(walletDataArray)
            return when(result) {
                42 -> "Rust lib is connected!"
                else -> "sent(${walletDataArray.size}) JNI call returned unexpected value: $result"
            }
        } catch (t: Throwable) {}
        return "Failure reading from JNI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
