package com.kosherclimate.userapp.farmeronboarding

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.BuildConfig
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.cropintellix.DashboardActivity
import com.kosherclimate.userapp.models.FarmerInfoModel
import com.kosherclimate.userapp.models.MobileVerifyModel
import com.kosherclimate.userapp.models.PlotDetailsModel
import com.kosherclimate.userapp.models.VerifyOtpModel
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import com.kosherclimate.userapp.utils.Common
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class FarmerOnboardingActivity : AppCompatActivity() {
    val access = arrayOf("--Select--", "Own Number", "Relatives Number")
    val plots = arrayOf("--Select--", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20")
    var plotNumberList = ArrayList<String>()
    var plotAreaList = ArrayList<String>()
    var plotList = ArrayList<String>()

    private lateinit var edtFarmerName: EditText
    private lateinit var edtMobile: EditText
//    private lateinit var edtotp: EditText
    private lateinit var edtGuardian: EditText
    private lateinit var edtAadhar: EditText
    private lateinit var txtUniqueID: TextView
//    private lateinit var linearList: LinearLayout
//    private lateinit var otpLAYOUT: LinearLayout
//    private lateinit var plot_spinner: Spinner
    private lateinit var owner_spinner: Spinner
    private lateinit var valid_nonValid: TextView

    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton

    val common: Common = Common()

    var clear: Boolean = false
    var close:  Int = 0
    var bValue: Double = 0.0
    var p: Int = 0
    var i: Int = 1

//    New onboarding Process changes
    private  lateinit var tvCall : TextView
    private  lateinit var tvTotalArea : TextView
    private  lateinit var tvOwnArea : TextView
    private  lateinit var tvLeaseArea : TextView

    private  lateinit var edTotalArea : EditText
    private  lateinit var edOwnArea : EditText
    private  lateinit var edLeaseArea : EditText

    private  lateinit var tvTotalAreaInAcres : TextView
    private  lateinit var tvOwnAreaInAcres : TextView
    private  lateinit var tvLeaseAreaInAcres : TextView
//    New onboarding Process changes

    var realtionshipIDList = java.util.ArrayList<Int>()
    var relationshipNameList = java.util.ArrayList<String>()

    var org: Int = 0
    var state_id: String = ""
    var state: String = ""
    var farmer_name: String = ""
    var guardian: String = ""
    var mobile_access: String = ""
    var relationship: String = ""
    var aadhar: String = ""
    var mobile: Long = 0
    var areaValue: Double? = 0.0
    var unit: String = ""
    var minBValue: Double = 0.0
    var maxBValue: Double = 0.0

    var FarmerId: String = ""
    var FarmerUniqueID: String = ""
    var isOTPvalid: Boolean = false

    var token: String = ""

    var lkjs: Int = 0

    private lateinit var progress: SweetAlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmer_onboarding)

        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        progress = SweetAlertDialog(this@FarmerOnboardingActivity, SweetAlertDialog.PROGRESS_TYPE)


        /** Getting some data's from previous screen.
         * 1. bValue = The base value
         * 2. org = The organization Id.
         * 3. state = The state name.
         * 4. areaValue =
         * 5. unit = The unit that needs to be show. Depends on the state.
         * 6. minBValue = Minimum base value.
         * maxBValue = Maximum base value.
         */


        val bundle = intent.extras
        if (bundle != null) {
            bValue = bundle.getDouble("bValue")
            org = bundle.getInt("org_id")
            state_id = bundle.getString("state_id").toString()
            state = bundle.getString("state").toString()
            areaValue = bundle.getString("area_value")?.toDouble()
            unit = bundle.getString("unit").toString()
            minBValue = bundle.getDouble("min_base_value")
            maxBValue = bundle.getDouble("max_base_value")
        }

        val next_button = findViewById<Button>(R.id.assam_farmer_Next)
        val back_button = findViewById<Button>(R.id.assam_farmer_back)

        val access_spinner = findViewById<Spinner>(R.id.assam_mobile_access)
        owner_spinner = findViewById(R.id.assam_relationship)

        edtFarmerName = findViewById(R.id.assam_farmer_name)
        edtMobile = findViewById(R.id.assam_mobile)
        edtGuardian = findViewById(R.id.assam_guardian_name)
        edtAadhar = findViewById(R.id.aadhar_number)
//        linearList = findViewById(R.id.assam_layout_list)
        txtUniqueID = findViewById(R.id.assam_plot_id)
        valid_nonValid = findViewById(R.id.assam_valid_nonValid)

        radioGroup = findViewById(R.id.assam_radioGroup)

//        New Onboarding Process changes
        tvCall = findViewById(R.id.tvCall)
        tvTotalArea = findViewById(R.id.tvTotalArea)
        tvOwnArea = findViewById(R.id.tvOwnArea)
        tvLeaseArea = findViewById(R.id.tvLeaseArea)

        edTotalArea = findViewById(R.id.edTotalArea)
        edOwnArea = findViewById(R.id.edOwnArea)
        edLeaseArea = findViewById(R.id.edLeaseArea)

        tvTotalAreaInAcres = findViewById(R.id.tvTotalAreaInAcres)
        tvOwnAreaInAcres = findViewById(R.id.tvOwnAreaInAcres)
        tvLeaseAreaInAcres = findViewById(R.id.tvLeaseAreaInAcres)
//        New Onboarding Process changes

        token = sharedPreference.getString("token","")!!


//        New Onboarding Process changes
        if (state.lowercase(Locale.ROOT) == "assam"){
            tvTotalArea.setText("${tvTotalArea.text} Bigha")
            tvOwnArea.setText("${tvOwnArea.text} Bigha")
            tvLeaseArea.setText("${tvLeaseArea.text} Bigha")
        }else{
            tvTotalArea.setText("${tvTotalArea.text} Acres")
            tvOwnArea.setText("${tvOwnArea.text} Acres")
            tvLeaseArea.setText("${tvLeaseArea.text} Acres")
        }

        edTotalArea.addTextChangedListener(object  :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                tvTotalAreaInAcres.setText(acresCalculation(edTotalArea.text.toString()))
            }

        })

        edOwnArea.addTextChangedListener(object  :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                tvOwnAreaInAcres.setText(acresCalculation(edOwnArea.text.toString()))
            }

        })

        edLeaseArea.addTextChangedListener(object  :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                tvLeaseAreaInAcres.setText(acresCalculation(edLeaseArea.text.toString()))
            }

        })
//        New Onboarding Process changes

        /**
         * Getting the current application version
         */
        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        val release = java.lang.Double.parseDouble(java.lang.String(Build.VERSION.RELEASE).replaceAll("(\\d+[.]\\d+)(.*)", "$1"))
        Log.e("version", versionName + versionCode + release.toString())


        /**
         * Assign the adapter to mobile access spinner.
         * Also if index 2nd is selected we enable the Relation ship with owner spinner.
         */
        if (access_spinner != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, access)
            access_spinner.adapter = adapter

            access_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    mobile_access = access[position]
                    Log.e("access", access[position])
                    owner_spinner.isEnabled = position == 2

                    if(position != 2){
                        owner_spinner.setSelection(0)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }




        /**
         * Added a watcher on the mobile number EditText.
         * Also added condition to not allow user enter 0, 1, 2, 3, 4, 5 digits.
         */
        edtMobile.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length == 1 && s.toString().startsWith("0") || s.toString().startsWith("1")
                    || s.toString().startsWith("2")  || s.toString().startsWith("3") || s.toString().startsWith("4")
                    || s.toString().startsWith("5"))
                {
                    s!!.clear()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })






        back_button.setOnClickListener {
            super.onBackPressed()
        }


        /**
         * Checking if all required data are present or not after clicking next button.
         */
        next_button.setOnClickListener {
            val WarningDialog =
                SweetAlertDialog(this@FarmerOnboardingActivity, SweetAlertDialog.WARNING_TYPE)

            if (edtFarmerName.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.farmer_name_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (edtGuardian.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.guardian_name_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (access_spinner.selectedItemPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_access_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (access_spinner.selectedItemPosition == 2 && owner_spinner.selectedItemPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.relationship_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (edtAadhar.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.aadhar_number_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (edtMobile.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_number_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (common.isValid(edtMobile.text.toString()) == false) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_length_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            } else if (edtMobile.text.length < 10) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_length_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }  else if (txtUniqueID.text.isEmpty()) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.missing_farmer_id_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }else {
                plotNumberList.clear()
                plotAreaList.clear()
                plotList.clear()
                sendData()
//                checkIfValidAndRead()
            }
        }

        getUniqueId(versionName)
    }






//    /**
//     * Checking if all the data is present in the area of lands fields.
//     */
//    private fun checkIfValidAndRead() {
//        lkjs = 0
//
//        for (i in 0 until linearList.childCount) {
//            val cricketerView: View = linearList.getChildAt(i)
//
//            val editTextPlotName = cricketerView.findViewById<View>(R.id.assam_plot_number) as TextView
//            val editTextLandArea = cricketerView.findViewById<View>(R.id.assam_area_bigha) as EditText
//            val editTextHectareName = cricketerView.findViewById<View>(R.id.assam_area_hector) as EditText
//
//            if (editTextPlotName.text.toString() != "") {
//                Log.e("PlotName", editTextPlotName.text.toString())
//                plotNumberList.add(editTextPlotName.text.toString())
//            } else {
//                break
//            }
//
//            if (editTextLandArea.text.toString() != "") {
//                Log.e("LandArea", editTextLandArea.text.toString())
//                plotList.add(editTextLandArea.text.toString())
//            }
//            else {
//                break
//            }
//
//            if (editTextHectareName.text.toString() != "") {
//                Log.e("LandAreaAcres", editTextHectareName.text.toString())
//                plotAreaList.add(editTextHectareName.text.toString())
//            }
//            else {
//                break
//            }
//
//
//        }
//
//
//        first@ for (i in 0 until plotList.size) {
//            val plotDouble = plotList[i].toDouble()
//            if (plotDouble < minBValue) {
//                Log.e("sdfkfjs", "fjksdfskdf")
//
//                val WarningDialog = SweetAlertDialog(this@FarmerOnboardingActivity, SweetAlertDialog.WARNING_TYPE)
//
//                WarningDialog.titleText = resources.getString(R.string.warning)
//                WarningDialog.contentText = "${resources.getString(R.string.area_less_warning)}  ${minBValue.toString()}"
//                WarningDialog.confirmText = resources.getString(R.string.ok)
//                WarningDialog.setCancelClickListener {
//                    Log.e("Next Screen", lkjs.toString())
//                    WarningDialog.cancel()
//                }.show()
//
//                lkjs = 1
//                Log.e("Next Screen", lkjs.toString())
//                break@first
//            }
//        }
//
//
//        /**
//         * If all data is present the sending the data through API.
//         */
//        Log.e("Next Screen", lkjs.toString())
//        if (lkjs == 0 && plotList.size == p){
//            Log.e("insidesetdata", "fjksdfskdf")
//            Log.e("plotList", plotList.size.toString())
//            Log.e("plots", p.toString())
//
//            sendData()
//        }
////        else{
////            plotNumberList.clear()
////            plotAreaList.clear()
////            plotList.clear()
////
////            val WarningDialog = SweetAlertDialog(this@AssamFarmerOnboardingActivity, SweetAlertDialog.WARNING_TYPE)
////
////            WarningDialog.titleText = resources.getString(R.string.warning)
////            WarningDialog.contentText = resources.getString(R.string.area_cannot_warning)
////            WarningDialog.confirmText = resources.getString(R.string.ok)
////            WarningDialog.setCancelClickListener {
////                Log.e("Next Screen", lkjs.toString())
////                WarningDialog.cancel()
////            }.show()
////        }
//    }


    /**
     * Sending all the data through API
     */
    private fun sendData() {
        val intSelectButton: Int = radioGroup.checkedRadioButtonId
        radioButton = findViewById(intSelectButton)

        Log.e("PRAMOD", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()

        guardian = edtGuardian.text.toString().trim()
        farmer_name = edtFarmerName.text.toString().trim()
        mobile = edtMobile.text.toString().toLong()
        aadhar = edtAadhar.text.toString()

        val plot_details_arrayList = ArrayList<PlotDetailsModel>()
        var plot: String
        var number: String
        var otherNumber: String

        for (i in 0 until plotList.size) {
            plot = plotAreaList[i]
            otherNumber = plotList[i]
            number = plotNumberList[i]

            val plot_details = PlotDetailsModel(plot, number, otherNumber, unit)
            plot_details_arrayList.add(plot_details)
        }

        if(relationship == "--Select--"){
            relationship = "NA"
        }
        Log.e("relationship", relationship)


        val farmerInfo = FarmerInfoModel(farmer_name, mobile_access, relationship, mobile, txtUniqueID.text.toString(), plotList.size.toString(),
             radioButton.text.toString(), guardian, org, aadhar,
            tvTotalAreaInAcres.text.toString(),tvOwnAreaInAcres.text.toString(),tvLeaseAreaInAcres.text.toString())

        val retIn = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        retIn.farmerInfo("Bearer $token", farmerInfo).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    val jsonObject = JSONObject(response.body()!!.string())
                    FarmerId = jsonObject.getString("FarmerId")
                    FarmerUniqueID = jsonObject.getString("FarmerUniqueID")

                    lkjs == 0
                    nextScreen()
                } else if (response.code() == 500) {
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("PRAMOD", it) }
                progress.dismiss()
            }
        })

    }

    /**
     * If sending data is successful the going to the next screen.
    */
    private fun nextScreen() {
        progress.dismiss()

        val intent = Intent(this, LocationActivity::class.java).apply {
            putExtra("total_plot", plotList.size)
            putExtra("unique_id", txtUniqueID.text)
            putExtra("FarmerId", FarmerId)
            putExtra("area_unit", unit)
            putExtra("area_value", areaValue)
            putExtra("areaAcres", edTotalArea.text.toString())
            putExtra("areaHectare", tvTotalAreaInAcres.text.toString())

            putExtra("state", state)
            putExtra("state_id", state_id)

//            putExtra("total_area", edT)

            Log.e("state_id", state_id)
        }
        startActivity(intent)
    }





    /**
     * Area in acres calculation is don her.
     */
    private fun acresCalculation(acres: String): String {
        return if(acres.isNotEmpty()){
            val value = acres.toDouble()
            val calculated = (value * areaValue!!).toString()
            Log.e("Area Bigha", calculated)
            calculated
        } else{
            "0.0"
        }
    }





    /**
     * Getting Farmer Unique ID after API call.
    */
    private fun getUniqueId(versionCode: String) {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.uniqueID(versionCode.toString()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val id = stringResponse.getString("UniqueId")
                        Log.e("UniqueId", id)

                        txtUniqueID.text = id
                        relationshipAPI()
                    } else {
                        progress.dismiss()
                    }
                } else {
                    progress.dismiss()
                    relationshipAPI()

                    errorDialog()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@FarmerOnboardingActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }


    private fun errorDialog() {
        val WarningDialog = SweetAlertDialog(this@FarmerOnboardingActivity, SweetAlertDialog.WARNING_TYPE)

        WarningDialog.titleText = " Warning "
        WarningDialog.contentText = " Please download new app. "
        WarningDialog.confirmText = " Download Now "
        WarningDialog.showCancelButton(false)
        WarningDialog.setCancelable(false)
        WarningDialog.setConfirmClickListener {

            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()

        }.show()
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
                Toast.makeText(this@FarmerOnboardingActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun relationshipSpinner() {
        if (owner_spinner != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, relationshipNameList)
            owner_spinner.adapter = adapter

            owner_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    relationship = relationshipNameList[position]
                    Log.e("owner_spinner", relationshipNameList[position])
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }
}