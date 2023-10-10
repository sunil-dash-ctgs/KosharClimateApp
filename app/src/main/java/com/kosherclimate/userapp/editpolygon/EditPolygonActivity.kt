package com.kosherclimate.userapp.editpolygon

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.polygon.LandInfoActivity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar

class EditPolygonActivity : AppCompatActivity() , OnMapReadyCallback, LocationListener {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationPermissionRequestCode = 1001
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    private var Polygon_lat_lng = ArrayList<String>()
    private val latLngslist = java.util.ArrayList<LatLng>()

    var handler: Handler = Handler()
    var runnable: Runnable? = null

    private var LAT = ArrayList<Double>()
    private var LNG = ArrayList<Double>()
    private var area: String = ""
    private var sumPlotArea: String = ""
    private var farmer_name: String = ""
    private var area_in_acers: String = ""
    private var unique_id: String = ""
    private var sub_plot_no: String = ""
    private var farmer_id: String = ""
    private var latitude: String = ""
    private var longitude: String = ""
    private var state: String = ""
    private var district: String = ""
    private var taluka: String = ""
    private var village: String = ""
    private var khasara_no: String = ""
    private var acers_units: String = ""
    private var imageLat: String = ""
    private var imageLng: String = ""

    private  var polygonArea:Double = 0.0

    private var markerIds = ArrayList<String>()

    private var status: Int = 0
    private var polygon_status: Int = 0

    lateinit var polygon: Polygon

    lateinit var save: ImageView
    lateinit var back: ImageView
    lateinit var txtArea: TextView
    lateinit var tvTotalArea: TextView

    var threshold: String = ""
    private var polygon_date_time: String = ""
    private var farmer_plot_uniqueid: String = ""

//    Drageable marker
private val markerList: java.util.ArrayList<Marker> = java.util.ArrayList()
    private var mCurrLocationMarker: Marker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_polygon)

        val bundle = intent.extras
        if (bundle != null) {

//            unique_id = bundle.getString("unique_id")!!
//            sub_plot_no = bundle.getString("sub_plot_no")!!
//            farmer_id = bundle.getString("farmer_id")!!
            latitude = bundle.getString("latitude")!!
            longitude = bundle.getString("longitude")!!
            area = bundle.getString("area")!!
            sumPlotArea = bundle.getString("awd_area")!!
            unique_id = bundle.getString("unique_id")!!
            sub_plot_no = bundle.getString("sub_plot_no")!!
            farmer_id = bundle.getString("farmer_id")!!
            farmer_plot_uniqueid = bundle.getString("farmer_plot_uniqueid")!!
            threshold = bundle.getString("threshold")!!
            farmer_name = bundle.getString("farmer_name")!!

            Polygon_lat_lng = bundle.getStringArrayList("polygon_lat_lng")!!
//            status = bundle.getInt("status")
//            polygon_status = bundle.getInt("polygon_status")
            Log.e("NEW_TEST", Polygon_lat_lng.toString())
        } else {
            Log.e("area", "Nope")
        }

        save = findViewById(R.id.editPolySaveLocation)
        back = findViewById(R.id.editPolyBack)
        txtArea = findViewById(R.id.edit_polygon_area)
        tvTotalArea = findViewById(R.id.editPoly_area_acres)

        tvTotalArea.text = area
        val c: Calendar = Calendar.getInstance()
        val dfi = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formattedDate: String = dfi.format(c.time)
        polygon_date_time = formattedDate


//        onsaved clicked Licker
        save.setOnClickListener {
            var maxValue = area.trim().toDouble();

            if(polygonArea > maxValue){
                val WarningDialog =
                    SweetAlertDialog(this@EditPolygonActivity, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Area drawn is more than plot \n area"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }else {
                val stringList = convertLatLngListToStringList(latLngslist)
                val intent = Intent(this, LandInfoActivity::class.java).apply {
                    putExtra("locationList", stringList)
                    putExtra("area", area)
                    putExtra("awd_area", sumPlotArea)
                    putExtra("unique_id", unique_id)
                    putExtra("sub_plot_no", sub_plot_no)
                    putExtra("farmer_id", farmer_id)
                    putExtra("polygon_area", polygonArea)
                    putExtra("farmer_plot_uniqueid", farmer_plot_uniqueid)
                    putExtra("polygon_date_time", polygon_date_time)
                    putExtra("farmer_name", farmer_name)
                }
                startActivity(intent)
                finish()
            }
        }

        back.setOnClickListener {
            mMap.clear()
            runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible
            super.onBackPressed()
            finish()
        }

        mapFragment = supportFragmentManager.findFragmentById(R.id.editPolyMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@EditPolygonActivity)
        getCurrentLocation()

    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                mapFragment.getMapAsync {
                    val dfgdg: String =  Polygon_lat_lng[0].replace("[^0-9,.-]".toRegex(), "")
                    val lati: Double = dfgdg.split(",").first().toDouble()
                    val longi: Double = dfgdg.split(",").last().toDouble()
                    val latLng = LatLng(lati, longi)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f))

//                    imageLat = latLng.latitude.toString()
//                    imageLng = latLng.longitude.toString()
//                    Log.e("getCurrentLocation", "$imageLat-$imageLng")
                }
            }
        }
    }

//*** **/
    fun updatePolygon(){
        markerList.clear()
        markerIds.clear()
        var polygonOptions = PolygonOptions()
        for (i in latLngslist.indices) if (i == 0) {
            Log.e("NEW_TEST","indiesc $i")
            polygonOptions = PolygonOptions().add(latLngslist[0])
        } else {
            Log.e("NEW_TEST","indiesc $i")
            mMap.clear()
            polygonOptions.add(latLngslist[i])
            Log.d("polygon123", polygonOptions.toString())
            polygonOptions.strokeColor(Color.BLACK)
            polygonOptions.strokeWidth(5f)
            polygonOptions.fillColor(0x33FF0000)
            polygon = mMap.addPolygon(polygonOptions)
        }

        for (i in 0 until LAT.size){
            mCurrLocationMarker =   mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(latLngslist[i]).draggable(true))
            mCurrLocationMarker?.let { markerList.add(it) }
            mCurrLocationMarker?.let { markerIds.add(it.id) }
        }

    polygonArea = calculatePolygonAreaInAcres(latLngslist)
    txtArea.setText("$polygonArea acers")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        for (i in 0 until Polygon_lat_lng.size){
            val dfgdg: String =  Polygon_lat_lng[i].replace("[^0-9,.-]".toRegex(), "")

            val lat: Double = dfgdg.split(",").first().toDouble()
            val lng: Double = dfgdg.split(",").last().toDouble()
            latLngslist.add(LatLng(lat, lng))

            LAT.add(lat)
            LNG.add(lng)
        }
        var polygonOptions = PolygonOptions()
        for (i in latLngslist.indices) if (i == 0) {
            Log.e("NEW_TEST","indiesc $i")
            polygonOptions = PolygonOptions().add(latLngslist[0])
        } else {
            Log.e("NEW_TEST","indiesc $i")
            mMap.clear()
            polygonOptions.add(latLngslist[i])
            Log.d("polygon123", polygonOptions.toString())
            polygonOptions.strokeColor(Color.BLACK)
            polygonOptions.strokeWidth(5f)
            polygonOptions.fillColor(0x33FF0000)
            polygon = mMap.addPolygon(polygonOptions)
        }

        for (i in 0 until LAT.size){
          mCurrLocationMarker =   mMap.addMarker(MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(LAT[i], LNG[i])).draggable(true))
            mCurrLocationMarker?.let { markerList.add(it) }
            mCurrLocationMarker?.let { markerIds.add(it.id) }
        }

        polygonArea = calculatePolygonAreaInAcres(latLngslist)
        txtArea.text = "$polygonArea acers"

//        Marker Drag
mMap.setOnMarkerDragListener(object : OnMarkerDragListener{
    override fun onMarkerDrag(p0: Marker) {
    }

    override fun onMarkerDragEnd(marker: Marker) {
try {
    Log.e("NEW_TEST","MARKER DRAGING ${marker.id}")
    Log.e("NEW_TEST","MARKER DRAGING ${marker.position}")
    var contain = markerIds.contains(marker.id)
    var index = markerIds.indexOf(marker.id)
    latLngslist[index] = marker.position
    Log.e("NEW_TEST","MARKER DRAGING ${markerIds}")
    Log.e("NEW_TEST","MARKER DRAGING ${contain} $index ${latLngslist.indices}")
    updatePolygon()
}catch (e:Exception){
    Log.e("NEW_TEST", "ERROR while updating $e")
}

    }

    override fun onMarkerDragStart(p0: Marker) {
    }

})
//        Marker drag

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.setMinZoomPreference(15f)
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
    }

    // Function to update the polygon's vertices based on marker positions
    private fun updatePolygonVertices() {
        val updatedVertices = mutableListOf<LatLng>()
        for (marker in markerList) {
            updatedVertices.add(marker.position)
        }
        polygon?.points = updatedVertices
    }


//    Change Location Fun
    override fun onLocationChanged(location: Location) {
        val df = DecimalFormat("#.#####")
        imageLat = df.format(location.latitude).toString()
        imageLng = df.format(location.longitude).toString()
    }

    fun calculatePolygonArea(coordinates: List<LatLng>): Double {
        if (coordinates.size < 3) {
            // A polygon with less than 3 vertices isn't valid
            return 0.0
        }

        var area = 0.0
        var j = coordinates.size - 1

        for (i in coordinates.indices) {
            val xi = coordinates[i].latitude
            val xj = coordinates[j].latitude
            val yi = coordinates[i].longitude
            val yj = coordinates[j].longitude

            area += (xi + xj) * (yj - yi)
            j = i
        }

        Log.e("NEW_TEST", "AREAA ${Math.abs(area / 2.0)}")
        return Math.abs(area / 2.0)
    }

//    Calculate Area in acres
    fun calculatePolygonAreaInAcres(coordinates: List<LatLng>): Double {
    // Calculating meters from polygon list
    val m = SphericalUtil.computeArea(coordinates)
    Log.e("NEW_TEST", "computeArea $m")
    Log.e("NEW_TEST", "computeArea $coordinates")

// converting meters to acers
    val df = DecimalFormat("#.#####")
    var polygon_area = df.format(m * 0.000247105).toDouble()
    Log.e("a", "computeArea $polygon_area")
//        if (coordinates.size < 3) {
//            // A polygon with less than 3 vertices isn't valid
//            return 0.0
//        }
//
//        val sqMetersArea = calculatePolygonArea(coordinates) // Calculate area in square meters
//
//        // Convert square meters to acres (1 acre = 4046.86 square meters)
//        val acresArea = (sqMetersArea / 4046.86).toString()
//    val formattedArea = "${acresArea[0]}${acresArea[1]}${acresArea[2]}${acresArea[3]}${acresArea[4]}${acresArea[5]}"
//    Log.e("NEW_TEST", "AREAA Acres ${acresArea}")
//    Log.e("NEW_TEST", "AREAA Acres ${formattedArea}")
        return polygon_area.toDouble()
    }

//    convert latlong List to string list
fun convertLatLngListToStringList(latlngList: java.util.ArrayList<LatLng>): java.util.ArrayList<String> {
    val stringList = java.util.ArrayList<String>()

    // Map each LatLng object to its string representation
    for (latlng in latlngList) {
        val lat = latlng.latitude
        val lng = latlng.longitude
        val latlngString = "$lat,$lng"
        stringList.add(latlngString)
    }

    return stringList
}
}