package com.kosherclimate.userapp.pipeinstallation

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.models.FarmerUniqueIdModel
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import com.kosherclimate.userapp.pipeinstallation.Submitted.MapSubmittedActivity
import com.google.android.gms.maps.model.LatLng
import com.kosherclimate.userapp.TimerData
import com.kosherclimate.userapp.polygon.MapActivity
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class PipeActivity : AppCompatActivity() {
    private var Polygon_lat_lng = ArrayList<String>()
    private lateinit var progress: SweetAlertDialog
    lateinit var edtMobile_number: EditText
    lateinit var edtFarmer_name: TextView
    private lateinit var txtArea: TextView
    private lateinit var text_timer: TextView
    private lateinit var search: ImageView

    lateinit var plot_ID: Spinner
    lateinit var sub_plot: Spinner

    var token: String = ""
    var UNIQURID: String = ""
    var SUBPLOT: String = ""
    var FarmerId: String = ""
    var threshold: String = ""
    private var farmerUniquePosition: Int = 0
    private var subPlotUniquePosition: Int = 0

    var IDList = ArrayList<Int>()
    var FarmerUniqueList = ArrayList<String>()
    var SubPlotList = ArrayList<String>()
    var PlotArea = ArrayList<String>()
    var FarmerPlotUniqueID = ArrayList<String>()

    private lateinit var btnBack : Button
    private lateinit var btnCaptureData : Button

    lateinit var timerData: TimerData
    var StartTime = 0
    var StartTime1 = 0

    lateinit var selectyear : String
    lateinit var selectSeason : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pipe)
        progress = SweetAlertDialog(this@PipeActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!

        edtMobile_number = findViewById(R.id.pipe_mobile_number)
        edtFarmer_name = findViewById(R.id.pipe_farmer_name)
        txtArea = findViewById(R.id.pipe_plot_area)

        plot_ID = findViewById(R.id.pipe_plot_unique_id)
        sub_plot = findViewById(R.id.pope_sub_plot)

        search = findViewById(R.id.pipe_search)

        btnBack = findViewById(R.id.btn_pipe_inst_back)
        text_timer = findViewById(R.id.text_timer)
        btnCaptureData = findViewById(R.id.btn_pipe_inst_captureData)

        btnBack.setOnClickListener {
            backScreen()
        }

        val bundle = intent.extras
        if (bundle != null) {
            StartTime1 = bundle.getInt("StartTime")
            selectSeason = bundle.getString("selectSeason").toString()
            selectyear = bundle.getString("selectyear").toString()

            Log.d("userdetailsyear",selectSeason +"  "+selectyear)
        }

        timerData = TimerData(this@PipeActivity, text_timer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        btnCaptureData.setOnClickListener {
            val WarningDialog = SweetAlertDialog(this@PipeActivity, SweetAlertDialog.WARNING_TYPE)

            if (edtMobile_number.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_number_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if(txtArea.text == "0.0"){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.area_in_acres_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (farmerUniquePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.farmer_unique_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else if (subPlotUniquePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.sub_plot_unique_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }
            else {
                checkData()
            }
        }


        search.setOnClickListener{
            if (edtMobile_number.text.isEmpty()){

            }
            else{
                var datalist = edtMobile_number.text.trim().toString()
                getPlotUniqueId(datalist)
            }
        }

        getThreshold()
    }

    private fun getThreshold() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.threshold("Bearer $token").enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200){
                    if(response.body() != null){
                        val stringResponse = JSONObject(response.body()!!.string())
                        threshold = stringResponse.optString("threshold")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    private fun checkData() {
        var plotUniqueIDName: String = FarmerUniqueList[farmerUniquePosition]
        var farmerUniqueList: String = FarmerPlotUniqueID[subPlotUniquePosition]
        var plotNumber: String = SubPlotList[subPlotUniquePosition - 1]

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.checkPipeData("Bearer $token", plotUniqueIDName, farmerUniqueList, plotNumber,selectyear,selectSeason).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){

                    Polygon_lat_lng.clear()

                    if(response.body() != null){
                        val stringResponse = JSONObject(response.body()!!.string())
                        val data = stringResponse.getJSONObject("data")
                        Log.i("NEW_TEST",">>> response dara $data")
                        val id = data.optString("id")
                        val farmer_id = data.optString("farmer_id")
                        val farmer_uniqueId = data.optString("farmer_uniqueId")
                        val plot_no = data.optString("plot_no")
                        Log.i("NEW_TEST",">>> Plot No $plot_no $plotNumber")

                        val locationArray = data.optJSONArray("ranges")
                        for (i in 0 until locationArray.length()) {
                            val jsonObject = locationArray.getJSONObject(i)
                            val lat = jsonObject.optString("lat")
                            val lng = jsonObject.optString("lng")


                            val latLng = LatLng(lat.toDouble(), lng.toDouble())
                            Polygon_lat_lng.add(latLng.toString())
                        }

                        val latitude = data.optString("latitude")
                        val longitude= data.optString("longitude")
                        val state = data.optString("state")
                        val district = data.optString("district")
                        val taluka = data.optString("taluka")
                        val village = data.optString("village")
                        val khasara_no = data.optString("khasara_no")
                        val acers_units = data.optString("acers_units")
                        val plot_area = data.optString("plot_area")
                        val area_in_acers = data.optString("area_in_acers")

                        val status = stringResponse.getInt("status")
                        val polygon_status = stringResponse.getInt("polygon_status")
//                        val polygon_status = 1
                        Log.e("status", status.toString())

                        submittedScreen(id, farmer_id, farmer_uniqueId, plot_no, latitude, longitude, state, district, taluka, village, khasara_no, acers_units, plot_area, status, polygon_status, area_in_acers)

                        edtMobile_number.setText("")
                        txtArea.setText("")
                        edtFarmer_name.setText("")

                        IDList.clear()
                        FarmerUniqueList.clear()
                        SubPlotList.clear()
                        PlotArea.clear()
                        FarmerPlotUniqueID.clear()
                    }
                }
                else if (response.code() == 422){

                    val WarningDialog = SweetAlertDialog(this@PipeActivity, SweetAlertDialog.WARNING_TYPE)
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Fill Polygon First"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                }else if (response.code() == 423){

                    val WarningDialog = SweetAlertDialog(this@PipeActivity, SweetAlertDialog.WARNING_TYPE)
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Pipe Already Submitted"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    private fun submittedScreen(
        id: String,
        farmer_id: String,
        farmer_uniqueId: String,
        plot_no: String,
        latitude: String,
        longitude: String,
        state: String,
        district: String,
        taluka: String,
        village: String,
        khasara_no: String,
        acers_units: String,
        plot_area: String,
        status: Int, // for image
        polygon_status: Int // for polygon
       ,area_in_acers: String,
    ) {
//        if (status != 0) {
            val intent = Intent(this, MapSubmittedActivity::class.java).apply {
                putExtra("farmer_id", farmer_id)
                putExtra("unique_id", farmer_uniqueId)
                putExtra("sub_plot_no", plot_no)
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("state", state)
                putExtra("district", district)
                putExtra("taluka", taluka)
                putExtra("village", village)
                putExtra("khasara_no", khasara_no)
                putExtra("acers_units", acers_units)
                putExtra("area_in_acers", area_in_acers)
                putExtra("area", txtArea.text.toString())
                putStringArrayListExtra("polygon_lat_lng", Polygon_lat_lng)
                putExtra("status", status)
                putExtra("polygon_status", polygon_status)
                putExtra("StartTime", StartTime)
                putExtra("farmer_name", edtFarmer_name.text.toString())
                putExtra("selectSeason", selectSeason)
                putExtra("selectyear", selectyear)
            }
            startActivity(intent)
//        }
//        else{
////            if (polygon_status == 1){
//                val intent = Intent(this, MapSubmittedActivity::class.java).apply {
//                    putExtra("farmer_id", farmer_id)
//                    putExtra("unique_id", farmer_uniqueId)
//                    putExtra("sub_plot_no", plot_no)
//                    putExtra("latitude", latitude)
//                    putExtra("longitude", longitude)
//                    putExtra("state", state)
//                    putExtra("district", district)
//                    putExtra("taluka", taluka)
//                    putExtra("village", village)
//                    putExtra("khasara_no", khasara_no)
//                    putExtra("acers_units", acers_units)
//                    putExtra("area", plot_area)
//                    putStringArrayListExtra("polygon_lat_lng", Polygon_lat_lng)
//                    putExtra("status", status)
//                    putExtra("polygon_status", polygon_status)
//                }
//                startActivity(intent)
//            }
//            else {
//                nextScreen()
//            }
//        }
    }

    private fun nextScreen() {
        val intent = Intent(this, MapActivity::class.java).apply {
            putExtra("area", PlotArea[subPlotUniquePosition - 1])
            putExtra("unique_id", FarmerUniqueList[farmerUniquePosition])
            putExtra("sub_plot_no", SubPlotList[subPlotUniquePosition - 1])
            putExtra("farmer_id",  IDList[farmerUniquePosition].toString())
            putExtra("farmer_plot_uniqueid",  FarmerPlotUniqueID[subPlotUniquePosition])
            putExtra("farmer_name", edtFarmer_name.text.toString())
            putExtra("threshold", threshold)
            putExtra("selectSeason", selectSeason)
            putExtra("selectyear", selectyear)
//            putExtra("area_acers", )
        }
        startActivity(intent)
    }

    private fun getPlotUniqueId(mobile: String) {
        FarmerUniqueList.clear()
        IDList.clear()

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()


        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.plotUniqueId("Bearer $token", mobile).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        IDList.add(0)
                        FarmerUniqueList.add("--Select--")

                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("list")

                        if (jsonArray.length() == 0) {
                            Log.e("length", jsonArray.length().toString())

                            IDList.clear()
                            FarmerUniqueList.clear()

                            progress.dismiss()
                            val WarningDialog = SweetAlertDialog(this@PipeActivity, SweetAlertDialog.WARNING_TYPE)

                            WarningDialog.titleText = resources.getString(R.string.warning)
                            WarningDialog.contentText = "No data for given \n number"
                            WarningDialog.confirmText = " OK "
                            WarningDialog.showCancelButton(false)
                            WarningDialog.setCancelable(false)
                            WarningDialog.setConfirmClickListener {
                                WarningDialog.cancel()
                            }.show()
                        }
                        else if (jsonArray.length() > 0){
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                val id = jsonObject.optString("id").toInt()
                                val farmer_uniqueId = jsonObject.optString("farmer_uniqueId")

                                IDList.add(id)
                                FarmerUniqueList.add(farmer_uniqueId)
                            }

                            farmerUniqueIdSpinner()
                            progress.dismiss()
                        }
                    }
                    else {
                        progress.dismiss()
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
                progress.dismiss()
            }
        })
    }

    private fun getPlots() {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        val plotUniqueID: String = IDList[farmerUniquePosition].toString()
        val plotUniqueIDName: String =   FarmerUniqueList[farmerUniquePosition] // "120824"

        SubPlotList.clear()
        PlotArea.clear()
        FarmerPlotUniqueID.clear()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.subPlotId("Bearer $token", plotUniqueIDName).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("plotlist")

                        FarmerPlotUniqueID.add("--Select--")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val plotArea = jsonObject.optString("plot_area")
                            val plot_no = jsonObject.optString("plot_no")
                            val farmer_plot_uniqueid = jsonObject.optString("farmer_plot_uniqueid")

                            val jsonApproved = jsonObject.getJSONObject("apprv_farmer_plot")
                            val area_in_acers = jsonApproved.optString("area_acre_awd")

                            SubPlotList.add(plot_no.toString())
                            PlotArea.add(plotArea.toString())
                            FarmerPlotUniqueID.add(farmer_plot_uniqueid.toString())
                        }
                        progress.dismiss()
                        plotSpinner()
                    }
                } else if(response.code() == 422){
                    try {
                        val errorResponse = JSONObject(response.errorBody()?.string() ?: "")
                        val message = errorResponse.optString("message")

                        Log.e("NEW_TEST", "HTTP message in 422: $message")

                        val warningDialog = SweetAlertDialog(this@PipeActivity, SweetAlertDialog.WARNING_TYPE)
                        warningDialog.titleText = resources.getString(R.string.warning)
                        warningDialog.contentText = message
                        warningDialog.confirmText = " OK "
                        warningDialog.showCancelButton(false)
                        warningDialog.setCancelable(false)
                        warningDialog.setConfirmClickListener {
                            warningDialog.cancel()
                            backScreen()
                        }.show()

                    } catch (e: JSONException) {
                        // Handle JSON parsing error
                        Log.e("NEW_TEST", "Error parsing JSON in 422 response", e)
                    }
                    progress.dismiss()
                }else{
                    Log.e("statusCode", response.code().toString())
                    progress.dismiss()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
                progress.dismiss()
            }
        })
    }


    private fun farmerUniqueIdSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, FarmerUniqueList)
        plot_ID.adapter = adapter
        plot_ID.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                farmerUniquePosition = position
                UNIQURID = FarmerUniqueList[farmerUniquePosition]

                getPlots()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun plotSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, FarmerPlotUniqueID)
        sub_plot.adapter = adapter
        sub_plot.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                subPlotUniquePosition = position
                SUBPLOT = FarmerPlotUniqueID[subPlotUniquePosition]

                if(position !=0){
                    Log.e("NEW_TEST",",,,,, ${PlotArea}")
                    Log.e("NEW_TEST",",,,,, ${subPlotUniquePosition }")
                    Log.e("NEW_TEST",",,,,, ${PlotArea[0]}");
                    Log.e("NEW_TEST",",,,,, ${"%.4f".format(PlotArea[0].trim().toDouble())} $subPlotUniquePosition");
                    Log.e("NEW_TEST",",,,,, ${PlotArea[subPlotUniquePosition-1]}");
                    txtArea.text =  "%.4f".format(PlotArea[subPlotUniquePosition-1].trim().toDouble())
                    getFarmerName()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun getFarmerName() {
        var plotUniqueIDName: String = FarmerPlotUniqueID[subPlotUniquePosition]
        Log.e("plotUniqueIDName", plotUniqueIDName)

        val farmerUniqueIdModel = FarmerUniqueIdModel(plotUniqueIDName)
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.farmerPipeDetails("Bearer $token", farmerUniqueIdModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonObject1 = stringResponse.getJSONObject("farmer")
                        FarmerId = jsonObject1.optString("id")

                        val jsonObject2 = stringResponse.getJSONObject("farmer")
                        val farmer_name = jsonObject2.optString("farmer_name")

                        edtFarmer_name.text = farmer_name
                    }
                }
                else if(response.code() == 422) {
                    val stringResponse = JSONObject(response.errorBody()!!.string())
                    val status = stringResponse.optInt("Status")

                    if (status == 1) {
                        val jsonObject = stringResponse.getJSONObject("farmer")

                        FarmerId = jsonObject.optString("id")
                        Log.e("farmer_id", FarmerId)

                        val farmer_name = jsonObject.optString("farmer_name")
                        edtFarmer_name.text = farmer_name
                    }else if (status == 2) {
                        val msg = stringResponse.getString("message")
                        print(">>>>>>>>>>>>>>>>>>>>>>>>>>>> $msg")
                        val WarningDialog = SweetAlertDialog(this@PipeActivity, SweetAlertDialog.WARNING_TYPE)

                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = "$msg"
                        WarningDialog.confirmText = " OK "
                        WarningDialog.showCancelButton(false)
                        WarningDialog.setCancelable(false)
                        WarningDialog.setConfirmClickListener {
                            WarningDialog.cancel()

                            backScreen()
                        }.show()
                    }
                    else {
                        val WarningDialog =
                            SweetAlertDialog(this@PipeActivity, SweetAlertDialog.WARNING_TYPE)

                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = "Enter Crop Data First"
                        WarningDialog.confirmText = " OK "
                        WarningDialog.showCancelButton(false)
                        WarningDialog.setCancelable(false)
                        WarningDialog.setConfirmClickListener {
                            WarningDialog.cancel()

                            backScreen()
                        }.show()
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
            }
        })
    }

    private fun backScreen() {
        super.onBackPressed()
        finish()
    }

    override fun onStart() {
        super.onStart()

        edtMobile_number.setText("")
        txtArea.setText("")
        edtFarmer_name.setText("")

        IDList.clear()
        FarmerUniqueList.clear()
        SubPlotList.clear()
        PlotArea.clear()
        FarmerPlotUniqueID.clear()

        plot_ID.setAdapter(null)
        sub_plot.setAdapter(null)
    }
}