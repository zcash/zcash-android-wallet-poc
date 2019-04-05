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
import cash.z.android.wallet.ui.presenter.HistoryPresenterModule
import cash.z.android.wallet.ui.util.AlternatingRowColorDecoration
import cash.z.wallet.sdk.dao.WalletTransaction
import dagger.Module
import dagger.android.ContributesAndroidInjector
import kotlinx.coroutines.launch
import javax.inject.Inject


class HistoryFragment : BaseFragment(), HistoryPresenter.HistoryView {

    @Inject
    lateinit var historyPresenter: HistoryPresenter
    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil
            .inflate<FragmentHistoryBinding>(inflater, R.layout.fragment_history, container, false)
            .also { binding = it }
            .root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (mainActivity != null) {
            mainActivity?.setToolbarShown(true)
            binding.recyclerTransactionsHistory.apply {
                layoutManager = LinearLayoutManager(mainActivity, RecyclerView.VERTICAL, false)
                adapter = TransactionAdapter(R.layout.item_transaction_history)
                addItemDecoration(AlternatingRowColorDecoration())
            }
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
        mainActivity?.supportActionBar?.title = resources.getQuantityString(R.plurals.history_transaction_count_title,
            transactions.size, transactions.size)
        with (binding.recyclerTransactionsHistory) {
            (adapter as TransactionAdapter).submitList(transactions)
            postDelayed({
                smoothScrollToPosition(0)
            }, 100L)
        }
     }
}

@Module
abstract class HistoryFragmentModule {
    @ContributesAndroidInjector(modules = [HistoryPresenterModule::class])
    abstract fun contributeHistoryFragment(): HistoryFragment
}