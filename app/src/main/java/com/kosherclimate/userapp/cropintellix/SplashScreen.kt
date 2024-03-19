package com.kosherclimate.userapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.kosherclimate.userapp.cropintellix.LanguageSelectActivity


class SplashScreen : AppCompatActivity() {

    private val REQUEST_CODE_WRITE_SETTINGS = 123
    var mockLocationHelper : MockLocationHelper = MockLocationHelper()

    lateinit var sharedPreference : SharedPreferences
    var logged : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        logged = sharedPreference.getBoolean("isLoggedin", false)

        val devOptions = Settings.Secure.getInt(
            this.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        )

        gonextSystem()

//        if (mockLocationHelper.isDevMode(this@SplashScreen)) {
//
//            startActivity(Intent(this@SplashScreen,CheckDevelopermode::class.java))
//
////            val builder: AlertDialog.Builder = AlertDialog.Builder(this@SplashScreen)
////            builder.setMessage(
////                "Please Turn Off Developer Option \n" +
////                        " \n" +
////                        " Go to Settings > Search developer options and toggle them off."
////            )
////            builder.setCancelable(false)
////            builder.setNegativeButton(" Ok ") { dialog, which ->
////                dialog.dismiss()
////                //finish()
////            }
////            builder.setPositiveButton(" Turn Off ") { dialog, which ->
////                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
////            }
////            val alertDialog: AlertDialog = builder.create()
////            alertDialog.show()
////            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#37367C"))
////
////            return
//
//        }else{
//
//            gonextSystem()
//        }

    }

    fun gonextSystem(){

        Handler(Looper.getMainLooper()).postDelayed({
            if (logged){
                val intent = Intent(this@SplashScreen, LanguageSelectActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val editor = sharedPreference.edit()
                editor.putBoolean("isLoggedin", false)
                editor.commit()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 3000)
    }
}