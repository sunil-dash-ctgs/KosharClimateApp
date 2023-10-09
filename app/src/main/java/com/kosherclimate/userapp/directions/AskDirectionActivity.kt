package com.kosherclimate.userapp.directions

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.R

class AskDirectionActivity : AppCompatActivity() {
    private lateinit var btnYes :Button
    private lateinit var btnNo :Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask_direction)

        btnYes = findViewById(R.id.btnYes)
        btnNo = findViewById(R.id.btnNo)

        btnYes.setOnClickListener {
            try {
                // Start Google Maps navigation
                val destinationLat = 19.2030581 // Replace with your destination latitude
                val destinationLng = 72.8617238 // Replace with your destination longitude

                val uri = "google.navigation:q=$destinationLat,$destinationLng"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps") // Use the Google Maps app
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    val warningDialog = SweetAlertDialog(this@AskDirectionActivity, SweetAlertDialog.WARNING_TYPE)
                    warningDialog.titleText = resources.getString(R.string.warning)
                    warningDialog.contentText = "Something went wrong, Please try Again"
                    warningDialog.confirmText = resources.getString(R.string.ok)
                    warningDialog.setCancelClickListener {
                        warningDialog.cancel()
                    }.show()
                }
            }catch (e:Exception){
                Log.e("NEW_TEST","Error >>>>>>$e")
            }

        }
    }
}