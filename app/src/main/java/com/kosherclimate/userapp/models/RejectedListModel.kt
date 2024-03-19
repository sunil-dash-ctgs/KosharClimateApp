package com.kosherclimate.userapp.models

class RejectedListModel(rejectedTitle: String, rejectedReason: String, id: String, unique_id: String, plot_no: String, base_vale: Double,
                        financial_year: String, season: String) {
    private var rejectedTitle: String
    private var rejectedReason: String
    private var id: String
    private var unique_id: String
    private var plot_no: String
    private var base_vale: Double
    private var financial_year: String
    private var season: String

    init {
        this.rejectedTitle = rejectedTitle
        this.rejectedReason = rejectedReason
        this.id = id
        this.unique_id = unique_id
        this.plot_no = plot_no
        this.base_vale = base_vale
        this.financial_year = financial_year
        this.season = season
    }
    fun getseason(): String {
        return season
    }

    fun setseason(season: String) {
        this.season = season
    }
    fun getfinancial_year(): String {
        return financial_year
    }

    fun setfinancial_year(financial_year: String) {
        this.financial_year = financial_year
    }

    fun getRejectedTitle(): String {
        return rejectedTitle
    }

    fun setRejectedTitle(rejectedTitle: String) {
        this.rejectedTitle = rejectedTitle
    }

    fun getRejectedReason(): String {
        return rejectedReason
    }

    fun setRejectedReason(rejectedReason: String) {
        this.rejectedReason = rejectedReason
    }

    fun getID(): String {
        return id
    }

    fun setID(id: String) {
        this.id = id
    }

    fun getUniqueID(): String {
        return unique_id
    }

    fun setUniqueID(unique_id: String) {
        this.unique_id = unique_id
    }

    fun getPlotNo(): String {
        return plot_no
    }

    fun setPlotNo(plot_no: String) {
        this.plot_no = plot_no
    }


    fun getBaseValue(): Double {
        return base_vale
    }

    fun setBaseValue(base_vale: Double) {
        this.base_vale = base_vale
    }
}
