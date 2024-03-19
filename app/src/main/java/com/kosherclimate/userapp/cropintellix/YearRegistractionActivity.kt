package com.kosherclimate.userapp.cropintellix

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.TimerData
import com.kosherclimate.userapp.awd.AerationActivity
import com.kosherclimate.userapp.cropdata.CropActivity
import com.kosherclimate.userapp.farmerbenefit.FarmerBenefitActivity
import com.kosherclimate.userapp.farmeronboarding.StateActivity
import com.kosherclimate.userapp.models.DataYear
import com.kosherclimate.userapp.network.ApiClient
import com.kosherclimate.userapp.network.ApiInterface
import com.kosherclimate.userapp.pipeinstallation.PipeActivity
import com.kosherclimate.userapp.polygon.PolygonActivity
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class YearRegistractionActivity : AppCompatActivity() {

    lateinit var showCurrentMonth: TextView
    lateinit var currentMonth: String
    lateinit var currentMonth_Name: String
    val yesr = arrayOf("--Select Year--", "2023-24")
    val session = arrayOf("--Select Season--", "rabi", "kharif")
    lateinit var SelectYear_spinner: Spinner
    lateinit var SelectSession_spinner: Spinner
    lateinit var claseeName: String
    lateinit var token: String
    lateinit var pagename: String
    lateinit var button_next: Button
    lateinit var button_back: Button
    lateinit var text_timer: TextView
    lateinit var setdatayear: TextView

    lateinit var timerData: TimerData
    var StartTime = 0;
    var AcresList = ArrayList<String>()
    var update = ArrayList<String>()
    private var end_of_date: Int = 0
    private var preparation_date_interval: Int = 0
    private var transplantation_date_interval: Int = 0
    var str_name = ArrayList<String>()
    var str_year = ArrayList<String>()
    private var namePosition: Int = 0
    private var yearPosition: Int = 0
    lateinit var selectyear : Any
    lateinit var selectSeason : Any

    private val PREF_NAME = "sharedcheckLogin"
    private lateinit var language: String
    private lateinit var locale: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_year_registraction)

        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!


        val bundle = intent.extras
        if (bundle != null) {
            claseeName = bundle.getString("farmer_classname").toString()
            language = bundle.getString("language").toString()
            pagename = bundle.getString("language").toString()

            if (claseeName.equals("farmer_cropinfo")) {
                AcresList = bundle.getStringArrayList("plot_area")!!
                end_of_date = bundle.getInt("cropdata_end_days")
                preparation_date_interval = bundle.getInt("preparation_date_interval")
                transplantation_date_interval = bundle.getInt("cropdata_end_days")
            }
        }else {
            language = "en"
            Log.e("data", "No bundle data")
        }

        locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)


        showCurrentMonth = findViewById(R.id.showCurrentMonth)
        SelectSession_spinner = findViewById(R.id.SelectSession)
        SelectYear_spinner = findViewById(R.id.SelectYear)
        button_next = findViewById(R.id.assam_farmer_Next)
        button_back = findViewById(R.id.assam_farmer_back)
        text_timer = findViewById(R.id.text_timer)
        setdatayear = findViewById(R.id.setdatayear)

        timerData = TimerData(this@YearRegistractionActivity, text_timer)
        StartTime = timerData.startTime(0).toInt()

        val dateFormat: DateFormat = SimpleDateFormat("MM")
        val date = Date()
        currentMonth = dateFormat.format(date)
        Log.d("Month", dateFormat.format(date))

        val cal: Calendar = Calendar.getInstance()
      //  cal.add(Calendar.MONTH, 4);
        val month_date = SimpleDateFormat("MMMM")
       // currentMonth = dateFormat.format(date)
        currentMonth_Name = month_date.format(cal.getTime())

        showCurrentMonth.text = currentMonth_Name + "   " + "(" + currentMonth + ")"

        getDataYear(currentMonth)

//        val adapter_year = ArrayAdapter(this, android.R.layout.simple_list_item_1, yesr)
//        SelectYear_spinner.adapter = adapter_year
        SelectYear_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    yearPosition = position
                    selectyear = parent?.getItemAtPosition(position)
                    Log.d("selected item",selectyear.toString())
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

//        val adapter_session = ArrayAdapter(this, android.R.layout.simple_list_item_1, session)
//        SelectSession_spinner.adapter = adapter_session
        SelectSession_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    namePosition = position
                    selectSeason = parent.getItemAtPosition(position)
                    println(selectSeason.toString())
                    Log.d("selected item1",selectyear.toString())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        if (claseeName.equals("farmer_onboarding")){
            setdatayear.setText(getResources().getString(R.string.Data_year) +" ( "+ getResources().getString(R.string.farmer_onboarding)+" )");
        }else if (claseeName.equals("farmer_Benefit")){
            setdatayear.setText(getResources().getString(R.string.Data_year) +" ( "+ getResources().getString(R.string.farmer_benefits_dashboard)+" )");
        }else if (claseeName.equals("framer_Polygon")){
            setdatayear.setText(getResources().getString(R.string.Data_year) +" ( "+ getResources().getString(R.string.polygon)+" )");
        }else if (claseeName.equals("framer_Pipe")){
            setdatayear.setText(getResources().getString(R.string.Data_year) +" ( "+ getResources().getString(R.string.pipe_installation)+" )");
        }else if (claseeName.equals("framer_aeration")){
            setdatayear.setText(getResources().getString(R.string.Data_year) +" ( "+ getResources().getString(R.string.capture_aeration_event)+" )");
        }else if (claseeName.equals("farmer_cropinfo")){
            setdatayear.setText(getResources().getString(R.string.Data_year) +" ( "+ getResources().getString(R.string.crop_data)+" )");
        }

        button_next.setOnClickListener {

            val WarningDialog =
                SweetAlertDialog(this@YearRegistractionActivity, SweetAlertDialog.WARNING_TYPE)

            if (yearPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Your Year"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    WarningDialog.cancel()
                }.show()

            } else if (namePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Your Season"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    WarningDialog.cancel()
                }.show()

            } else if (claseeName.equals("farmer_onboarding")) {
                val intent = Intent(this, StateActivity::class.java)
                intent.putExtra("StartTime", StartTime)
                intent.putExtra("selectSeason", selectSeason.toString())
                intent.putExtra("selectyear", selectyear.toString())
                intent.putExtra("StartTime", StartTime)
                startActivity(intent)

                var sharedprefernce: SharedPreferences = getSharedPreferences("farmer_onboarding", 0)
                var editor: SharedPreferences.Editor = sharedprefernce.edit()
                editor.putString("selectSeason", selectSeason.toString()); // Storing string
                editor.putString("selectyear", selectyear.toString());
                editor.commit(); // commit changes// Storing string


            } else if (claseeName.equals("farmer_Benefit")) {
                val intent = Intent(this, FarmerBenefitActivity::class.java).apply { }
                intent.putExtra("StartTime", StartTime)
                startActivity(intent)
            } else if (claseeName.equals("framer_Polygon")) {
                val intent = Intent(this, PolygonActivity::class.java)
                intent.putExtra("StartTime", StartTime)
                startActivity(intent)
            } else if (claseeName.equals("framer_Pipe")) {
                val intent = Intent(this, PipeActivity::class.java)
                intent.putExtra("StartTime", StartTime)
                intent.putExtra("selectSeason", selectSeason.toString())
                intent.putExtra("selectyear", selectyear.toString())
                intent.putExtra("StartTime", StartTime)
                startActivity(intent)
            } else if (claseeName.equals("framer_aeration")) {
                val intent = Intent(this, AerationActivity::class.java)
                intent.putExtra("StartTime", StartTime)
                intent.putExtra("selectSeason", selectSeason.toString())
                intent.putExtra("selectyear", selectyear.toString())
                startActivity(intent)
            } else if (claseeName.equals("farmer_cropinfo")) {
                val intent = Intent(this, CropActivity::class.java).apply {
                    putExtra("total_sub_plots", 0)
                    putExtra("total_number", 0)
                    putExtra("StartTime", StartTime)
                    putStringArrayListExtra("plot_area", AcresList)
                    putStringArrayListExtra("farmer_plot_uniqueid", AcresList)
                    putStringArrayListExtra("plot_no", AcresList)
                    putStringArrayListExtra("plot_id", AcresList)
                    putStringArrayListExtra("awd_plot_area", AcresList)
                    putStringArrayListExtra("awd_acres_area", AcresList)
                    putStringArrayListExtra("update", update)
                    putExtra("farmer_name", "")
                    putExtra("mobile_number", "")
                    putExtra("unique_id", "")
                    putExtra("state", " ")
                    putExtra("state_id", " ")
                    putExtra("cropdata_end_days", end_of_date)
                    putExtra("preparation_date_interval", preparation_date_interval)
                    putExtra("transplantation_date_interval", transplantation_date_interval)
                    putExtra("transplantation_day", 0)
                    putExtra("transplantation_month", 0)
                    putExtra("transplantation_year", 0)
                }
                startActivity(intent)
            }

        }

        button_back.setOnClickListener { super.onBackPressed() }

    }

    fun getDataYear(currentMonth: String) {

        //var month = DataYear(currentMonth)
        var month = DataYear(currentMonth)
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.getDataYear("Bearer $token", month)
            .enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    if (response.code() == 200) {

                        str_name.add("--Select Season--")
                        str_year.add("--Select Year--")

                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonarraydata = stringResponse.optJSONObject("data")

                        var string_name = jsonarraydata.getString("season")
                        var string_year = jsonarraydata.getString("year")

                        str_name.add(string_name)
                        str_year.add(string_year)

                        Log.d("usersession", str_name.toString())

                            val adapter = ArrayAdapter(
                                this@YearRegistractionActivity,
                                android.R.layout.simple_list_item_1,
                                str_name
                            )
                            SelectSession_spinner.adapter = adapter

                            val adapter_year = ArrayAdapter(
                                this@YearRegistractionActivity,
                                android.R.layout.simple_list_item_1,
                                str_year
                            )
                            SelectYear_spinner.adapter = adapter_year

//                        if (jsonarraydata.length() != 0){
//
//                            for (i in 0 until jsonarraydata.length()) {
//
//                                var jsonObject: JSONObject = jsonarraydata.getJSONObject(i)
//
//                                var string_name = jsonObject.getString("name")
//                                var string_year = jsonObject.getString("year")
//
//                                str_name.add(string_name)
//                                str_year.add(string_year)
//
//                            }
//
//                            Log.d("usersession", str_name.toString())
//
//                            val adapter = ArrayAdapter(
//                                this@YearRegistractionActivity,
//                                android.R.layout.simple_list_item_1,
//                                str_name
//                            )
//                            SelectSession_spinner.adapter = adapter
//
//                            val adapter_year = ArrayAdapter(
//                                this@YearRegistractionActivity,
//                                android.R.layout.simple_list_item_1,
//                                str_year
//                            )
//                            SelectYear_spinner.adapter = adapter_year
//
//                        }
//
//                        else{
//                            val WarningDialog =
//                                SweetAlertDialog(this@YearRegistractionActivity, SweetAlertDialog.WARNING_TYPE)
//                            WarningDialog.titleText = resources.getString(R.string.warning)
//                            WarningDialog.contentText = "Data year Not Avilable"
//                            WarningDialog.confirmText = resources.getString(R.string.ok)
//                            WarningDialog.setCancelClickListener {
//                                WarningDialog.cancel()
//                            }.show()
//                        }

                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("NEW_TEST", "Get Address Error  $t")
                    Log.e("NEW_TEST", "Get Address Error  $call")
                }

            })
    }
}