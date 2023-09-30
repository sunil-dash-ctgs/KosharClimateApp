package com.kosherclimate.userapp.models.polygonmodel

data class PolygonsModelValue (
    val gid: String,
    val fid: String,
    val ranges: List<Range>
)

data class Range (
    val lat: String,
    val lng: String
)

