package com.kosherclimate.userapp.farmeronboarding

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.models.OrganizationModel
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StateActivity : AppCompatActivity() {
    private lateinit var txtState: TextView
    private lateinit var txtOrganization: TextView
    private lateinit var state_back: Button
    private lateinit var state_next: Button

//    var stateIDList = java.util.ArrayList<Int>()
//    var stateNameList = java.util.ArrayList<String>()
//    var stateUnitList = ArrayList<String>()
//    var stateBaseValueList = ArrayList<String>()

//    var orgIDList = java.util.ArrayList<Int>()
//    var orgNameList = java.util.ArrayList<String>()

    private var stateID: String = ""
    private var orgID: Int = 0
    var bValue: Double = 0.0
    var state: String = ""
    var state_id: String = ""
    var unit: String = ""
    var token: String = ""
    var userId: String = ""
    var areaValue: String = ""
    var maxBValue: String = ""
    var minBValue: String = ""
    private lateinit var progress: SweetAlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_state)

        /**
         * Initializing shared preference.
         * Getting the data from shared preference.
         */
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!
        userId = sharedPreference.getString("user_id","")!!
        stateID = sharedPreference.getString("state_id","")!!

        state_back = findViewById(R.id.state_back)
        state_next = findViewById(R.id.state_Next)
        state_next.visibility = View.GONE
        txtState = findViewById(R.id.onboarding_state)
        txtOrganization = findViewById(R.id.organization)

        progress  = SweetAlertDialog(this@StateActivity, SweetAlertDialog.PROGRESS_TYPE)

        state_back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })


        /**
         * Going to next activity
         */
        state_next.setOnClickListener(View.OnClickListener {
            Log.e("DEBUG_EXC","Max value is $maxBValue")
               try {
                   val intent = Intent(this, FarmerOnboardingActivity::class.java).apply {
                       putExtra("bValue", bValue)
                       putExtra("state", state)
                       putExtra("state_id", stateID)
                       putExtra("org_id", orgID)
                       putExtra("unit", unit)
                       putExtra("area_value", areaValue)
                       putExtra("max_base_value", maxBValue.toDouble())
                       putExtra("min_base_value", minBValue.toDouble())

                       Log.e("state_id", stateID)
                   }
                   startActivity(intent)
               }catch (e:Exception){
                   Log.e("DEBUG_EXC","Exception $this \n$e")
               }
        })

        /**
         * Calling the base value
         */
        baseValue()
    }


    /**
     * Getting the base value, min, max, unit value from API.
     */
    private fun baseValue() {
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = "Loading"
        progress.contentText = "Getting base values"
        progress.setCancelable(false)
        progress.show()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.baseValue("Bearer $token", stateID).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.code() == 200){
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val value = stringResponse.getJSONObject("value")

                        val stateResponse = stringResponse.getJSONObject("state")

                        bValue = value.optDouble("value")
                        state = stateResponse.getString("name")
                        unit = stateResponse.optString("units")
                        areaValue = stateResponse.getString("base_value")
                        minBValue = stateResponse.getString("min_base_value")
                        maxBValue = stateResponse.getString("max_base_value")

                        txtState.text = state
                        orgAPI()
                        state_next.visibility = View.VISIBLE
                    }
                    progress.cancel()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@StateActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
                progress.cancel()
            }
        })
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

                        orgID = id
                        txtOrganization.text = name
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@StateActivity, "Internet Connection Issue", Toast.LENGTH_SHORT).show()
            }
        })
    }
}