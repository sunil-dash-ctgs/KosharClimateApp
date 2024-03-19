package com.kosherclimate.userapp.models

data class PipeQtyModel(
    val farmer_uniqueId: String,
    val plot_no: String,
    val farmer_plot_uniqueid: String,
    val financial_year: String,
    val season: String
)
