package com.kosherclimate.userapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button

class CheckDevelopermode : AppCompatActivity() {

    lateinit var location_Clickhere : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_developermode)

        location_Clickhere = findViewById(R.id.location_Clickhere)

        location_Clickhere.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}