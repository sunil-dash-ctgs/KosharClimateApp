package com.kosherclimate.userapp.awd

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.kosherclimate.userapp.network.ApiInterface
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.network.ApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.*


import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.maps.android.PolyUtil
import com.kosherclimate.userapp.TimerData

class AerationMapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var mLastLocation: Location
    private lateinit var mapFragment: SupportMapFragment

    private val MY_PERMISSIONS_REQUEST_LOCATION = 99

    private lateinit var next: FloatingActionButton
    private lateinit var back: ImageView
    private lateinit var farmer_plot_uniqueid: String
    private lateinit var pipe_no: String
    private lateinit var unique_id: String
    private lateinit var aeriation: String
    private lateinit var farmer_name: String
    private lateinit var mobile_number: String
    private var pipe_installation_id: Int = 0
    private var plot_no: Int = 0
    private var xxxxx: Int = 0
    private var token: String = ""
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0

    private var latlngList = ArrayList<LatLng>()
    private var pipeLatitude = 0.0
    private var pipeLongitude = 0.0
    lateinit var polygon: Polygon

    private lateinit var progress: SweetAlertDialog

    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;
    lateinit var text_timer: TextView
    lateinit var selectSeason: String
    lateinit var selectyear: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aeration_map)
        progress = SweetAlertDialog(this@AerationMapActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        back = findViewById(R.id.aeriation_map_back)
        text_timer = findViewById(R.id.text_timer)
        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })

        val bundle = intent.extras
        if (bundle != null) {

            farmer_plot_uniqueid = bundle.getString("farmer_plot_uniqueid").toString()
            pipe_no = bundle.getString("pipe_no")!!
            unique_id = bundle.getString("unique_id")!!
            aeriation = bundle.getString("aeriation")!!
            farmer_name = bundle.getString("farmer_name")!!
            mobile_number = bundle.getString("mobile_number")!!
            pipe_installation_id = bundle.getInt("pipe_installation_id")
            plot_no = bundle.getInt("plot_no")
            StartTime1 = bundle.getInt("StartTime")
            selectSeason = bundle.getString("selectSeason").toString()
            selectyear = bundle.getString("selectyear").toString()

            Log.e("pipe_installation_id", pipe_installation_id.toString())

        }

        timerData = TimerData(this@AerationMapActivity, text_timer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        xxxxx = 1

//        progress.progressHelper.barColor = Color.parseColor("#06c238")
//        progress.titleText = "Checking"
//        progress.contentText = " Checking location !!"
//        progress.setCancelable(false)
//        progress.show()
//        locationReq()

        next = findViewById(R.id.fabNext)
        next.setOnClickListener {
            calculate()
            //getLocationList()
        }


        mapFragment = supportFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@AerationMapActivity)
        getLocationList()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return

            askForPermission()
        }
        else {

            fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                requestNewLocationData()
            }
        }
    }

    private fun askForPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,),
            MY_PERMISSIONS_REQUEST_LOCATION)
    }

    private fun requestNewLocationData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                mapFragment.getMapAsync {
                    var latLng = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

                    Log.e("getCurrentLocation", latLng.latitude.toString() + "-" + latLng.longitude)
                }
            }
        }
        locationReq()
    }

    private fun locationReq() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        var locationRequest = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,0)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(0)
            .setMaxUpdateDelayMillis(5000)
            .build()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        getCurrentLocation()
    }

    private fun getLocationList() {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        apiInterface.aeriationPloygonList("Bearer $token", farmer_plot_uniqueid, pipe_no).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("polygon")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val lat = jsonObject.optString("lat")
                            val lng = jsonObject.optString("lng")

                            var latLng = LatLng(lat.toDouble(), lng.toDouble())
                            latlngList.add(latLng)
                        }

                        val pipeLocationArray =  stringResponse.optJSONObject("PipeLocation")
                        pipeLatitude = pipeLocationArray.getDouble("lat")
                        pipeLongitude = pipeLocationArray.getDouble("lng")

                    }

                    plotPolygons()
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AerationMapActivity, "Please Retry", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun plotPolygons() {
        var polygonOptions = PolygonOptions()

        for (i in latlngList.indices) if (i == 0) {
            polygonOptions = PolygonOptions().add(latlngList[0])
        } else {
            mMap.clear()
            polygonOptions.add(latlngList[i])
            Log.d("polygon123", polygonOptions.toString())
            polygonOptions.strokeColor(Color.BLACK)
            polygonOptions.strokeWidth(5f)
            polygonOptions.fillColor(0x33FF0000)
            polygon = mMap.addPolygon(polygonOptions)
        }

        for (i in 0 until latlngList.size){
            mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(latlngList[i].latitude, latlngList[i].longitude))
                .icon(bitmapDescriptorFromVector(this@AerationMapActivity, R.drawable.ic_location_marker)))
        }

        mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(pipeLatitude, pipeLongitude))
                .icon(bitmapDescriptorFromVector(this@AerationMapActivity, R.drawable.ic_plot_marker)))

    }

    override fun onLocationChanged(location: Location) {

        mLastLocation = location
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.moveCamera(CameraUpdateFactory.zoomIn())
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20f))

    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            Log.e("mLastLocation.latitude", mLastLocation?.latitude.toString())
            Log.e("mLastLocation.longitude", mLastLocation?.longitude.toString())
            currentLatitude = mLastLocation!!.latitude
            currentLongitude = mLastLocation!!.longitude

           // stop()
        }
    }

    private fun stop() {
        Log.e("Stopped", "Location Update Stopped")
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)

        calculate ()
    }

    private fun calculate (){
       val cond = PolyUtil.containsLocation(LatLng(currentLatitude, currentLongitude), latlngList, false)
        if (xxxxx == 1){
            if(cond){
                var latlng = ArrayList<String>()
//
                for (i in 0 until latlngList.size){
                    val lat = latlngList[i].latitude
                    val lng = latlngList[i].longitude
                    val ll = LatLng(lat, lng).toString()

                    latlng.add(ll)
                }
//
                progress.dismiss()
                val intent = Intent(this, AerationImageActivity::class.java).apply {

                    putStringArrayListExtra("latlngList", latlng)
                    putExtra("farmer_plot_uniqueid", farmer_plot_uniqueid)
                    putExtra("latitude", pipeLatitude)
                    putExtra("longitude", pipeLongitude)
                    putExtra("unique_id", unique_id)
                    putExtra("aeriation", aeriation)
                    putExtra("farmer_name", farmer_name)
                    putExtra("mobile_number", mobile_number)
                    putExtra("pipe_no", pipe_no)
                    putExtra("pipe_installation_id", pipe_installation_id)
                    putExtra("plot_no", plot_no)
                    putExtra("StartTime", StartTime)
                    putExtra("selectSeason", selectSeason)
                    putExtra("selectyear", selectyear)


                }
                startActivity(intent)
            }
            else{
                progress.dismiss()

                val WarningDialog = SweetAlertDialog(this@AerationMapActivity, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "You are not inside the marked polygon."
                WarningDialog.confirmText = " OK "
                WarningDialog.showCancelButton(false)
                WarningDialog.setCancelable(false)
                WarningDialog.setConfirmClickListener {
                    WarningDialog.cancel()
                }.show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}