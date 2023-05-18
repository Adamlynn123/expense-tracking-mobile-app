package com.example.myexpensetracker

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SummaryActivity : AppCompatActivity() {

    // Initializing variables
    var selectedCategory = ""
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var transactions: List<Transaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // Set up the recycler view
        transactions = emptyList()
        recyclerView = findViewById(R.id.summary_recycler_view)
        adapter = TransactionAdapter(transactions)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Referencing the pie chart
        val pieChart = findViewById<PieChart>(R.id.pie_chart)

        // Referencing colors for the pie chart
        val colors = listOf(
            ContextCompat.getColor(this, R.color.food_color),
            ContextCompat.getColor(this, R.color.transportation_color),
            ContextCompat.getColor(this, R.color.entertainment_color),
            ContextCompat.getColor(this, R.color.medical_color),
            ContextCompat.getColor(this, R.color.housing_color),
            ContextCompat.getColor(this, R.color.income_color),
            ContextCompat.getColor(this, R.color.insurance_color),
            ContextCompat.getColor(this, R.color.miscellaneous_color),
            ContextCompat.getColor(this, R.color.personal_color),
            ContextCompat.getColor(this, R.color.savings_color),
            ContextCompat.getColor(this, R.color.utilities_color)
        )

        // Use coroutines to access database and update UI asynchronously
        GlobalScope.launch {
            var db: AppDatabase? = null
            try {
                // Build the database
                db = Room.databaseBuilder(this@SummaryActivity,
                    AppDatabase::class.java,
                    "transactions"
                ).build()

                // Get total expenses from each category using the DAO getSumByCategory function
                val foodTotal = -db.transactionDao().getSumByCategory("Food")
                val transportationTotal = -db.transactionDao().getSumByCategory("Transportation")
                val entertainmentTotal = -db.transactionDao().getSumByCategory("Entertainment")
                val medicalTotal = -db.transactionDao().getSumByCategory("Medical")
                val housingTotal = -db.transactionDao().getSumByCategory("Housing")
                val incomeTotal = -db.transactionDao().getSumByCategory("Income")
                val insuranceTotal = -db.transactionDao().getSumByCategory("Insurance")
                val miscellaneousTotal = -db.transactionDao().getSumByCategory("Miscellaneous")
                val personalTotal = -db.transactionDao().getSumByCategory("Personal")
                val savingsTotal = -db.transactionDao().getSumByCategory("Savings")
                val utilitiesTotal = -db.transactionDao().getSumByCategory("Utilities")

                // Setup the pie chart entries and filter out all that are greater than zero
                val entries = listOf(
                    PieEntry(foodTotal, "Food"),
                    PieEntry(transportationTotal, "Transportation"),
                    PieEntry(entertainmentTotal, "Entertainment"),
                    PieEntry(medicalTotal, "Medical"),
                    PieEntry(housingTotal, "Housing"),
                    PieEntry(incomeTotal, "Income"),
                    PieEntry(insuranceTotal, "Insurance"),
                    PieEntry(miscellaneousTotal, "Miscellaneous"),
                    PieEntry(personalTotal, "Personal"),
                    PieEntry(savingsTotal, "Savings"),
                    PieEntry(utilitiesTotal, "Utilities"),
                ).filter { it.value > 0f }


                // Set the data set and colors for the pie chart
                val dataSet = PieDataSet(entries, "Expenses")
                dataSet.colors = colors
                val data = PieData(dataSet)

                // Formatting pie chart
                data.setValueFormatter(PercentFormatter(pieChart))
                data.setValueTextSize(20f)
                pieChart.setDrawEntryLabels(true)
                pieChart.setUsePercentValues(true)
                pieChart.legend.isEnabled = false
                pieChart.setEntryLabelColor(Color.BLACK)
                pieChart.setCenterTextOffset(5f, 5f)

                // Setting the listener for clicking on a pie chart slice
                pieChart.setOnChartValueSelectedListener(object: OnChartValueSelectedListener{
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        if (e != null) {

                            // Getting label for category
                            selectedCategory = (e as PieEntry).label

                            // Build database
                            val db = Room.databaseBuilder(
                                this@SummaryActivity,
                                AppDatabase::class.java,
                                "transactions"
                            ).build()

                            // Launching coroutine to get all transaction for a specified category
                            GlobalScope.launch {
                                transactions =
                                    db.transactionDao().getTransactionsByCategory(selectedCategory)

                                // Updating the adapter with the retrieved transactions
                                withContext(Dispatchers.Main) {
                                    adapter.setData(transactions)
                                }

                                // Updating the pie chart center text with selected category and total amount
                                val entryIndex = dataSet.getEntryIndex(e)
                                val entry = dataSet.getEntryForIndex(entryIndex) as PieEntry
                                val label = entry.label
                                val value = entry.value
                                pieChart.centerText = "$label\nTotal Spent: $ $value"
                                pieChart.setCenterTextSize(20f)
                                pieChart.invalidate()
                            }
                        }
                    }
                    override fun onNothingSelected() {
                        return
                    }
                })


                // Setting the data for the pie chart and invalidating it
                withContext(Dispatchers.Main) {
                    pieChart.data = data
                    pieChart.invalidate()
                    }
                } finally {
                    db?.close()
            }
        }
    }
}

