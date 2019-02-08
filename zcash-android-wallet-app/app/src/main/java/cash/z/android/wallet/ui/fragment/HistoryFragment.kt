package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import cash.z.android.wallet.R
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import cash.z.android.wallet.ui.presenter.HistoryPresenter
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.launch
import cash.z.android.wallet.databinding.FragmentHistoryBinding
import cash.z.wallet.sdk.dao.WalletTransaction


class HistoryFragment : BaseFragment(), HistoryPresenter.HistoryView {


    override val titleResId: Int get() = R.string.destination_title_history
    lateinit var historyPresenter: HistoryPresenter
    lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil
            .inflate<FragmentHistoryBinding>(inflater, R.layout.fragment_history, container, false)
            .also { binding = it }
            .root
    }

    override fun onResume() {
        super.onResume()
        launch {
            historyPresenter.start()
        }
    }

    override fun onPause() {
        super.onPause()
        historyPresenter.stop()
    }

    override fun setTransactions(transactions: List<WalletTransaction>) {
    }
}

@Module
abstract class HistoryFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeHistoryFragment(): HistoryFragment
}