package com.kosherclimate.userapp.models.updatefarmerdetails

data class UpdatedFarmerLocation(
    val farmer_uniqueId: String,
    val state_id: String,
    val district_id: String,
    val taluka_id: String,
    val panchayat_id: String,
    val village_id: String,
    val remarks: String,
)
