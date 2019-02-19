package cash.z.android.wallet.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cash.z.android.wallet.R
import cash.z.android.wallet.databinding.FragmentHistoryBinding
import cash.z.android.wallet.ui.adapter.TransactionAdapter
import cash.z.android.wallet.ui.presenter.HistoryPresenter
import cash.z.android.wallet.ui.util.AlternatingRowColorDecoration
import cash.z.wallet.sdk.dao.WalletTransaction
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.launch


class HistoryFragment : BaseFragment(), HistoryPresenter.HistoryView {

    lateinit var historyPresenter: HistoryPresenter
    lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil
            .inflate<FragmentHistoryBinding>(inflater, R.layout.fragment_history, container, false)
            .also { binding = it }
            .root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        historyPresenter = HistoryPresenter(this, mainActivity.synchronizer)
        binding.recyclerTransactionsHistory.apply {
            layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            adapter = TransactionAdapter()
            addItemDecoration(AlternatingRowColorDecoration())
        }
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
        (binding.recyclerTransactionsHistory.adapter as TransactionAdapter).submitList(transactions)
     }
}

@Module
abstract class HistoryFragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeHistoryFragment(): HistoryFragment
}