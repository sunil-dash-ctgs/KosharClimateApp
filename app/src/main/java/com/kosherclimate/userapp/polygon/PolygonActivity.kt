package com.kosherclimate.userapp.polygon

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.maps.model.LatLng
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.models.FarmerUniqueIdModel
import com.kosherclimate.userapp.models.existingplots.UniqueIDModel
import com.kosherclimate.userapp.models.updatefarmerdetails.UpdateFarmerAreaModel
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import com.kosherclimate.userapp.polygon.Submitted.PolygonMapSubmittedActivity
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class PolygonActivity : AppCompatActivity() {
    private var Polygon_lat_lng = ArrayList<String>()
    private lateinit var progress: SweetAlertDialog
    lateinit var edtMobile_number: EditText
    lateinit var edtFarmer_name: TextView
    private lateinit var txtArea: EditText
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
    var areaAwdList = ArrayList<String>()
    var FarmerPlotUniqueID = ArrayList<String>()


    private var nextPlot: Int = 0
    private var nextPlotId :String = ""
    private var lastPlotId :String = ""


    private lateinit var btnBack: Button
    private lateinit var btnCaptureData: Button

//    New
    var availableArea :Double = 0.0
    var sumPlotArea :Double = 0.0

    var newTotalArea :Double = 0.0

    var needToUpdate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_polygon)

        progress = SweetAlertDialog(this@PolygonActivity, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!
        Log.e("NEW_TEST", "TOKEN = $token")
        edtMobile_number = findViewById(R.id.pipe_mobile_number)
        edtFarmer_name = findViewById(R.id.pipe_farmer_name)
        txtArea = findViewById(R.id.pipe_plot_area)
        txtArea.isEnabled = false

        plot_ID = findViewById(R.id.pipe_plot_unique_id)
        sub_plot = findViewById(R.id.pope_sub_plot)

        search = findViewById(R.id.pipe_search)

        btnBack = findViewById(R.id.btn_pipe_inst_back)
        btnCaptureData = findViewById(R.id.btn_pipe_inst_captureData)


        btnBack.setOnClickListener {
            backScreen()
        }

        txtArea.addTextChangedListener  (object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                newTotalArea = s.toString().trim().toDouble()
            }

        } )

        btnCaptureData.setOnClickListener {
            val WarningDialog =
                SweetAlertDialog(this@PolygonActivity, SweetAlertDialog.WARNING_TYPE)

            if (edtMobile_number.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_number_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (farmerUniquePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.farmer_unique_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (subPlotUniquePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.sub_plot_unique_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else  if (SUBPLOT == "Create Plot" ) {
                getSubPlot()
                if(lastPlotId == nextPlotId){
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.fill_previous_plot)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                }else{
                    Log.e("NEW_TEST"," Success")
                    if(needToUpdate){
                        if(txtArea.text.toString().isEmpty()){
                            WarningDialog.titleText = resources.getString(R.string.warning)
                            WarningDialog.contentText = "Total Area cannot be 0"
                            WarningDialog.confirmText = resources.getString(R.string.ok)
                            WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                        }else{
                            updateArea()
                        }

                    }else{
                        checkData()
                    }

                }

            }else{
                Log.e("NEW_TEST"," Success 2")
                if(needToUpdate){
                    if(txtArea.text.toString().isEmpty()){
                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = "Total Area cannot be 0"
                        WarningDialog.confirmText = resources.getString(R.string.ok)
                        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                    }else{
                        updateArea()
                    }
                }else{
                 checkData()
                }
            }
        }


        search.setOnClickListener {
            if (edtMobile_number.text.isEmpty()) {

            } else {
                getPlotUniqueId(edtMobile_number.text.toString())
            }
        }

        getThreshold()
    }


    /** Update Farmer Area ***/
    private fun updateArea(){
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()
        Log.i("NEW_TEST","Updating Data ==>${FarmerUniqueList[farmerUniquePosition]} || $newTotalArea || ")
        var newArea = UpdateFarmerAreaModel(
            FarmerUniqueList[farmerUniquePosition],
            newTotalArea.toString(),
            newTotalArea.toString()
        )

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.polygonUpdateFarmerArea("Bearer $token",newArea).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.i("NEW_TEST","Response is ${response.code()}")
                val stringResponse = response.body()?.string()?.let { JSONObject(it) }
                Log.i("NEW_TEST","Response is ${stringResponse}")
                if (response.code() == 200) {
                    if (response.body() != null) {
                      Log.i("NEW_TEST","Response is $stringResponse")

                    }
                    btnCaptureData.text = "Next"
                    needToUpdate = false
                    availableArea = newTotalArea
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
            }
        })
    }
    /** Update Farmer Area ***/

    private fun getThreshold() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.threshold("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
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
        apiInterface.checkPipeData("Bearer $token", plotUniqueIDName, farmerUniqueList, plotNumber)
            .enqueue(object :
                Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val stringResponse = JSONObject(response.body()!!.string())
                            val data = stringResponse.getJSONObject("data")

                            val id = data.optString("id")
                            val farmer_id = data.optString("farmer_id")
                            val farmer_uniqueId = data.optString("farmer_uniqueId")
                            val plot_no = data.optString("plot_no")

                            val locationArray = data.optJSONArray("ranges")
                            for (i in 0 until locationArray.length()) {
                                val jsonObject = locationArray.getJSONObject(i)
                                val lat = jsonObject.optString("lat")
                                val lng = jsonObject.optString("lng")


                                val latLng = LatLng(lat.toDouble(), lng.toDouble())
                                Polygon_lat_lng.add(latLng.toString())
                            }

                            val latitude = data.optString("latitude")
                            val longitude = data.optString("longitude")
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

                            submittedScreen(id, farmer_id, farmer_uniqueId, plot_no, latitude,
                                longitude,
                                state,
                                district,
                                taluka,
                                village,
                                khasara_no,
                                acers_units,
                                plot_area,
                                status,
                                polygon_status,
                                area_in_acers
                            )
                        }
                    } else if (response.code() == 422) {
                        nextScreen()
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
        polygon_status: Int, // for polygon
        area_in_acers: String,
    ) {
//        if (status != 0) {
        val intent = Intent(this, PolygonMapSubmittedActivity::class.java).apply {
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
            putExtra("area", plot_area)
            putStringArrayListExtra("polygon_lat_lng", Polygon_lat_lng)
            putExtra("status", status)
            putExtra("polygon_status", polygon_status)
            putExtra("farmer_name", edtFarmer_name.text.toString())
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
        Log.e("NEW_TEST","Plot area is $PlotArea")
        Log.e("NEW_TEST","Plot area is ${PlotArea[subPlotUniquePosition - 1]}")
        if (txtArea.text.toString().trim().toDouble() == 0.0) {
            val WarningDialog =
                SweetAlertDialog(this@PolygonActivity, SweetAlertDialog.WARNING_TYPE)
            WarningDialog.titleText = resources.getString(R.string.warning)
            WarningDialog.contentText = resources.getString(R.string.area_in_acres_warning)
            WarningDialog.confirmText = resources.getString(R.string.ok)
            WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
        } else {
            val intent = Intent(this, MapActivity::class.java).apply {
//                putExtra("area", PlotArea[subPlotUniquePosition - 1])
                putExtra("area", availableArea.toString())
                putExtra("awd_area", sumPlotArea.toString())
                putExtra("unique_id", FarmerUniqueList[farmerUniquePosition])
                putExtra("sub_plot_no", SubPlotList[subPlotUniquePosition - 1])
                putExtra("farmer_id", IDList[farmerUniquePosition].toString())
                putExtra("farmer_plot_uniqueid", nextPlotId)
                putExtra("farmer_name", edtFarmer_name.text.toString())
                putExtra("threshold", threshold)
//            putExtra("area_acers", )
            }
            startActivity(intent)
        }
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
                        Log.e("NEW_TEST", jsonArray.length().toString())

                        if (jsonArray.length() == 0) {
                            Log.e("NEW_TEST", "got Farmer Lists")
                            Log.e("NEW_TEST", jsonArray.toString())
                            IDList.clear()
                            FarmerUniqueList.clear()

                            progress.dismiss()
                            val WarningDialog = SweetAlertDialog(
                                this@PolygonActivity,
                                SweetAlertDialog.WARNING_TYPE
                            )

                            WarningDialog.titleText = resources.getString(R.string.warning)
                            WarningDialog.contentText = "No data for given \n number"
                            WarningDialog.confirmText = " OK "
                            WarningDialog.showCancelButton(false)
                            WarningDialog.setCancelable(false)
                            WarningDialog.setConfirmClickListener {
                                WarningDialog.cancel()
                            }.show()
                        } else if (jsonArray.length() > 0) {
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
                    } else {
                        progress.dismiss()
                    }
                } else {
                    Log.e("statusCode", response.code().toString())
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
        val plotUniqueIDName: String = FarmerUniqueList[farmerUniquePosition] // "120824"

        SubPlotList.clear()
        PlotArea.clear()
        areaAwdList.clear()
        FarmerPlotUniqueID.clear()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.subPlotId("Bearer $token", plotUniqueIDName).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("plotlist")
                        availableArea = stringResponse.optString("available_area").toDouble()
                        Log.e("NEW_TEST", "got sub plots Lists")
                        Log.e("NEW_TEST", jsonArray.toString())
                        getLastPlotId(jsonArray)
                        FarmerPlotUniqueID.add("--Select--")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val plot_no = jsonObject.optString("plot_no")
                            val farmer_plot_uniqueid = jsonObject.optString("farmer_plot_uniqueid")

                            val jsonApproved = jsonObject.getJSONObject("apprv_farmer_plot")
                            val area_in_acers = jsonObject.optString("area_in_acers")
                            val area_acre_awd = jsonApproved.optString("area_acre_awd")
                            val tmpPlotArea = jsonObject.optString("plot_area").toDouble()
                            sumPlotArea += tmpPlotArea
                            SubPlotList.add(plot_no.toString())
                            areaAwdList.add(if(area_in_acers.trim().toDouble() == 0.0) area_acre_awd else area_in_acers)
                            PlotArea.add(area_in_acers.toString())
                            FarmerPlotUniqueID.add(farmer_plot_uniqueid.toString())
                        }
                        Log.e("NEW_TEST", "Available plot Area $availableArea $sumPlotArea")
                        if (SubPlotList.isNotEmpty()) {
                            SubPlotList.add(SubPlotList.last().toString())
                            PlotArea.add(PlotArea.last().toString())
                            areaAwdList.add(PlotArea.last().toString())
                        }

                        FarmerPlotUniqueID.add("Create Plot")
                        progress.dismiss()
                        Log.e("NEW_TEST", ">>>>>>>>>>>>>>")
                        Log.e("NEW_TEST", FarmerPlotUniqueID.toString())
                        Log.e("NEW_TEST", SubPlotList.toString())
                        Log.e("NEW_TEST", PlotArea.toString())
                        plotSpinner()
                    }
                } else {
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
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                farmerUniquePosition = position
                UNIQURID = FarmerUniqueList[farmerUniquePosition]
                if(position >0){
                    getPlots()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun plotSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, FarmerPlotUniqueID)
        sub_plot.adapter = adapter
        sub_plot.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                subPlotUniquePosition = position
                SUBPLOT = FarmerPlotUniqueID[subPlotUniquePosition]
                if(SUBPLOT == "Create Plot"){
                    lastPlotId = FarmerPlotUniqueID[subPlotUniquePosition -1]
                }
                Log.e("NEW_TEST", "SUb $SUBPLOT $lastPlotId")

                if (position != 0) {
                    println(
                        "????????????????????????? ${
                            "%.4f".format(
                                PlotArea[subPlotUniquePosition - 1].trim().toDouble()
                            )
                        }"
                    )
                    if (PlotArea[subPlotUniquePosition - 1].trim().toDouble() == 0.0 && areaAwdList[subPlotUniquePosition - 1].trim().toDouble() == 0.0){
                        needToUpdate = true
                        btnCaptureData.text = "Update"
                        txtArea.isEnabled = true
                    }else if(PlotArea[subPlotUniquePosition - 1].trim().toDouble() == 0.0){
                        needToUpdate = true
                        btnCaptureData.text = "Update"
                        areaAwdList.forEach {
                            var awd = it.trim().toDouble()
                            newTotalArea += awd
                        }
                        txtArea.setText("%.4f".format(newTotalArea))
                    }else{
                        txtArea.setText("%.4f".format(PlotArea[subPlotUniquePosition - 1].trim().toDouble()))
                    }


                    getFarmerName()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun getFarmerName() {
        var plotUniqueIDName: String = FarmerPlotUniqueID[1]
        Log.e("plotUniqueIDName", plotUniqueIDName)

        val farmerUniqueIdModel = FarmerUniqueIdModel(plotUniqueIDName)
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.farmerPipeDetails("Bearer $token", farmerUniqueIdModel).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonObject1 = stringResponse.getJSONObject("farmer")
                        FarmerId = jsonObject1.optString("id")

                        val jsonObject2 = stringResponse.getJSONObject("farmer")
                        val farmer_name = jsonObject2.optString("farmer_name")

                        edtFarmer_name.text = farmer_name
                        Log.e("NEW_TEST", "SUB Plot is $SUBPLOT")
                            getSubPlot()

                    }
                } else if (response.code() == 422) {
                    val stringResponse = JSONObject(response.errorBody()!!.string())
                    val status = stringResponse.optInt("Status")

                    if (status == 1) {
                        val jsonObject = stringResponse.getJSONObject("farmer")

                        FarmerId = jsonObject.optString("id")
                        Log.e("farmer_id", FarmerId)

                        val farmer_name = jsonObject.optString("farmer_name")
                        edtFarmer_name.text = farmer_name
                    } else if (status == 2) {
                        val msg = stringResponse.getString("message")
                        print(">>>>>>>>>>>>>>>>>>>>>>>>>>>> $msg")
                        val WarningDialog =
                            SweetAlertDialog(this@PolygonActivity, SweetAlertDialog.WARNING_TYPE)

                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = "$msg"
                        WarningDialog.confirmText = " OK "
                        WarningDialog.showCancelButton(false)
                        WarningDialog.setCancelable(false)
                        WarningDialog.setConfirmClickListener {
                            WarningDialog.cancel()

                            backScreen()
                        }.show()
                    } else {
                        val WarningDialog =
                            SweetAlertDialog(this@PolygonActivity, SweetAlertDialog.WARNING_TYPE)

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
                } else {
                    Log.e("statusCode", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
            }
        })
    }

    private fun getSubPlot() {
        Log.e("NEW_TEST", "$UNIQURID")
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val requestBody = JSONObject()
        var uniqueIdModel = UniqueIDModel(UNIQURID)
        requestBody.put("farmer_uniqueId", UNIQURID)
        apiInterface.getPlotId("Bearer $token", UNIQURID)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.e("NEW_TEST", ">>> ${response.code()}")
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val stringResponse = JSONObject(response.body()!!.string())
                            val plotIDD = stringResponse.getString("plot_id")
                    Log.e("NEW_TEST", " >.. $plotIDD")
                            Log.e("NEW_TEST", " >.. $stringResponse")
                            nextPlotId = plotIDD.toString()
                        }
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.message?.let { Log.e("NEW_TEST", it) }
                }

            })

    }

    fun getLastPlotId(jsonArray: JSONArray) {
        val mutableList: MutableList<Int> = mutableListOf(11,2,5,8,4,3)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val plot_no = jsonObject.optString("plot_no")
            mutableList.add(plot_no.trim().toInt())

        }
        val sortedList = mutableList.sorted() // Sorts the list in ascending order
        Log.e("NEW_TEST","NEW_TEST  $mutableList $sortedList")
        Log.e("NEW_TEST","NEW_TEST  $mutableList ${sortedList.last()}")
        nextPlot = sortedList.last() + 1
    }

    private fun backScreen() {
        super.onBackPressed()
        finish()
    }
}