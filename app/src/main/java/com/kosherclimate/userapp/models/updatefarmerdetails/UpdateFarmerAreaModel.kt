package com.kosherclimate.userapp.models.updatefarmerdetails

data class UpdateFarmerAreaModel(
    val farmer_uniqueId: String,
//    val farmer_plot_uniqueid: String,
    val available_area: String,
    val area_in_acers: String,
)
