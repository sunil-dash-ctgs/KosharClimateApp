package com.kosherclimate.userapp.models.updatefarmerdetails

data class UpdatePlotInfo(
    val farmer_uniqueId :String,
//    val area_in_other :String,
    val area_in_other_unit :String,
    val survey_no :String,
    val land_ownership :String,
    val patta_number :String,
    val daag_number :String,
    val khatha_number :String,
    val pattadhar_number :String,
    val khatian_number :String,
    val actual_owner_name :String,
)
