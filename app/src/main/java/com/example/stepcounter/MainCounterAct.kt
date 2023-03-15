package com.example.stepcounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.boyzdroizy.simpleandroidbarchart.SimpleBarChart
import kotlin.random.Random

class MainCounterAct : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main_counter)

        // SimpleBarChart
        val chartData = (12 downTo 1).map { Random.nextInt(10, 100) }.toMutableList()
        val intervalData = (12 downTo 1).map { it }.toMutableList()
        val simpleBarChart = findViewById<SimpleBarChart>(R.id.simpleBarChart)
        simpleBarChart.setChartData(chartData, intervalData)
        simpleBarChart.setMaxValue(100)
        simpleBarChart.setMinValue(0)

    }
}