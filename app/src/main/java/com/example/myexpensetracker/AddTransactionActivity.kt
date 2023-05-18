package com.example.myexpensetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextWatcher
import android.text.Editable
import android.widget.*
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch



class AddTransactionActivity : AppCompatActivity() {

    private val categories = arrayOf("Food", "Transportation", "Entertainment", "Medical", "Housing", "Income", "Insurance", "Miscellaneous", "Personal", "Savings", "Utilities")

    // Defining adapter for the recycler view
    private lateinit var adapterItems: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Getting references to the activity_add_transaction
        val addTransactionButton =
            findViewById<Button>(R.id.btn_add_transaction)
        val labelInput =
            findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.label_input)
        val amountInput =
            findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.amount_input)
        val categoryInput =
            findViewById<AutoCompleteTextView>(R.id.category_input)
        val descriptionInput =
            findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.description_input)

        val labelLayout =
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.label_layout)
        val amountLayout =
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.amount_layout)
        val categoryLayout =
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.category_layout)
        val closeBtn =
            findViewById<ImageButton>(R.id.closeBtn)

        // Adapter for the list of categories
        adapterItems = ArrayAdapter(this, R.layout.list_item, categories)

        // Set the categoryInput AutoCompleteTextView with this adapter
        categoryInput.setAdapter(adapterItems)

        categoryInput.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val item = parent.getItemAtPosition(position).toString()
                Toast.makeText(applicationContext, "Item: $item", Toast.LENGTH_SHORT).show()
            }

        // Adding listeners for the label, amount, and category inputs so that they will raise errors when fields are empty
        labelInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                labelLayout.error = null
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
                amountLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {
                // do nothing
            }
        })

        addTransactionButton.setOnClickListener {

            // Retrieve input values from text field and format them according to database requirements
            val label = labelInput.text.toString()
            val amount = amountInput.text.toString().toDoubleOrNull()
            val category = categoryInput.text.toString()
            val description = descriptionInput.text.toString()
            val date = System.currentTimeMillis()

            // Validate label, amount, and category inputs
            if (label.isEmpty())
                labelLayout.error = "Please enter a valid label"
            else if (amount == null)
                amountLayout.error = "Please enter a valid amount"
            else if (category.isEmpty())
                categoryLayout.error = "Please enter a category"
            else {

                // Create a new transaction object with input values
                val transaction = Transaction(0, label, amount, category, description, date)
                insert(transaction)
            }
        }

        closeBtn.setOnClickListener {
            finish()
        }
    }

        private fun insert(transaction: Transaction){

            // Build database
            val db = Room.databaseBuilder(this,
                AppDatabase::class.java,
                "transactions").build()

            // Launch a coroutine to perform database operation in background thread
            GlobalScope.launch {
                db.transactionDao().insertAll(transaction)
                finish()
            }
        }
    }
