package com.example.stepcounterr

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun convertDateToTimestamp(): String{
        val dateFormat  = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        val currentTime = dateFormat.format(date)
        val timestamp = dateFormat.parse(currentTime) as Date
        return timestamp.time.toString()
    }

    // Get List Sunday Of Month
    fun getListSundayOfMonth(): MutableList<String>{
        val listSundayDate: MutableList<Date> = ArrayList()
        val listSunday: MutableList<String> = ArrayList()
        val cal = Calendar.getInstance()
        cal[Calendar.DAY_OF_MONTH] = 1
        val month = cal[Calendar.MONTH]
        do {
            val dayOfWeek = cal[Calendar.DAY_OF_WEEK]
            if (dayOfWeek == Calendar.SUNDAY) listSundayDate.add(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        } while (cal[Calendar.MONTH] === month)
        val fmt = SimpleDateFormat("yyyy-MM-dd")

        for (date in listSundayDate){
            listSunday.add(fmt.format(date))
        }
        return listSunday
    }

    // [0]: Year, [1]: Month, [2]: Day
    fun getYearMonthDay(time: String): List<String>{
        return time.trim().split("-")
    }


}