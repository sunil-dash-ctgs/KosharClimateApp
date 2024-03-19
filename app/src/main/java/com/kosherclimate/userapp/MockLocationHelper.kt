package com.kosherclimate.userapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi


class MockLocationHelper {

    fun setMockLocationPermission(context: Context, enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val contentResolver = context.contentResolver

            // Check if the app has the "WRITE_SECURE_SETTINGS" permission
            if (Settings.System.canWrite(context)) {
                try {
                    // Enable or disable mock locations
                    Settings.Secure.putInt(
                        contentResolver,
                        Settings.Secure.ALLOW_MOCK_LOCATION,
                        if (enable) 1 else 0
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun promptUserToEnableMockLocations(context: Context) {
        var intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // If the device does not have the specific developer settings activity, open general settings
            intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun isDevMode(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN -> {
                Settings.Secure.getInt(context.contentResolver,
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN -> {
                @Suppress("DEPRECATION")
                Settings.Secure.getInt(context.contentResolver,
                    Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
            }
            else -> false
        }
    }
}