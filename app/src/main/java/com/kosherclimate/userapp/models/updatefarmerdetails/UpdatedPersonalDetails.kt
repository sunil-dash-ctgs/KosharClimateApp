package com.kosherclimate.userapp.models.updatefarmerdetails

data class UpdatedPersonalDetails(
    val farmer_uniqueId: String,
    val farmer_name: String,
    val mobile_access: String,
    val mobile_reln_owner: String,
    val mobile: String,
    val aadhar: String,
//    var plot_detail: ArrayList<PlotDetailsModel>,
    val gender: String,
    val guardian_name: String,
    val organization_id: String,
    var area_in_acers : String,
    var own_area_in_acres : String,
    var lease_area_in_acres : String,

)
