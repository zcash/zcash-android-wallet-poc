package cash.z.android.wallet.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cash.z.android.wallet.R
import cash.z.android.wallet.extention.toAppColor
import cash.z.wallet.sdk.ext.convertZatoshiToZec
import cash.z.wallet.sdk.ext.toZec
import java.text.SimpleDateFormat
import cash.z.wallet.sdk.dao.WalletTransaction
import java.util.*
import kotlin.math.absoluteValue


class TransactionAdapter(@LayoutRes val itemResId: Int = R.layout.item_transaction) : ListAdapter<WalletTransaction, TransactionViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(itemResId, parent, false)
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
    private val icon = itemView.findViewById<ImageView>(R.id.image_transaction_type)
    private val timestamp = itemView.findViewById<TextView>(R.id.text_transaction_timestamp)
    private val amount = itemView.findViewById<TextView>(R.id.text_transaction_amount)
    private val address = itemView.findViewById<TextView>(R.id.text_transaction_address)
    private val formatter = SimpleDateFormat("M/d h:mma", Locale.getDefault())

    fun bind(tx: WalletTransaction) {
        val sign = if (tx.isSend) "-" else "+"
        val amountColor = if (tx.isSend) R.color.text_dark_dimmed else R.color.colorPrimary
        val transactionColor = if (tx.isSend) R.color.send_associated else R.color.receive_associated
        val transactionIcon = if (tx.isSend) R.drawable.ic_sent_transaction else R.drawable.ic_received_transaction
        val zecAbsoluteValue = tx.value.absoluteValue.convertZatoshiToZec(3)
        val toOrFrom = if (tx.isSend) "to" else "from"
        val srcOrDestination = address ?: "from shielded mystery person"
        timestamp.text = if (!tx.isMined || tx.timeInSeconds == 0L) "Pending" else formatter.format(tx.timeInSeconds * 1000)
        amount.text = "$sign$zecAbsoluteValue"
        amount.setTextColor(amountColor.toAppColor())

        // maybes - and if this gets to be too much, then pass in a custom holder when constructing the adapter, instead
        status?.setBackgroundColor(transactionColor.toAppColor())
        address?.text = "$toOrFrom $srcOrDestination"
        icon?.setImageResource(transactionIcon)
    }
}
