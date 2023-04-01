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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import java.util.concurrent.TimeUnit

class MainCounterAct : AppCompatActivity() {
    private val _keyStepsCounter: String = "_keyStepsCounter"
    private val _googleFitPermissionsRequestCode: Int = 102
    private val _myPermissionRequestActivityRecognition: Int = 101
    private lateinit var fitnessOptions: FitnessOptions
    private lateinit var tvStepsCounter: TextView
    private lateinit var circularProgressBar: CircularProgressBar
    private var stepsCounter: Long = 0

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

        // Save valueStepsCounter use SharedPreference
        // When auto rotate not set steps = 0
        // When open app then auto update valueStepsCounter
        stepsCounter = PreferManager.getInstance(this)!!.readLong(Utils.convertDateToTimestamp(), 0)
        tvStepsCounter.text = getString(R.string.steps_counter, stepsCounter)
        circularProgressBar.progress = stepsCounter.toFloat()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun accessGoogleFit() {
        // Record fitness data:
        // 1. The Recording API lets your app request automated storage of sensor data in a battery-efficient manner by creating subscriptions
        // 2. Enables low-battery, always-on background collection of sensor data into the Google Fit store.
        subscribeToFitnessData()
        readStepsRealTime()
    }

    private fun readStepsRealTime() {
        val listener = OnDataPointListener { dataPoint ->
            for (field in dataPoint.dataType.fields) {
                // value represent steps in setSamplingRate(5, TimeUnit.SECONDS)
                val value = dataPoint.getValue(field)
                Log.i("Logger", "Detected DataPoint field: ${field.name}")
                Log.i("Logger", "Detected DataPoint value: $value")

                // Update Steps Counter
                stepsCounter += dataPoint.getValue(field).asInt()
                // Update local
                PreferManager.getInstance(this)!!.write(Utils.convertDateToTimestamp(), stepsCounter)
                // Update Textview, CircularProgressbar
                tvStepsCounter.text = getString(R.string.steps_counter, PreferManager.getInstance(this)!!.readLong(Utils.convertDateToTimestamp(), 0))
                circularProgressBar.progress = PreferManager.getInstance(this)!!.readLong(Utils.convertDateToTimestamp(), 0).toFloat()
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
                Log.i("Logger", "Listener registered!")
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