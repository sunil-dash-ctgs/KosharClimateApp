package com.kosherclimate.userapp

import android.content.Context
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView

class TimerData(val context: Context, val textView: TextView) {

    var MillisecondTime: Long = 0
    var StartTime:Long = 0
    var TimeBuff:Long = 0
    var UpdateTime:Long = 0L
    lateinit var handler: Handler
    var Seconds = 0
    var Minutes:Int = 0
    var MilliSeconds:Int = 0

    var pusetimestr : String  = "0"
    var pusetimestrdata : String  = "0"

    fun startTime(timer : Long) : Long{

        if (timer.toInt() == 0){

            handler = Handler()
            StartTime = SystemClock.uptimeMillis()
            handler.postDelayed(runnable, 0)

        }else{

            handler = Handler()
            StartTime = timer
            handler.postDelayed(runnable, 0)

        }

        return StartTime
    }

    fun resetTimer(){

        MillisecondTime = 0L
        StartTime = 0L
        TimeBuff = 0L
        UpdateTime = 0L
        Seconds = 0
        Minutes = 0
        MilliSeconds = 0
        textView.setText("00:00")
    }

    fun puseTimer(){


        TimeBuff += MillisecondTime
        handler.removeCallbacks(runnable)

        Log.d("usertimedetails","userdata  "+TimeBuff.toString())
        Log.d("usertimedetails","userdata1  "+MillisecondTime.toString())

        pusetimestr = "1"

        var datastr = TimeBuff.toString()
        var dataprovide = datastr.substring(0, datastr.length - 2)

        pusetimestrdata = (dataprovide.toLong() * 1000).toString()

        Log.d("detailsparamter",pusetimestrdata)

    }

    var runnable: Runnable = object : Runnable {
        override fun run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime
            UpdateTime = TimeBuff + MillisecondTime
            Seconds = (UpdateTime / 1000).toInt()
            Minutes = Seconds / 60
            Seconds = Seconds % 60
            //  MilliSeconds = (UpdateTime % 1000).toInt()
            // timer.text = ("" + Minutes + ":" + String.format("%02d", Seconds) + ":" + String.format("%03d", MilliSeconds))
            textView.text = ("" + Minutes + ":" + String.format("%02d", Seconds))
            handler.postDelayed(this, 0)
        }
    }
}