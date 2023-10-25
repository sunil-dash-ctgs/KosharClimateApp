package com.kosherclimate.userapp.updatefarmer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.farmeronboarding.SubmitActivity
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class UpdateTNCActivity : AppCompatActivity() {
    private lateinit var btnNext: Button
    private lateinit var txtCarbonLink: TextView
    private lateinit var checkCarbonCredit: CheckBox

    lateinit var tnc: TextView
    var whichTNC: Int = 0

    private var imageList = ArrayList<String>()
    private var unique_id: String = ""
    private var Pattadhar_number: String = ""
    private var khatian_number: String = ""
    private var survey_number: String = ""
    private var khatha_number: String = ""
    private var relationship: String = ""
    private var patta_number: String = ""
    private var daag_number: String = ""
    //    private var plot_number: String = ""
    private var owner_name: String = ""
    //    private var longitude: String = ""
    private var areaOther: String = ""
    private var areaAcres: String = ""
    //    private var latitude: String = ""
    private var farmerId: String = ""
    private var plot: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_tncactivity)

        tnc = findViewById(R.id.update_tnc)
        btnNext = findViewById(R.id.update_tncNext)
        txtCarbonLink = findViewById(R.id.update_carbon_credit_link)
        checkCarbonCredit = findViewById(R.id.update_carbon_credit_checkBox)

        /**
         * Getting some data's from previous screen.
         */
        val bundle = intent.extras
        if (bundle != null) {
            farmerId = bundle.getString("farmer_unique_id")!!

//            Log.e("plot_number", plot_number)
        } else {
            Log.e("total_plot", "Nope")
        }

        /**
         * Checking if terms & condition is accepted or not.
         */
        checkCarbonCredit.setOnClickListener{
            var checked = checkCarbonCredit.isChecked
            if(checked) {
                Log.e("selected", checked.toString())
                btnNext.visibility = View.VISIBLE
            }
            else{
                Log.e("selected", checked.toString())
                btnNext.visibility = View.GONE
            }
        }


        /**
         * Going to the next screen with so data.
         */
        btnNext.setOnClickListener {
            val intent = Intent(this, UpdateFarmerImageActivity::class.java).apply {
                putExtra("farmer_unique_id", farmerId)

            }
            startActivity(intent)
        }

        getCarbonTNC()
    }

    /**
     * Getting the tnc from API.
     */
    private fun getCarbonTNC() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.tnc().enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.body() != null) {
                    val stringResponse = JSONObject(response.body()!!.string())

                    val carbon_credit = stringResponse.optString("carbon_credit")
                    Log.e("carbon_credit", carbon_credit)

                    tnc.text = HtmlCompat.fromHtml(carbon_credit, HtmlCompat.FROM_HTML_MODE_LEGACY)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("access", it) }
            }
        })
    }
}