package com.kosherclimate.userapp.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.models.RejectedListModel
import com.kosherclimate.userapp.reports.farmer_report.FramerImageUploadActivity
import com.kosherclimate.userapp.reports.farmer_report.ImageReuploadActivity
import com.kosherclimate.userapp.reports.farmer_report.ReuploadPlotDetailsActivity
import com.kosherclimate.userapp.updatefarmer.UpdateFarmerLocationActivity

class RejectedOnBoardingList_Adapter(private  var list : List<RejectedListModel>) : RecyclerView.Adapter<RejectedOnBoardingList_Adapter.MyViewHolder>() {


     class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

         val rejectedTitle : TextView = itemView.findViewById(R.id.tvPlotRejectedTitle)
         val rejectedReason : TextView = itemView.findViewById(R.id.tvRejectedReason)
         val ivArrow : ImageView = itemView.findViewById(R.id.ivArrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rejected_list_recycler,parent,false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        holder.rejectedTitle.text = item.getRejectedTitle()
        holder.rejectedReason.text = item.getRejectedReason()


        holder.itemView.setOnClickListener {
            if (item.getID() == "3" || item.getID() == "4" || item.getID() == "5") {
                val intent = Intent(holder.itemView.context, ReuploadPlotDetailsActivity::class.java).apply {
                    putExtra("reason_id", item.getID())
                    putExtra("unique_id", item.getUniqueID())
                    putExtra("plot_no", item.getPlotNo())
                    putExtra("base_value", item.getBaseValue())
                    putExtra("financial_year", item.getfinancial_year())
                    putExtra("season", item.getseason())
                }
                holder.itemView.context.startActivity(intent)
            }
            else if(item.getID() == "1" || item.getID() == "2"){
                val intent = Intent(holder.itemView.context, ImageReuploadActivity::class.java).apply {
                    putExtra("reason_id", item.getID())
                    putExtra("unique_id", item.getUniqueID())
                    putExtra("plot_no", item.getPlotNo())
                    putExtra("financial_year", item.getfinancial_year())
                    putExtra("season", item.getseason())
                }
                holder.itemView.context.startActivity(intent)

            } else if(item.getID() == "12" || item.getID() == "13"){
                val intent = Intent(holder.itemView.context, FramerImageUploadActivity::class.java).apply {
                    putExtra("reason_id", item.getID())
                    putExtra("unique_id", item.getUniqueID())
                    putExtra("plot_no", item.getPlotNo())
                    putExtra("financial_year", item.getfinancial_year())
                    putExtra("season", item.getseason())
                }
                holder.itemView.context.startActivity(intent)

            }


        }
    }

    override fun getItemCount(): Int {
       return  list.size
    }
}