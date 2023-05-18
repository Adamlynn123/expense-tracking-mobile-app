// This code is defining a RecyclerView Adapter for Transactions, it also includes predefined icons for transaction categories.
// Importing necessary Android libraries and declaring the TransactionAdapter class.
package com.example.myexpensetracker
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class TransactionAdapter(private var transactions: List<Transaction>): RecyclerView.Adapter<TransactionAdapter.TransactionHolder>(){
    // Defining a map of category icons based on transaction category name.
    private val CATEGORY_ICONS = mapOf(
        "Transportation" to R.drawable.car,
        "Food" to R.drawable.food,
        "Entertainment" to R.drawable.game,
        "Medical" to R.drawable.health,
        "Housing" to R.drawable.house,
        "Income" to R.drawable.income,
        "Insurance" to R.drawable.insurance,
        "Miscellaneous" to R.drawable.miscellaneous,
        "Personal" to R.drawable.personal,
        "Savings" to R.drawable.savings,
        "Utilities" to R.drawable.utilities
    )

    // Defining a view holder for RecyclerView item.
    class TransactionHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.findViewById(R.id.tv_label) // Declaring TextView for transaction label.
        val tvAmount: TextView = view.findViewById(R.id.tv_amount) // Declaring TextView for transaction amount.
        val ivIcon: ImageView = view.findViewById(R.id.iv_icon) // Declaring Imageview for transaction icon.
    }

    // Overriding onCreateViewHolder method to get a reference to the layout that should be used for displaying each item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_layout, parent, false) // Inflating the transaction_layout.xml layout.
        return TransactionHolder(view) // Creating a new instance of Transaction Holder with view reference.
    }

    // Overriding onBindViewHolder method to set values of each item in ListView.
    override fun onBindViewHolder(holder: TransactionHolder, position: Int) {
        val transaction = transactions[position] // Getting current transaction object.
        val context = holder.tvAmount.context // Getting current Context.

        if(transaction.tv_amount >= 0) { // Setting text and color for positive amount.
            holder.tvAmount.text = "+ $%.2f".format(transaction.tv_amount)
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.green))
        }else{ // Setting text and color for negative amount.
            holder.tvAmount.text = "- $%.2f".format(abs(transaction.tv_amount))
            holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.red))
        }

        holder.tvLabel.text = transaction.tv_label // Setting transaction label text.
        holder.ivIcon.setImageResource(CATEGORY_ICONS[transaction.category] ?: R.drawable.default_transaction) // Setting transaction image.

        holder.itemView.setOnClickListener{ // Defining click listener for each item in ListView.
            val intent = Intent(context, DetailedActivity::class.java) // Creating a new intent for detailed activity.
            intent.putExtra("transaction", transaction) // Adding transaction data to the intent.
            context.startActivity(intent) // Starting the detailed activity.
        }
    }

    // Getting total number of items in RecyclerView.
    override fun getItemCount(): Int {
        return transactions.size
    }

    // Updating transaction data and refreshing the RecyclerView.
    fun setData(transactions: List<Transaction>){
        this.transactions = transactions
        notifyDataSetChanged()
    }
}
