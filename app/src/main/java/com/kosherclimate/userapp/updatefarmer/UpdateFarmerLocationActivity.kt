package com.kosherclimate.userapp.updatefarmer

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.models.DistrictModel
import com.kosherclimate.userapp.models.updatefarmerdetails.UpdatedFarmerLocation
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import com.kosherclimate.userapp.utils.Common
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class UpdateFarmerLocationActivity : AppCompatActivity() {
    private lateinit var progress: SweetAlertDialog

    private lateinit var btnBack: Button
    private lateinit var btnNext: Button

    lateinit var state_spinner: Spinner
    lateinit var district_spinner: Spinner
    lateinit var taluka_spinner: Spinner
    lateinit var village_spinner: Spinner
    lateinit var panchayat_spinner: Spinner

    private lateinit var etRemark: EditText

    var stateIDList = ArrayList<String>()
    var stateNameList = ArrayList<String>()

    var districtIDList = ArrayList<String>()
    var districtNameList = ArrayList<String>()

    var talukaIDList = ArrayList<String>()
    var talukaNameList = ArrayList<String>()

    var panchayatIDList = ArrayList<String>()
    var panchayatNameList = ArrayList<String>()

    var villageIDList = ArrayList<String>()
    var villageNameList = ArrayList<String>()

    var token: String = ""
    var userId: String = ""
    var farmerUniqueId: String = ""
    var state: String = ""
    var stateId: String = ""
    var district: String = ""
    var districtId: String = ""
    var taluka: String = ""
    var talukaId: String = ""
    var panchayat: String = ""
    var panchayatId: String = ""
    var village: String = ""
    var villageId: String = ""

    var baseValue: String = ""
    var baseunit: String = ""

    private var statePosition: Int = 0
    private var districtPosition: Int = 0
    private var talukaPosition: Int = 0
    private var panchayatPosition: Int = 0
    private var villagePosition: Int = 0

    val common: Common = Common()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_farmer_location)

        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!
        userId = sharedPreference.getString("user_id", "")!!
        progress = SweetAlertDialog(this@UpdateFarmerLocationActivity, SweetAlertDialog.PROGRESS_TYPE)
        btnNext = findViewById(R.id.updatedLocation_Next)
        btnBack = findViewById(R.id.updatedLocation_back)

        stateIDList.add("0")
        stateNameList.add("--Select--")

//        districtIDList.add("0")
//        districtNameList.add("--Select--")
//
//        talukaIDList.add("0")
//        talukaNameList.add("--Select--")
//
//        panchayatIDList.add("0")
//        panchayatNameList.add("--Select--")
//
//        villageIDList.add("0")
//        villageNameList.add("--Select--")

        /**
         * Getting some data's from previous screen.
         */

        val bundle = intent.extras
        if (bundle != null) {
            farmerUniqueId = bundle.getString("farmer_unique_id")!!
            state = bundle.getString("state")!!
            stateId = bundle.getString("stateId")!!
            baseValue = bundle.getString("base_value")!!
            baseunit = bundle.getString("base_unit")!!
            stateIDList.add(stateId)
            stateNameList.add(state)
        } else {
            Log.e("NEW_TEST", "Nope")
        }

        state_spinner = findViewById(R.id.updatedState)
        district_spinner = findViewById(R.id.updatedDistrict)
        taluka_spinner = findViewById(R.id.updatedTaluka)
        village_spinner = findViewById(R.id.updatedVillage)
        panchayat_spinner = findViewById(R.id.updatedPanchayat)

        etRemark = findViewById(R.id.updatedRemark)

        btnBack.setOnClickListener {
            finish()
        }

        /**
         * Checking if all required data are present or not after clicking next button.
         */
        btnNext.setOnClickListener {
            val WarningDialog =
                SweetAlertDialog(this@UpdateFarmerLocationActivity, SweetAlertDialog.WARNING_TYPE)

            if (stateNameList.isEmpty() || statePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.state_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (districtNameList.isEmpty() || districtPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.district_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (talukaNameList.isEmpty() || talukaPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.taluka_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (panchayatNameList.isEmpty() || panchayatPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.panchayat_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (villageNameList.isEmpty() || villagePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.village_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else {
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_send)
                progress.setCancelable(false)
                progress.show()

                sendData()
            }
        }

        stateSpinner()
//        getFarmerDetails(farmerUniqueId)
    }

    /**
     * Sending all the data through API
     */

    private fun sendData() {

        val state = stateIDList[statePosition]
        val district = districtIDList[districtPosition]
        val taluka = talukaIDList[talukaPosition]
        val panchayat = panchayatIDList[panchayatPosition]
        val village = villageIDList[villagePosition]
        var remarks = etRemark.text.toString()


        val farmerLocationModel = UpdatedFarmerLocation( farmerUniqueId,  state, district, taluka, panchayat, village, remarks,)

        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.updateFarmerLocation("Bearer $token", farmerLocationModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    val jsonObject = JSONObject(response.body()!!.string())
//                    farmerId = jsonObject.getString("farmerId")
//                    farmerUniqueId = jsonObject.getString("farmerUniqueId")

                    progress.dismiss()
                    nextScreen()

                } else if (response.code() == 500) {
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UpdateFarmerLocationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun nextScreen() {
        progress.dismiss()

        val intent = Intent(this, UpdatePlotDetailsActivity::class.java).apply {
            putExtra("farmer_unique_id",farmerUniqueId)
            putExtra("state_id",stateId)
            putExtra("base_value",baseValue)
            putExtra("base_unit",baseunit)
        }
        startActivity(intent)
    }

    /**  Get Farmer Location Details*/
    private fun getFarmerDetails(farmerUniqueId: String) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        var apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.getFarmerDetails("Bearer $token", "$farmerUniqueId", "2").enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.i("NEW_TEST", "getFarmerDetails Location response >> ${response.code()}")

                if (response.code() == 200) {
                    if (response.body() != null) {
                        var data = JSONObject(response.body()!!.string())
                        val farmerObject = data.getJSONObject("farmer")
                        district = common.getStringFromJSON(farmerObject, "district")
                        districtId = common.getStringFromJSON(farmerObject, "district_id")
                        taluka = common.getStringFromJSON(farmerObject, "taluka")
                        talukaId = common.getStringFromJSON(farmerObject, "taluka_id")
                        panchayat = common.getStringFromJSON(farmerObject, "panchayat")
                        panchayatId = common.getStringFromJSON(farmerObject, "panchayat_id")
                        village = common.getStringFromJSON(farmerObject, "village")
                        villageId = common.getStringFromJSON(farmerObject, "village_id")


                        stateSpinner()
                        state_spinner.setSelection(1)



                        districtSpinner()
                        talukaSpinner()
                        panchayatSpinner()
                        villageSpinner()

//                        if (district.isNotEmpty()) {
//                            districtIDList.add(if (districtId.isEmpty()) "0" else districtId)
//                            districtNameList.add(district)
//                            district_spinner.setSelection(1)
//                        }else{
////                            district_spinner.setSelection(0)
//                        }
//
//                        if (taluka.isNotEmpty()) {
//                            talukaIDList.add(if (talukaId.isEmpty()) "0" else districtId)
//                            talukaNameList.add(taluka)
//
//                            taluka_spinner.setSelection(1)
//                        }else{
////                            taluka_spinner.setSelection(0)
//                        }
//
//                        if (panchayat.isNotEmpty()) {
//                            panchayatIDList.add(if (panchayatId.isEmpty()) "0" else districtId)
//                            panchayatNameList.add(panchayat)
//
//                            panchayat_spinner.setSelection(1)
//                        }else{
////                            panchayat_spinner.setSelection(0)
//                        }
//
//                        if (village.isNotEmpty()) {
//                            villageIDList.add(if (villageId.isEmpty()) "0" else districtId)
//                            villageNameList.add(village)
//
//                            village_spinner.setSelection(1)
//                        }else{
////                            village_spinner.setSelection(0)
//                        }
                        districtAPI()
                    }
                }
                progress.dismiss()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
            }

        })

    }

    private fun stateSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stateNameList)
        state_spinner.adapter = adapter
        state_spinner.setSelection(1)
        state_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                statePosition = position
                Log.e("NEW_TEST", statePosition.toString())

                if (position != 0) {
                   districtAPI()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun districtSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, districtNameList)
        district_spinner.adapter = adapter
        district_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                districtPosition = position
                Log.e("NEW_TEST", districtPosition.toString())

                if (position != 0) {
                       talukaAPI()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun talukaSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, talukaNameList)
        taluka_spinner.adapter = adapter
        taluka_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                talukaPosition = position
                Log.e("NEW_TEST", talukaPosition.toString())

                if (position != 0) {
                   panchayatAPI()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun villageSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, villageNameList)
        village_spinner.adapter = adapter
        village_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                villagePosition = position
                Log.e("NEW_TEST", villagePosition.toString())

                if (position != 0) {
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun panchayatSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, panchayatNameList)
        panchayat_spinner.adapter = adapter
        panchayat_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                panchayatPosition = position
                Log.e("NEW_TEST", panchayatPosition.toString())

                if (position != 0) {
                   villageAPI()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    /**
     * Calling the District API
     */

    private fun districtAPI() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val districtModel = DistrictModel(userId.toInt())

        apiInterface.newDistrict("Bearer $token", districtModel).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                districtIDList.clear()
                districtNameList.clear()
                if (response.body() != null) {
                    districtIDList.add("0")
                    districtNameList.add("--Select--")
                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("district")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id")
                        val name = jsonObject.optString("district")

                        Log.e("NEW_TEST", name)

                        districtIDList.add(id)
                        districtNameList.add(name)
                    }
                    districtSpinner()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UpdateFarmerLocationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Calling the Taluka API
     */

    private fun talukaAPI() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        var districtID: String = districtIDList[districtPosition].toString()

        apiInterface.taluka("Bearer $token", districtID).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                talukaIDList.clear()
                talukaNameList.clear()
                if (response.body() != null) {
                    talukaIDList.add("0")
                    talukaNameList.add("--Select--")
                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("Taluka")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id")
                        val name = jsonObject.optString("taluka")

                        Log.e("name", name)

                        talukaIDList.add(id)
                        talukaNameList.add(name)
                    }
                    talukaSpinner()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UpdateFarmerLocationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }

        })
    }


    /**
     * Calling the Panchayat API
     */

    private fun panchayatAPI() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        var panchayatID: String = talukaIDList[talukaPosition].toString()

        apiInterface.panchayat("Bearer $token", panchayatID).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                panchayatIDList.clear()
                panchayatNameList.clear()

                if (response.body() != null) {
                    panchayatIDList.add("0")
                    panchayatNameList.add("--Select--")
                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("panchayat")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id")
                        val name = jsonObject.optString("panchayat")

                        Log.e("name", name)

                        panchayatIDList.add(id)
                        panchayatNameList.add(name)
                    }
                    panchayatSpinner()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UpdateFarmerLocationActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }

        })
    }

    /**
     * Calling the Village API
     */

    private fun villageAPI() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        var villageID: String = panchayatIDList[panchayatPosition].toString()

        apiInterface.village("Bearer $token", villageID).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                villageIDList.clear()
                villageNameList.clear()

                if (response.body() != null) {
                    villageIDList.add("0")
                    villageNameList.add("--Select--")

                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("Village")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id")
                        val name = jsonObject.optString("village")

                        Log.e("name", name)

                        villageIDList.add(id)
                        villageNameList.add(name)
                    }
                    villageSpinner()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
            }

        })
    }

}