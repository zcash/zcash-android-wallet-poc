package cash.z.android.wallet.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cash.z.android.wallet.R
import cash.z.android.wallet.extention.toAppColor
import cash.z.android.wallet.vo.WalletTransaction
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*


class TransactionAdapter(private val transactions: MutableList<WalletTransaction> = mutableListOf()) :
    RecyclerView.Adapter<TransactionViewHolder>() {

    init {
        transactions.sortBy { it.timestamp * -1 }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun getItemCount(): Int = transactions.size

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) = holder.bind(transactions[position])

    fun setTransactions(txs: List<WalletTransaction>) {
        transactions.clear()
        transactions.addAll(txs)
        notifyDataSetChanged()
    }

    fun add(tx: WalletTransaction) {
        // TODO: work with a set of transactions rather than a list
        transactions.add(0, tx)
        notifyItemInserted(0)
    }
}

class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val status = itemView.findViewById<View>(R.id.view_transaction_status)
    private val timestamp = itemView.findViewById<TextView>(R.id.text_transaction_timestamp)
    private val amount = itemView.findViewById<TextView>(R.id.text_transaction_amount)
    private val background = itemView.findViewById<View>(R.id.container_transaction)
    private val formatter = SimpleDateFormat("M/d h:mma", Locale.getDefault())

    fun bind(tx: WalletTransaction) {
        val sign = if(tx.amount > BigDecimal.ZERO) "+" else "-"
        val amountColor = if(tx.amount > BigDecimal.ZERO) R.color.colorPrimary else R.color.text_dark_dimmed
        status.setBackgroundColor(tx.status.color.toAppColor())
        timestamp.text = formatter.format(tx.timestamp)
        amount.text = String.format("$sign %,.3f", tx.amount.round(MathContext(3, RoundingMode.HALF_EVEN )).abs())
        amount.setTextColor(amountColor.toAppColor())
    }

}

