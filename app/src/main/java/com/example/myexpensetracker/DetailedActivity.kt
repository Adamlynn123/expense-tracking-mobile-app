package com.example.myexpensetracker

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DetailedActivity : AppCompatActivity() {

    private lateinit var transaction : Transaction

    private val categories = arrayOf("Food", "Transportation", "Entertainment", "Medical", "Housing", "Income", "Insurance", "Miscellaneous", "Personal", "Savings", "Utilities")

    lateinit var adapterItems: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed)

        transaction = intent.getSerializableExtra("transaction") as Transaction

        val updateTransactionButton =
            findViewById<Button>(R.id.btn_update_transaction)
        val labelInput =
            findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.label_input)
        val amountInput =
            findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.amount_input)
        val categoryInput =
            findViewById<AutoCompleteTextView>(R.id.category_input)
        val transactionDate =
            findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.transaction_date)
        val descriptionInput =
            findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.description_input)

        val rootView =
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.root_view)
        val labelLayout =
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.label_layout)
        val amountLayout =
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.amount_layout)
        val categoryLayout =
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.category_layout)
        val closeBtn =
            findViewById<ImageButton>(R.id.closeBtn)

        // Clear focus from the currently focused view, if any
        rootView.setOnClickListener{
            this.window.decorView.clearFocus()

            // Get the InputMethodManager service and hide the keyboard
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        // Set the text fields and teh date label with the transaction object's values
        labelInput.setText(transaction.tv_label)
        amountInput.setText(transaction.tv_amount.toString())
        categoryInput.setText(transaction.category)

        val dateFormat = SimpleDateFormat("MM/dd/yyyy")
        val formattedDate = dateFormat.format(Date(transaction.date))
        transactionDate.setText(formattedDate)
        descriptionInput.setText((transaction.description))

        // Set up the adapter for the category input's dropdown menu
        adapterItems = ArrayAdapter(this, R.layout.list_item, categories)
        categoryInput.setAdapter(adapterItems)

        // Listener for the category input dropdown menu
        categoryInput.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->

                // Get the selected item and display it in a toast message
                val item = parent.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, "Item: $item", Toast.LENGTH_SHORT).show()
            }

        // listener functions to display errors on layout for label, amount, and category if none selected
        labelInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                labelLayout.error = null
                updateTransactionButton.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }
        })

        amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTransactionButton.visibility = View.VISIBLE
                amountLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }
        })

        categoryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTransactionButton.visibility = View.VISIBLE
                amountLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }
        })

        descriptionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTransactionButton.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }
        })

        updateTransactionButton.setOnClickListener {

            // Get the values entered by the user and format for database
            val label = labelInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val category = categoryInput.text.toString()
            val date = transaction.date.toString().toLong()
            val description = descriptionInput.text.toString()

            // Validate input for label, amount, and category
            if (label.isEmpty())
                labelLayout.error = "Please enter a valid label"
            else if (amount == null)
                amountLayout.error = "Please enter a valid amount"
            else if (category.isEmpty())
                categoryLayout.error = "Please enter a category"
            else {

                // Create a new transaction object with the updated values
                val transaction = Transaction(transaction.id, label, amount, category, description, date)

                update(transaction)
            }
        }

        closeBtn.setOnClickListener {
            finish()
        }
    }

    private fun update(transaction: Transaction){

        // Build database
        val db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        // launch DAO function in a coroutine background thread
        GlobalScope.launch {
            db.transactionDao().update(transaction)
            finish()
        }
    }
}
