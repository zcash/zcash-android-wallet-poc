package cash.z.android.wallet.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cash.z.android.wallet.BuildConfig
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentAboutBinding
import dagger.Module
import dagger.android.ContributesAndroidInjector


class AboutFragment : BaseFragment() {
    lateinit var binding: cash.z.android.wallet.databinding.FragmentAboutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil
            .inflate<FragmentAboutBinding>(inflater, R.layout.fragment_about, container, false)
            .also { binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textAboutVersionValue.text = BuildConfig.VERSION_NAME
        binding.textAboutLicensingValue.setOnClickListener {
            openUrl("https://z.cash/trademark-policy/")
        }
        binding.textAboutWhatsNewValue.setOnClickListener {
            openUrl("https://github.com/gmale/zcash-android-wallet-poc/blob/feature/scan-blocks-integration/CHANGELOG.md")
        }
        binding.textAboutZcashBlogValue.setOnClickListener {
            openUrl("https://z.cash/blog/")
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainActivity.setToolbarShown(true)
    }


}
@Module
abstract class AboutFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeAboutFragment(): AboutFragment
}