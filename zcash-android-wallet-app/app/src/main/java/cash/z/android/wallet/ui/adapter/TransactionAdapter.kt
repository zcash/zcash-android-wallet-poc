package cash.z.android.wallet.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cash.z.android.wallet.R
import cash.z.android.wallet.extention.toAppColor
import cash.z.android.wallet.vo.WalletTransaction
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*


class TransactionAdapter : ListAdapter<WalletTransaction, TransactionViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) = holder.bind(getItem(position))
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WalletTransaction>() {
    override fun areItemsTheSame(oldItem: WalletTransaction, newItem: WalletTransaction) = oldItem.height == newItem.height
    override fun areContentsTheSame(oldItem: WalletTransaction, newItem: WalletTransaction) = oldItem == newItem
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
        Log.e("TWIG-u", "formatted timestamp: ${tx.timestamp} for value ${amount.text}")
    }

}

