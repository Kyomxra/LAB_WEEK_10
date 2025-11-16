package com.example.lab_week_10

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.viewmodels.TotalViewModel
import android.widget.Toast
import com.example.lab_week_10.database.TotalObject
import java.util.Date


class MainActivity : AppCompatActivity() {

    private val db by lazy { prepareDatabase() }

    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeValueFromDatabase()

        prepareViewModel()
    }

    override fun onPause() {
        super.onPause()
        val currentDate = java.util.Date().toString()
        db.totalDao().update(
            Total(
                ID,
                TotalObject(
                    value = viewModel.total.value ?: 0,
                    date = currentDate
                )
            )
        )
    }

    override fun onStart() {
        super.onStart()

        val total = db.totalDao().getTotal(ID)
        if (total.isNotEmpty()) {
            val lastDate = total.first().total.date

            if (lastDate.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Last updated: $lastDate",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text =
            getString(R.string.text_total, total)
    }

    private fun prepareViewModel() {
        viewModel.total.observe(this, {
            updateText(it)
        })

        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    private fun prepareDatabase(): TotalDatabase {
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java, "total-database"
        ).fallbackToDestructiveMigration().allowMainThreadQueries().build()
    }

    private fun initializeValueFromDatabase() {
        val total = db.totalDao().getTotal(ID)
        if (total.isEmpty()) {
            db.totalDao().insert(
                Total(
                    id = ID,
                    total = TotalObject(value = 0, date = "")
                )
            )
        } else {
            viewModel.setTotal(total.first().total.value)
        }
    }

    companion object {
        const val ID: Long = 1
    }
}