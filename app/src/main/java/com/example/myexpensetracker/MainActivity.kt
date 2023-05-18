package com.example.myexpensetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

// This code defines a MainActivity class that extends the AppCompatActivity class.
class MainActivity : AppCompatActivity() {

    // Declaring instance variables for deletedTransaction, newRecyclerView, transactions, oldTransactions, transactionAdapter, linearLayoutManager, db and selectedOption.
    private lateinit var deletedTransaction: Transaction
    private lateinit var newRecyclerView: RecyclerView
    private lateinit var transactions : List<Transaction>
    private lateinit var oldTransactions : List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db : AppDatabase
    private var selectedOption: String = "All Time"



    // Overriding the onCreate method to initialize the Activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Setting the layout for the activity.

        val filterOptions = resources.getStringArray(R.array.filter_options) // Getting filter options from resources.

        transactions = arrayListOf() // Initializing an empty list of Transactions.

        transactionAdapter = TransactionAdapter(transactions) // Creating a new TransactionAdapter with the previously initialized transactions list.

        linearLayoutManager = LinearLayoutManager(this) // Initializing a new LinearLayoutManager.
        linearLayoutManager.reverseLayout = true

        newRecyclerView = findViewById(R.id.recyclerView) // Finding reference to the RecyclerView in the XML layout file.

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "transactions"
        ).build() // Building the AppDatabase.

        newRecyclerView.apply { // Applying the configuration to the RecyclerView instance.
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

        // Create an instance of ItemTouchHelper.SimpleCallback to enable swipe-to-delete functionality
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Disable dragging and dropping
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Delete the transaction associated with the swiped item
                deleteTransaction(transactions[viewHolder.bindingAdapterPosition])
            }
        }

        // Attach the itemTouchHelper to the RecyclerView to enable swipe-to-delete functionality
        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(newRecyclerView)

        // Find the "Add Transaction" floating action button and set an onClickListener to start AddTransactionActivity
        val addBtn = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.addBtn)
        addBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        // Find the "Summary" button and set an onClickListener to start SummaryActivity
        val summaryBtn = findViewById<Button>(R.id.summaryBtn)
        summaryBtn.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java)
            startActivity(intent)
        }

        // Find the filter spinner and set an adapter using the filter_options array as the data source
        val spinner = findViewById<Spinner>(R.id.spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.filter_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        // Set an onItemSelectedListener for the spinner that updates selectedOption and calls fetchByDate() when an item is selected
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Update selectedOption with the selected filter option
                selectedOption = filterOptions[position]
                // Fetch transactions by date using the selected filter option
                fetchByDate()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // do nothing
            }
        }
    }

    // Defining function to fetch the transactions specified by a certain date
    private fun fetchByDate() {
        var startDate = 0L
        var endDate = System.currentTimeMillis()
        if (selectedOption == "Last Week") {
            // seconds in 7 days
            startDate = System.currentTimeMillis() - 604800000
        }
        else if (selectedOption == "Last Month") {
            // seconds in 30 days
            startDate = System.currentTimeMillis() - 2592000000
        }
        else if (selectedOption == "Last 3 Months") {
            // seconds in 90 days
            startDate = System.currentTimeMillis() - 7776000000
        }
        else if (selectedOption == "Last 6 Months") {
            // seconds in 180 days
            startDate = System.currentTimeMillis() - 15552000000
        }
        else if (selectedOption == "Last Year") {
            // seconds in 365 days
            startDate = System.currentTimeMillis() - 31536000000
        }
        else {fetchAll()}

        // Launch a coroutine in the global scope to fetch transactions by date
        GlobalScope.launch {

            // transaction variable that holds the call to the DAO function
            transactions = db.transactionDao().getTransactionsByDateRange(startDate, endDate)

            // Switch back to the main ui thread and update the dashboard and transactionAdapter
            runOnUiThread{
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }

    // Defining function to get all transactions from the database
    private fun fetchAll(){

        // Launch a coroutine in the global scope te fetch all transactions
        GlobalScope.launch {
            transactions = db.transactionDao().getAll()

            // Switch back to the main ui thread and update the dashboard and transactionAdapter
            runOnUiThread{
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }

    // Define a function that will update information displayed on the dashboard
    private fun updateDashboard(){

        // Calculating the total, income, and expense amount based on the transactions list
        val totalAmount = transactions.sumOf { it.tv_amount }
        val incomeAmount = transactions.filter { it.tv_amount > 0 }.sumOf { it.tv_amount }
        val expenseAmount = totalAmount - incomeAmount

        // Update the balance, income, and expenses TextView with the formatted total amount
        val balance = findViewById<TextView>(R.id.tv_total_balance)
        balance.text = "$ ${NumberFormat.getInstance(Locale.US).format(totalAmount)}"

        val income = findViewById<TextView>(R.id.tv_income_value)
        income.text = "$ ${NumberFormat.getInstance(Locale.US).format(incomeAmount)}"

        val expenses = findViewById<TextView>(R.id.tv_expense_value)
        expenses.text = "$ ${NumberFormat.getInstance(Locale.US).format(expenseAmount)}"
    }

    private fun undoDelete() {

        // launching coroutine to insert the deleted transaction
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)

            transactions = oldTransactions

            // Updating dashboard on the main ui thread
            runOnUiThread{
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }


    // Function to show a message when a transaction is deleted
    private fun showSnackbar() {

        // Get the coordinator layout from the activity's layout
        val view = findViewById<View>(R.id.coordinator)

        // Create a Snack bar with the "Transaction Deleted!" Message and attach it to the coordinator layout
        val snackbar = Snackbar.make(view, "Transaction Deleted!", Snackbar.LENGTH_LONG)

        // Adding the undo functionality to the taskbar
        snackbar.setAction("Undo"){
            undoDelete()
        }
            // Formatting
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }

    private fun deleteTransaction(transaction: Transaction){

        // Save the deleted transaction and the old transactions list for undo functionality
        deletedTransaction = transaction
        oldTransactions = transactions

        // Delete transaction from database in a separate coroutine
        GlobalScope.launch {
            db.transactionDao().delete(transaction)

            // Remove the transaction from the list of transactions
            transactions = transactions.filter {it.id != transaction.id}

            runOnUiThread{
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchByDate()
    }

}
