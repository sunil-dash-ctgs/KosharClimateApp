package com.kosherclimate.userapp.adapters

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.models.PipeReportModel
import com.kosherclimate.userapp.reports.pipe_report.PipeReportDetailActivity
import java.lang.Integer.min

class Pipe_Report_Adapter(var pipeReportModel: List<PipeReportModel>) : RecyclerView.Adapter<Pipe_Report_Adapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
       // var srNo: TextView = view.findViewById(R.id.firstColumn)
//        var uniqueId: TextView = view.findViewById(R.id.secondColumn)
        var farmerName: TextView = view.findViewById(R.id.secondColumn)
        var farmerUniqueId: TextView = view.findViewById(R.id.thirdColumn)
//        var pipeNo: TextView = view.findViewById(R.id.fourthColumn)
        var direction: ImageView = view.findViewById(R.id.fourthColumn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.direction_report, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val reportModel = pipeReportModel[position]
       //.
        // 3holder.srNo.text = reportModel.getId()
        holder.farmerName.text = reportModel.getFarmerFirstName()
        holder.farmerUniqueId.text = reportModel.getFarmerPlotUniqueId()


        holder.direction.setOnClickListener {
            Log.e("NEW_TEST","CLick on directions")
            try {
                // Start Google Maps navigation
                val destinationLat = reportModel.getLat() // Replace with your destination latitude
                val destinationLng = reportModel.getLng() // Replace with your destination longitude
                Log.e("NEW_TEST","CLick on directions $destinationLat,$destinationLng")
                val uri = "google.navigation:q=$destinationLat,$destinationLng"
//                val uri = "geo:0,0?q=$destinationLat,$destinationLng"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps") // Use the Google Maps app
                if (intent.resolveActivity(it.context.packageManager) != null) {
                    it.context.startActivity(intent)
                }
            }catch (e:Exception){
                Log.e("NEW_TEST","Error >>>>>>$e")
            }
        }

        holder.itemView.setOnClickListener {
            Log.e("uniqueId", reportModel.getFarmerId())

            val intent = Intent(it.context, PipeReportDetailActivity::class.java).apply {

                putExtra("pipe_img_id", reportModel.getId())
                putExtra("farmer_uniqueId", holder.farmerUniqueId.text.toString())
                putExtra("pipe_no", reportModel.getPlotNo())
                putExtra("uniqueId", reportModel.getFarmerId())
                putExtra("plot_no", reportModel.getPlotNo())
                putExtra("lat", reportModel.getLat())
                putExtra("lng", reportModel.getLng())
                putExtra("distance", reportModel.getAreaInAcers())
                putExtra("farmer_name", reportModel.getFarmerName())
                putExtra("reasons", reportModel.getReason())
                putExtra("reason_id", reportModel.getReasonID())
                putExtra("state", reportModel.getState())
                putExtra("district", reportModel.getDistrict())
                putExtra("taluka", reportModel.getTaluka())
                putExtra("village", reportModel.getVillage())
                putExtra("aadhar", reportModel.getAadhar())
                putExtra("mobile", reportModel.getMobile())
                putExtra("season", reportModel.getseason())
                putExtra("financial_year", reportModel.getfinancial_year())

                Log.e("mLastLocation.latitude", reportModel.getLat())
                Log.e("mLastLocation.longitude",reportModel.getLng())
                Log.e("mLastLocation.longitude",reportModel.getLng())
                Log.e("mLastLocation.longitude",reportModel.getLng())
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pipeReportModel.size
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getNextItems(startIndex: Int): List<PipeReportModel> {
        val endIndex = min(startIndex + 10, pipeReportModel.size)
//        Log.e("next_data", endIndex.toString())
        return pipeReportModel.subList(startIndex, endIndex)
    }
}