package com.example.stepcounterr

import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun convertDateToTimestamp(): String{
        val dateFormat  = SimpleDateFormat("dd-MM-yyyy")
        val date: Date = Date()
        val currentTime = dateFormat.format(date)
        val timestamp = dateFormat.parse(currentTime) as Date
        return timestamp.time.toString()
    }
}