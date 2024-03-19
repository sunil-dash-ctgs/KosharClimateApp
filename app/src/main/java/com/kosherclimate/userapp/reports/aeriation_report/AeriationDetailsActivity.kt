package com.kosherclimate.userapp.reports.aeriation_report

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.kosherclimate.userapp.R

class AeriationDetailsActivity : AppCompatActivity() {
    private lateinit var txtUniqueID: TextView
    private lateinit var txtFarmerName: TextInputEditText
    private lateinit var txtPlotNo: TextInputEditText
    private lateinit var txtPlotUniqueId: TextInputEditText
    private lateinit var txtAerationNumber: TextInputEditText
    private lateinit var txtReason: TextInputEditText
    private lateinit var aeration_details_aeration_Season: TextInputEditText
    private lateinit var aeration_details_FinancialYear: TextInputEditText

    private lateinit var imgBack: ImageView
    private lateinit var imgEdt: ImageView

    private lateinit var pipe_installation_id: String
    private lateinit var aeration_no: String
    private lateinit var pipe_no: String

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aeriation_details)

        txtAerationNumber = findViewById(R.id.aeration_details_aeration_number)
        txtPlotUniqueId = findViewById(R.id.aeration_details_plot_unique_id)
        txtFarmerName = findViewById(R.id.aeration_details_farmer_name)
        txtReason = findViewById(R.id.aeriation_report_detail_reason)
        txtPlotNo = findViewById(R.id.aeration_details_plot_number)
        txtUniqueID = findViewById(R.id.aeration_details_uniqueId)
        aeration_details_aeration_Season = findViewById(R.id.aeration_details_aeration_Season)
        aeration_details_FinancialYear = findViewById(R.id.aeration_details_FinancialYear)

        imgEdt = findViewById(R.id.aeriation_report_detail_edit)
        imgBack = findViewById(R.id.aeration_report_back)

        val bundle = intent.extras
        if (bundle != null) {
            txtPlotUniqueId.text = bundle.getString("farmer_uniqueId").toString().toEditable()
            pipe_installation_id = bundle.getString("pipe_installation_id")!!
            txtFarmerName.text = bundle.getString("farmer_name")!!.toEditable()
            txtUniqueID.text = bundle.getString("uniqueId")!!
            txtAerationNumber.text = bundle.getString("aeration_no")!!.toEditable()
            txtPlotNo.text = bundle.getString("plot_no")!!.toEditable()
            txtReason.text = bundle.getString("reasons")!!.toEditable()
            pipe_no = bundle.getString("pipe_no")!!
            aeration_details_aeration_Season.text = bundle.getString("season")!!.toEditable()
            aeration_details_FinancialYear.text = bundle.getString("financial_year")!!.toEditable()
        }

        imgBack.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })


        imgEdt.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, AeriationReportMapActivity::class.java).apply {
                putExtra("farmer_plot_uniqueid", txtPlotUniqueId.text.toString())
                putExtra("pipe_installation_id", pipe_installation_id)
                putExtra("aeration_no", txtAerationNumber.text.toString())
                putExtra("farmer_name", txtFarmerName.text.toString())
                putExtra("unique_id", txtUniqueID.text.toString().toString())
                putExtra("plot_no", txtPlotNo.text.toString())
                putExtra("pipe_no", pipe_no)
                putExtra("season", aeration_details_aeration_Season.text.toString())
                putExtra("financial_year", aeration_details_FinancialYear.text.toString())
            }
            startActivity(intent)
        })
    }
}