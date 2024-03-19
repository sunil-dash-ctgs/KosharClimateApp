package com.kosherclimate.userapp.adapters


import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kosherclimate.userapp.R
import com.kosherclimate.userapp.models.LandRecordsModel
import com.kosherclimate.userapp.weather.RecyclerViewEvent
import java.io.ByteArrayOutputStream


class ImageRecyclerView(private var imageModel: List<LandRecordsModel>) : RecyclerView.Adapter<ImageRecyclerView.MyViewHolder>() {

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var img: ImageView = itemView.findViewById(R.id.image_data)
        lateinit var listener: RecyclerViewEvent
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_imageview, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model: LandRecordsModel = imageModel[position]
        holder.img.setImageBitmap(model.image)
        holder.img.rotation = model.rotate.toFloat()

    }


    override fun getItemCount(): Int {
        return imageModel.size
    }

}