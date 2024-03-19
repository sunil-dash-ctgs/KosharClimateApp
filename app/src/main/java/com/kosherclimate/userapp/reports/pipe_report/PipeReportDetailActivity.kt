package com.kosherclimate.userapp.reports.pipe_report

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.directions.AskDirectionActivity
import com.kosherclimate.userapp.reports.polygon_report.PolygonReportMapActivity

class PipeReportDetailActivity : AppCompatActivity() {

    private lateinit var txtUniqueID: TextView
    private lateinit var txtFarmerName: TextInputEditText
    private lateinit var txtPlotNo: TextInputEditText
    private lateinit var txtPlotUniqueId: TextInputEditText
    private lateinit var txtPipeNumber: TextInputEditText
    private lateinit var txtReason: TextInputEditText
    private lateinit var txtPlotArea: TextInputEditText
    private lateinit var pipe_report_detail_Season: TextInputEditText
    private lateinit var pipe_report_detail_Year: TextInputEditText

    private lateinit var imgBack: ImageView
    private lateinit var imgEdt: ImageView

    private lateinit var distance: String
    private lateinit var reason_id: String
    private lateinit var pipe_img_id: String
    private lateinit var pipeImageLatitude: String
    private lateinit var pipeImageLongitude: String

    private lateinit var state: TextInputEditText
    private lateinit var district: TextInputEditText
    private lateinit var taluka: TextInputEditText
    private lateinit var villageTv: TextInputEditText
    private lateinit var aadharTv: TextInputEditText
    private lateinit var mobileTv: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pipe_report_detail)

        fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

        txtUniqueID = findViewById(R.id.pipe_report_detail_uniqueId)
        txtFarmerName = findViewById(R.id.pipe_report_detail_name)
        txtPlotNo = findViewById(R.id.pipe_report_detail_plot)
        txtPlotUniqueId = findViewById(R.id.pipe_report_detail_plot_uniqueId)
        txtPipeNumber = findViewById(R.id.pipe_report_detail_pipe)
        txtReason  = findViewById(R.id.pipe_report_detail_reason)
        txtPlotArea = findViewById(R.id.pipe_report_detail_plot_area)
        pipe_report_detail_Year = findViewById(R.id.pipe_report_detail_Year)
        pipe_report_detail_Season = findViewById(R.id.pipe_report_detail_Season)

        imgBack = findViewById(R.id.pipe_report_detail_back)
        imgEdt = findViewById(R.id.pipe_report_detail_edit)

        state = findViewById(R.id.pipe_report_detail_plot_state)
        district = findViewById(R.id.pipe_report_detail_district)
        taluka = findViewById(R.id.pipe_report_detail_taluka)
        villageTv = findViewById(R.id.pipe_report_detail_village)
        aadharTv = findViewById(R.id.pipe_report_detail_aadhar)
        mobileTv = findViewById(R.id.pipe_report_detail_mobile)

        val bundle = intent.extras
        if (bundle != null) {
            pipe_img_id = bundle.getString("pipe_img_id").toString()
            txtPlotUniqueId.text = bundle.getString("farmer_uniqueId").toString().toEditable()
            txtFarmerName.text = bundle.getString("farmer_name").toString().toEditable()
            txtUniqueID.text = bundle.getString("uniqueId").toString().toEditable()
            pipeImageLatitude = bundle.getString("lat").toString()
            pipeImageLongitude = bundle.getString("lng").toString()
            txtPlotNo.text = bundle.getString("plot_no").toString().toEditable()
            txtPipeNumber.text = bundle.getString("pipe_no").toString().toEditable()
            txtPlotArea.text = bundle.getString("distance").toString().toEditable()
            txtReason.text = bundle.getString("reasons").toString().toEditable()
            reason_id = bundle.getString("reason_id").toString()

            state.text = bundle.getString("state").toString().toEditable()
            district.text = bundle.getString("district").toString().toEditable()
            taluka.text = bundle.getString("taluka").toString().toEditable()
            villageTv.text = bundle.getString("village").toString().toEditable()
            aadharTv.text = bundle.getString("aadhar").toString().toEditable()
            mobileTv.text = bundle.getString("mobile").toString().toEditable()
            pipe_report_detail_Season.text = bundle.getString("season").toString().toEditable()
            pipe_report_detail_Year.text = bundle.getString("financial_year").toString().toEditable()

        } else {
            Log.e("unique_id", "Nope")
        }

        imgBack.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
            finish()
        })

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(it.context, AskDirectionActivity::class.java)
            startActivity(intent)
        }

        imgEdt.setOnClickListener(View.OnClickListener{
            if (reason_id == "8"){
                val intent = Intent(it.context, ShowGoogleMapOnPipe::class.java).apply {
                    putExtra("pipe_img_id", pipe_img_id)
                    putExtra("farmer_uniqueId",  txtPlotUniqueId.text?.toString() ?: "")
                    putExtra("pipe_no", txtPipeNumber.text?.toString() ?: "")
                    putExtra("uniqueId", txtUniqueID.text?.toString() ?: "")
                    putExtra("plot_no", txtPlotNo.text?.toString() ?: "")
                    putExtra("lat", pipeImageLatitude)
                    putExtra("lng", pipeImageLongitude)
                    putExtra("distance", txtPlotArea.text?.toString() ?: "")
                    putExtra("financial_year", pipe_report_detail_Year.text?.toString() ?: "")
                    putExtra("season", pipe_report_detail_Season.text?.toString() ?: "")

                    Log.d("detailsactivity","details"+pipe_report_detail_Year.text?.toString() ?: "")
                    Log.d("detailsactivity","details1"+pipe_report_detail_Season.text?.toString() ?: "")
                }
                startActivity(intent)
            }
            else if (reason_id == "10"){
                val intent = Intent(it.context, PipeReportMapActivity::class.java).apply {
                    putExtra("pipe_img_id", pipe_img_id)
                    putExtra("farmer_uniqueId",  txtPlotUniqueId.text?.toString() ?: "")
                    putExtra("pipe_no", txtPipeNumber.text?.toString() ?: "")
                    putExtra("uniqueId", txtUniqueID.text?.toString() ?: "")
                    putExtra("plot_no", txtPlotNo.text?.toString() ?: "")
                    putExtra("lat", pipeImageLatitude)
                    putExtra("lng", pipeImageLongitude)
                    putExtra("distance", txtPlotArea.text?.toString() ?: "")
                    putExtra("financial_year", pipe_report_detail_Year.text?.toString() ?: "")
                    putExtra("season", pipe_report_detail_Season.text?.toString() ?: "")
                }
                startActivity(intent)
            }
            else if (reason_id == "11"){
                val intent = Intent(it.context, PolygonReportMapActivity::class.java).apply {
                    putExtra("pipe_img_id", pipe_img_id)
                    putExtra("farmer_uniqueId",  txtPlotUniqueId.text?.toString() ?: "")
                    putExtra("pipe_no", txtPipeNumber.text?.toString() ?: "")
                    putExtra("uniqueId", txtUniqueID.text?.toString() ?: "")
                    putExtra("plot_no", txtPlotNo.text?.toString() ?: "")
                    putExtra("lat", pipeImageLatitude)
                    putExtra("lng", pipeImageLongitude)
                    putExtra("distance", txtPlotArea.text?.toString() ?: "")
                    putExtra("financial_year", pipe_report_detail_Year.text?.toString() ?: "")
                    putExtra("season", pipe_report_detail_Season.text?.toString() ?: "")
                }
                startActivity(intent)
            }
        })
    }
}