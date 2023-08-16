package com.kosherclimate.userapp.models

import android.graphics.Color
import android.graphics.Typeface
import androidx.annotation.ColorInt
import com.kosherclimate.userapp.utils.Corner

data class WatermarkOptions(
    val corner: Corner = Corner.BOTTOM_RIGHT,
    val textSizeToWidthRatio: Float = 0.04f,
    val paddingToWidthRatio: Float = 0.03f,
    @ColorInt val textColor: Int = Color.WHITE,
    @ColorInt val shadowColor: Int? = Color.BLACK,
    val typeface: Typeface? = null
)
