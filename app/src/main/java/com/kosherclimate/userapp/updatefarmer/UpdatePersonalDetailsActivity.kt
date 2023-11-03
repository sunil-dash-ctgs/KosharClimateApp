package com.kosherclimate.userapp.updatefarmer

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
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
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.models.FarmerInfoModel
import com.kosherclimate.userapp.models.OrganizationModel
import com.kosherclimate.userapp.models.updatefarmerdetails.UpdatedPersonalDetails
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import com.kosherclimate.userapp.utils.Common
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class UpdatePersonalDetailsActivity : AppCompatActivity() {

    val access = arrayOf("--Select--", "Own Number", "Relatives Number")

    private lateinit var progress: SweetAlertDialog
    lateinit var etFetchFarmer: EditText
    lateinit var searchBtn: ImageView

    var token: String = ""
    var userId: String = ""
    var orgId: String = ""
    var IDList = ArrayList<Int>()
    var FarmerUniqueList = ArrayList<String>()
    val common: Common = Common()
    var farmerUniqueId :String = ""

    lateinit var farmerIdAdapter: Spinner

    private lateinit var tvVerify: TextView
    private lateinit var tvTotalArea: TextView
    private lateinit var tvOwnArea: TextView
    private lateinit var tvLeaseArea: TextView

    private lateinit var etTotalArea: EditText
    private lateinit var etOwnArea: EditText
    private lateinit var etLeaseArea: EditText

    private lateinit var tvTotalAreaInAcres: TextView
    private lateinit var tvOwnAreaInAcres: TextView
    private lateinit var tvLeaseAreaInAcres: TextView

    private lateinit var edtFarmerName: EditText
    private lateinit var edtMobile: EditText
    private lateinit var edtGuardian: EditText
    private lateinit var edtAadhaar: EditText

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    private lateinit var btnBack : Button
    private lateinit var btnNext : Button

    private lateinit var spRelation : Spinner
    private lateinit var spMobileAccess : Spinner

    var realtionshipIDList = java.util.ArrayList<Int>()
    var relationshipNameList = java.util.ArrayList<String>()

    var state :String = "Assam"
    var stateId :String = ""
    var areaValue :Double? = 0.0
    var areaUnit :String = ""

    var farmerDataLoaded:Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_personal_details)

        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!
        userId = sharedPreference.getString("user_id","")!!
        stateId = sharedPreference.getString("state_id","")!!
        Log.i("NEW_TEST", "Token >> $token")

//        Initialise
        etFetchFarmer = findViewById(R.id.etFetch_farmer)
        searchBtn = findViewById(R.id.ivFetch_farmer)
        farmerIdAdapter = findViewById(R.id.spUpdateFarmerId)
        progress = SweetAlertDialog(this@UpdatePersonalDetailsActivity, SweetAlertDialog.PROGRESS_TYPE)
        radioGroup = findViewById(R.id.update_radioGroup)
        tvVerify = findViewById(R.id.updateVerify)
        edtFarmerName = findViewById(R.id.update_farmer_name)
        edtGuardian = findViewById(R.id.update_guardian_name)
        edtAadhaar = findViewById(R.id.updateAadhaar_number)
        edtMobile = findViewById(R.id.update_mobile)
        tvTotalAreaInAcres = findViewById(R.id.tvUpdateTotalAreaInAcres)
        tvOwnAreaInAcres = findViewById(R.id.tvUpdateOwnAreaInAcres)
        tvLeaseAreaInAcres = findViewById(R.id.tvUpdateLeaseAreaInAcres)

        tvTotalArea = findViewById(R.id.tvUpdateTotalArea)
        tvOwnArea = findViewById(R.id.tvUpdateOwnArea)
        tvLeaseArea = findViewById(R.id.tvUpdateLeaseArea)

        tvTotalArea = findViewById(R.id.tvUpdateTotalArea)
        tvOwnArea = findViewById(R.id.tvUpdateOwnArea)
        tvLeaseArea = findViewById(R.id.tvUpdateLeaseArea)

        etTotalArea = findViewById(R.id.edUpdateTotalArea)
        etOwnArea = findViewById(R.id.edUpdateOwnArea)
        etLeaseArea = findViewById(R.id.edUpdateLeaseArea)

        btnBack = findViewById(R.id.update_farmer_back)
        btnNext = findViewById(R.id.update_farmer_Next)

        spRelation = findViewById(R.id.update_relationship)
        spMobileAccess = findViewById(R.id.update_mobile_access)

//      Get base values
        baseValue()
//        Initialise

        /**
         * Search function is here.
         * We search for data from here.
         */
        searchBtn.setOnClickListener {
            if (etFetchFarmer.text.isEmpty()){

            }
            else{
                getPlotUniqueId(etFetchFarmer.text.toString())
            }
        }

        etTotalArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (farmerDataLoaded){
                    tvTotalAreaInAcres.setText(acresCalculation(etTotalArea.text.toString()))
                }
            }

        })

        etOwnArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                tvOwnAreaInAcres.setText(acresCalculation(etOwnArea.text.toString()))
            }

        })

        etLeaseArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                tvLeaseAreaInAcres.setText(acresCalculation(etLeaseArea.text.toString()))
            }

        })

        btnBack.setOnClickListener {
            finish()
        }

        btnNext.setOnClickListener {

            var totalArea: Double? = if (etTotalArea.text.isNullOrEmpty()) {
                0.0
            } else {
                etTotalArea.text.toString().toDouble()
            }

            var ownArea: Double? = if (etOwnArea.text.isNullOrEmpty()) {
                0.0
            } else {
                etOwnArea.text.toString().toDouble()
            }

            var leasesArea: Double? = if (etLeaseArea.text.isNullOrEmpty()) {
                0.0
            } else {
                etLeaseArea.text.toString().toDouble()
            }

            val WarningDialog = SweetAlertDialog(this@UpdatePersonalDetailsActivity, SweetAlertDialog.WARNING_TYPE)
            if (edtFarmerName.text.isEmpty()) {
                warningDialog(WarningDialog,resources.getString(R.string.farmer_name_warning))
            } else if (edtGuardian.text.isEmpty()) {
                warningDialog(WarningDialog,resources.getString(R.string.guardian_name_warning))
            } else if (spMobileAccess.selectedItemPosition == 0) {
                warningDialog(WarningDialog,resources.getString(R.string.mobile_access_warning))
            } else if (spMobileAccess.selectedItemPosition == 2 && spRelation.selectedItemPosition == 0) {
                warningDialog(WarningDialog,resources.getString(R.string.relationship_warning))
            } else if (edtAadhaar.text.isEmpty()) {
                warningDialog(WarningDialog,resources.getString(R.string.aadhar_number_warning))
            } else if (edtMobile.text.isEmpty()) {
                warningDialog(WarningDialog,resources.getString(R.string.mobile_number_warning))
            } else if (common.isValid(edtMobile.text.toString()) == false) {
                warningDialog(WarningDialog,resources.getString(R.string.mobile_length_warning))
            } else if (edtMobile.text.length < 10) {
                warningDialog(WarningDialog,resources.getString(R.string.mobile_length_warning))
            }else if (totalArea == 0.0) {
                Log.e("NEW_TEST", "$totalArea $ownArea $leasesArea")
                warningDialog(WarningDialog,resources.getString(R.string.cannot_be_empty_total_area))
            } else if (totalArea!! < (ownArea!! + leasesArea!!)) {
                Log.e("NEW_TEST", "$totalArea $ownArea $leasesArea")
                warningDialog(WarningDialog,resources.getString(R.string.exeeding_total))
            }else{
                submitData()
            }


        }

        tvVerify.setOnClickListener {
            // cheacking permission
            verifyNumber()
        }

        orgAPI()
    }

/** Show Warning Dialog */
private fun warningDialog(warningDialog: SweetAlertDialog, string: String) {
    warningDialog.titleText = resources.getString(R.string.warning)
    warningDialog.contentText = string
    warningDialog.confirmText = resources.getString(R.string.ok)
    warningDialog.setCancelClickListener { warningDialog.cancel() }.show()
    }

    /** Submit Data to go to Next Screen */
    private fun submitData() {
        val intSelectButton: Int = radioGroup.checkedRadioButtonId
        radioButton = findViewById(intSelectButton)

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()

        var relationship :String = ""
        if (relationshipNameList[spRelation.selectedItemPosition] == "--Select--") {
            relationship = "NA"
        }

        val farmerInfo = UpdatedPersonalDetails(
            farmerUniqueId,
            edtFarmerName.text.toString(),
            access[spMobileAccess.selectedItemPosition],
            relationship,
            edtMobile.text.toString(),
            edtAadhaar.text.toString(),
            radioButton.text.toString(),
            edtGuardian.text.toString(),
            orgId,
            tvTotalAreaInAcres.text.toString(),
            tvOwnAreaInAcres.text.toString(),
            tvLeaseAreaInAcres.text.toString()
            )
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.updateFarmerPersonalDetails("Bearer $token", farmerInfo).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.e("NEW_TEST","updateFarmerPersonalDetails API >> ${response.code()}")
            progress.cancel()
                Log.d("NEW_TEST","Gender >>${radioButton.text}")
                var intent = Intent(this@UpdatePersonalDetailsActivity,UpdateFarmerLocationActivity::class.java)
                intent.putExtra("farmer_unique_id",farmerUniqueId)
                intent.putExtra("state",state)
                intent.putExtra("stateId",stateId)
                intent.putExtra("base_value",areaValue.toString())
                intent.putExtra("base_unit",areaUnit.toString())
                startActivity(intent)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.cancel()
            }
        })

    }

    /** Verify Mobile Number **/
    private fun  verifyNumber(){

    }

    /**
     * After searching the number this fun will be called.
     * The func will give us the list of farmer unique ID.
     */
    private fun  getPlotUniqueId(mobile: String) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        IDList.clear()
        FarmerUniqueList.clear()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.plotUniqueId("Bearer $token", mobile).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        IDList.add(0)
                        FarmerUniqueList.add("--Select--")

                        val stringResponse = JSONObject(response.body()!!.string())
                        Log.i("NEW_TEST","Response Fetch Farmer Id, ${stringResponse}")
                        val jsonArray = stringResponse.optJSONArray("list")

                        if (jsonArray != null) {
                            if (jsonArray.length() == 0) {
                                Log.e("length", jsonArray.length().toString())

                                IDList.clear()
                                FarmerUniqueList.clear()
                                progress.dismiss()

                                val WarningDialog = SweetAlertDialog(this@UpdatePersonalDetailsActivity, SweetAlertDialog.WARNING_TYPE)
                                WarningDialog.titleText = resources.getString(R.string.warning)
                                WarningDialog.contentText = "No data available for\n the given number"
                                WarningDialog.confirmText = " OK "
                                WarningDialog.showCancelButton(false)
                                WarningDialog.setCancelable(false)
                                WarningDialog.setConfirmClickListener {
                                    WarningDialog.cancel()
                                }.show()
                            } else if (jsonArray.length() > 0){
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
                    }
                    else {
                        progress.dismiss()
                    }
                }
                else{
                    Log.e("statusCode", response.code().toString())
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UpdatePersonalDetailsActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun farmerUniqueIdSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, FarmerUniqueList)
        farmerIdAdapter.adapter = adapter
        farmerIdAdapter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                Log.d("NEW_TEST","Postion >> $position")
                if(position != 0){
                    farmerUniqueId = FarmerUniqueList[position]
                    getFarmerDetails(FarmerUniqueList[position])
                }

            }
            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun getFarmerDetails(farmerUniqueId: String) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        var apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.getFarmerDetails("Bearer $token", "$farmerUniqueId","1").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.i("NEW_TEST","getFarmerDetails response >> ${response.code()}")

                if(response.code() == 200){
                    if (response.body() != null){
                        var data = JSONObject(response.body()!!.string())
                        val farmerObject = data.getJSONObject("farmer")
//                        val farmerObject = farmerArray.getJSONObject(0)
                        val farmerUniqueId = common.getStringFromJSON(farmerObject, "farmer_uniqueId")
                        val farmerPlotUniqueId = common.getStringFromJSON(farmerObject, "farmer_plot_uniqueid")
                        val farmerName = common.getStringFromJSON(farmerObject, "farmer_name")
                        val mobileAccess = common.getStringFromJSON(farmerObject, "mobile_access")
                        val mobileRelnOwner = common.getStringFromJSON(farmerObject, "mobile_reln_owner")
                        val mobile = common.getStringFromJSON(farmerObject, "mobile")
                        val gender = common.getStringFromJSON(farmerObject, "gender")
                        val guardianName = common.getStringFromJSON(farmerObject, "guardian_name")
                        val aadhaar = common.getStringFromJSON(farmerObject, "aadhaar")
                        val areaInAcres = common.getStringFromJSON(farmerObject, "area_in_acers")
                        val ownAreaInAcres = common.getStringFromJSON(farmerObject, "own_area_in_acres")
                        val leaseAreaInAcres = common.getStringFromJSON(farmerObject, "lease_area_in_acres")
                        state = common.getStringFromJSON(farmerObject, "state")
                        stateId = common.getStringFromJSON(farmerObject, "state_id")

// Use the extracted values for further processing
                        Log.d("NEW_TEST","Farmer Unique ID: $farmerUniqueId")
                        Log.d("NEW_TEST","Farmer Plot Unique ID: $farmerPlotUniqueId")
                        Log.d("NEW_TEST","Farmer Name: $farmerName")
                        Log.d("NEW_TEST","Mobile Access: $mobileAccess")
                        Log.d("NEW_TEST","Mobile Relationship Owner: $mobileRelnOwner")
                        Log.d("NEW_TEST","Mobile: $mobile")
                        Log.d("NEW_TEST","Gender: $gender")
                        Log.d("NEW_TEST","Guardian Name: $guardianName")
                        Log.d("NEW_TEST","Aadhaar: $aadhaar")
                        Log.d("NEW_TEST","Area in Acres: $areaInAcres")
                        Log.d("NEW_TEST","Own Area in Acres: $ownAreaInAcres")
                        Log.d("NEW_TEST","Lease Area in Acres: $leaseAreaInAcres")

//                        Set values in edittext
                        edtFarmerName.setText(farmerName)
                        edtMobile.setText(mobile.toString())
                        edtAadhaar.setText(aadhaar)
                        edtGuardian.setText(guardianName)
                        etOwnArea.setText(ownAreaInAcres)
                        tvTotalAreaInAcres.text = areaInAcres

                        etTotalArea.setText(acresToBigha(areaInAcres).toString())

                        if (!ownAreaInAcres.isNullOrEmpty()){
                            etOwnArea.setText(acresToBigha(ownAreaInAcres).toString())
                        }

                        if (!leaseAreaInAcres.isNullOrEmpty()){
                            etLeaseArea.setText(acresToBigha(leaseAreaInAcres).toString())
                        }

//                        select gender based on
                        when(gender.lowercase(Locale.ROOT)){
                            "male" -> radioGroup.check(R.id.update_radioMale)
                            "female" -> radioGroup.check(R.id.update_radioFemale)
                            "other" -> radioGroup.check(R.id.update_radioOther)
                            else -> radioGroup.check(R.id.update_radioMale)
                        }

                        when(mobileAccess.lowercase()){
                            "own number" ->  mobileAccessSpinner(1)
                            "relatives number" -> mobileAccessSpinner(2)
                            else -> mobileAccessSpinner(0)
                        }
                        relationshipAPI()
                        if (state.lowercase(Locale.ROOT) == "assam") {
                            tvTotalArea.setText("${tvTotalArea.text} Bigha")
                            tvOwnArea.setText("${tvOwnArea.text} Bigha")
                            tvLeaseArea.setText("${tvLeaseArea.text} Bigha")
                        } else {
                            tvTotalArea.setText("${tvTotalArea.text} Acres")
                            tvOwnArea.setText("${tvOwnArea.text} Acres")
                            tvLeaseArea.setText("${tvLeaseArea.text} Acres")
                        }
                        farmerDataLoaded = true
                    }
                }
                progress.dismiss()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
            }

        })

    }

//    Mobile access Spinner
    private fun mobileAccessSpinner(i: Int) {
    val adapter =
        ArrayAdapter(this, android.R.layout.simple_list_item_1, access)
    spMobileAccess.adapter = adapter
    spMobileAccess.setSelection(i)
    spMobileAccess.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>, view: View, position: Int, id: Long
        ) {
            spRelation.isEnabled = position == 2
            Log.e("NEW_TEST", access[position])
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }
    }



    /**
     * Getting the relationship data from API.
     */
    private fun relationshipAPI() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.relationship("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.body() != null) {
                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("relationshipowner")

                    relationshipNameList.add("--Select--")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("name")

                        realtionshipIDList.add(id)
                        relationshipNameList.add(name)
                    }

                    relationshipSpinner()
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(
                    this@UpdatePersonalDetailsActivity,
                    "Internet Connection Issue",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun relationshipSpinner() {
        if (spRelation != null) {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, relationshipNameList)
            spRelation.adapter = adapter

            spRelation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    Log.e("NEW_TEST", relationshipNameList[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }


    /**
     * Getting the organization data from API.
     */
    private fun orgAPI() {
        val organizationModel = OrganizationModel(userId.toInt())

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.org(organizationModel).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.body() != null) {
                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("list")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("company")

                        orgId = id.toString()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UpdatePersonalDetailsActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Getting the base value, min, max, unit value from API.
     */
    private fun baseValue() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.baseValue("Bearer $token", stateId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val value = stringResponse.getJSONObject("value")

                        val stateResponse = stringResponse.getJSONObject("state")
                        Log.i("NEW_TEST","Base values >> $stringResponse")
//                        bValue = value.optDouble("value")
                        state = stateResponse.getString("name")
                        areaUnit = stateResponse.optString("units")
                        areaValue = stateResponse.getString("base_value").toDouble()
//                        minBValue = stateResponse.getString("min_base_value")
//                        maxBValue = stateResponse.getString("max_base_value")
//
//                        txtState.text = state
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@UpdatePersonalDetailsActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Convert Acer to bigha *****/
    fun acresToBigha(acres: String): Double {
        val value = acres.toDouble() / areaValue!!
        val result = String.format("%.2f", value).toDouble()
        Log.d("NEW_TEST","Convert to bigha >> $acres & $areaValue")
        Log.d("NEW_TEST","Convert to bigha >> $acres & $areaValue = $result")
        return result
    }

    /**
     * Area in acres calculation is don her.
     */
    private fun acresCalculation(acres: String): String {
        return if (acres.isNotEmpty()) {
            val value = acres.toDouble()
            val calculated = (value * areaValue!!).toString()
            Log.e("Area Bigha", calculated)
            calculated
        } else {
            "0.0"
        }
    }
}