package com.example.stepcounterr

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.boyzdroizy.simpleandroidbarchart.SimpleBarChart
import com.example.stepcounterr.Utils.getListSundayOfMonth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MainCounterAct : AppCompatActivity() {
    private val _googleFitPermissionsRequestCode: Int = 102
    private val _myPermissionRequestActivityRecognition: Int = 101
    private lateinit var fitnessOptions: FitnessOptions
    private lateinit var tvStepsCounter: TextView
    private lateinit var circularProgressBar: CircularProgressBar

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_main_counter)

        initViews()

        // SimpleBarChart
//        val chartData = (12 downTo 1).map { Random.nextInt(10, 100) }.toMutableList()
//        val intervalData = (12 downTo 1).map { it }.toMutableList()
//        val simpleBarChart = findViewById<SimpleBarChart>(R.id.simpleBarChart)
//        simpleBarChart.setChartData(chartData, intervalData)
//        simpleBarChart.setMaxValue(100)
//        simpleBarChart.setMinValue(0)

        // Step 1: Check Permission ACTIVITY_RECOGNITION & ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.ACTIVITY_RECOGNITION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    _myPermissionRequestActivityRecognition
                )
            }
        }

        // Register FitnessOption & Login Account Google
        fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this, // your activity
                _googleFitPermissionsRequestCode, // e.g. 1
                account,
                fitnessOptions
            )
        } else {
            accessGoogleFit()
        }

    }

    private fun initViews() {
        tvStepsCounter = findViewById(R.id.tvStepsCounter)
        circularProgressBar = findViewById(R.id.circularProgressBar)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun accessGoogleFit() {
        // Record fitness data:
        // 1. The Recording API lets your app request automated storage of sensor data in a battery-efficient manner by creating subscriptions
        // 2. Enables low-battery, always-on background collection of sensor data into the Google Fit store.
        subscribeToFitnessData()

        // Sensor API: Read data near-real-time
        readStepsRealTime()
        // History API: method Read Data Daily Total
        readDataDailyTotal()

        readDataDaysOfWeek()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun readDataDaysOfWeek() {
        // Read the data that's been collected throughout the past week.
        val zoneId = ZoneId.of(ZoneId.systemDefault().toString())
        var endTime: ZonedDateTime? = null
        for(i in 0 until getListSundayOfMonth().size){
            if(Utils.convertDateToTimestamp().toLong() <= SimpleDateFormat("yyyy-MM-dd").parse(getListSundayOfMonth()[i]).time){
                endTime = ZonedDateTime.of(Utils.getYearMonthDay(getListSundayOfMonth()[i])[0].toInt(), Utils.getYearMonthDay(getListSundayOfMonth()[i])[1].toInt(), Utils.getYearMonthDay(getListSundayOfMonth()[i])[2].toInt(), 23, 59, 59,0, zoneId)
                break
            }
        }
//        val endTime = ZonedDateTime.of(2023, 4, 2, 23, 59, 59,0, zoneId)
        val startTime = endTime!!.minusWeeks(1)
        Log.e("Logger", "Range Start: $startTime")
        Log.e("Logger", "Range End: $endTime")

        val readRequest =
            DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()

        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener { response ->
                // The aggregate query puts datasets into buckets, so flatten into a
                // single list of datasets
                for (dataSet in response.buckets.flatMap { it.dataSets }) {
                    dumpDataSet(dataSet)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Logger","There was an error reading data from Google Fit", e)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dumpDataSet(dataSet: DataSet) {
        Log.e("Logger", "Data returned for Data type: ${dataSet.dataType.name}")
        for (dp in dataSet.dataPoints) {
            Log.e("Logger","Data point:")
            Log.e("Logger","\tType: ${dp.dataType.name}")
            Log.e("Logger","\tStart: ${dp.getStartTimeString()}")
            Log.e("Logger","\tEnd: ${dp.getEndTimeString()}")
            for (field in dp.dataType.fields) {
                Log.e("Logger","\tField: ${field.name} Value: ${dp.getValue(field)}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun DataPoint.getStartTimeString() = Instant.ofEpochSecond(this.getStartTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    @RequiresApi(Build.VERSION_CODES.O)
    fun DataPoint.getEndTimeString() = Instant.ofEpochSecond(this.getEndTime(TimeUnit.SECONDS))
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime().toString()

    private fun readDataDailyTotal() {
        Fitness.getHistoryClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                // When open app then auto update valueStepsCounter
                if(it.dataPoints.size != 0){
                    val dataDailyTotal = it.dataPoints.first().getValue(Field.FIELD_STEPS)
                    Log.e("Logger", "Steps Total: ${dataDailyTotal.asInt()}")
                    tvStepsCounter.text = getString(R.string.steps_counter, dataDailyTotal.asInt())
                    circularProgressBar.progress = dataDailyTotal.toString().toFloat()
                    PreferManager.getInstance(this)!!.write(Utils.convertDateToTimestamp(), dataDailyTotal.asInt())
                }else{
                    tvStepsCounter.text = getString(R.string.steps_counter, 0)
                }
            }
            .addOnFailureListener { e ->
                Log.w("Logger","There was an error reading data from Google Fit", e)
            }
    }

    private fun readStepsRealTime() {
        val listener = OnDataPointListener { dataPoint ->
            for (field in dataPoint.dataType.fields) {
                // value represent steps in setSamplingRate(5, TimeUnit.SECONDS)
                val value = dataPoint.getValue(field)
                Log.e("Logger", "Detected DataPoint field: ${field.name}")
                Log.e("Logger", "Detected DataPoint value: $value")

                // Update Steps Counter
                var stepsCounter = PreferManager.getInstance(this)!!.readInt(Utils.convertDateToTimestamp(), 0)
                stepsCounter += dataPoint.getValue(field).asInt()

                // Update local
                PreferManager.getInstance(this)!!.write(Utils.convertDateToTimestamp(), stepsCounter)
                // Update Textview, CircularProgressbar
                tvStepsCounter.text = getString(R.string.steps_counter, PreferManager.getInstance(this)!!.readInt(Utils.convertDateToTimestamp(), 0))
                circularProgressBar.progress = PreferManager.getInstance(this)!!.readInt(Utils.convertDateToTimestamp(), 0).toFloat()
            }
        }

        Fitness.getSensorsClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .add(
                SensorRequest.Builder()
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setSamplingRate(5, TimeUnit.SECONDS)
                    .build(),
                listener
            )
            .addOnSuccessListener {
                Log.e("Logger", "Listener registered!")
            }
            .addOnFailureListener {
                Log.e("Logger", "Listener not registered.", it)
            }
    }

    private fun subscribeToFitnessData() {
        Fitness.getRecordingClient(this, GoogleSignIn.getAccountForExtension(this, fitnessOptions))
            .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.e("Logger", "Successfully subscribed!")
            }
            .addOnFailureListener {
                Log.e("Logger", "There was a problem subscribing: $it")
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                _googleFitPermissionsRequestCode -> accessGoogleFit()
                else -> {
                    // Result wasn't from Google Fit
                    Log.e("Logger", "Result wasn't from Google Fit")
                }
            }
            else -> {
                // Permission not granted
                Log.e("Logger", "Permission not granted")
            }
        }
    }
}