package com.kosherclimate.userapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

class CommonData {

    fun drawTextToBitmap(gContext: Context, bitmap: Bitmap, gText: String
    ): Bitmap? {
        var bitmap = bitmap
        val resources = gContext.resources
        val scale = resources.displayMetrics.density
        var bitmapConfig = bitmap.config
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true)
        val canvas = Canvas(bitmap)
        // new antialised Paint
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // text color - #3D3D3D
        paint.color = Color.WHITE
        // text size in pixels
        paint.textSize = (35 * scale).toInt().toFloat()
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        // draw text to the Canvas center
        val bounds = Rect()
        var noOfLines = 0
        for (line in gText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            noOfLines++
        }
        paint.getTextBounds(gText, 0, gText.length, bounds)
        val x = 20
        var y = bitmap.height - bounds.height() * noOfLines
        val mPaint = Paint()
        mPaint.color = gContext.getResources().getColor(com.kosherclimate.userapp.R.color.transparentBlack)
        val left = 0
        val top = bitmap.height - bounds.height() * (noOfLines + 1)
        val right = bitmap.width
        val bottom = bitmap.height
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
        for (line in gText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            canvas.drawText(line, x.toFloat(), y.toFloat(), paint)
            y += (paint.descent() - paint.ascent()).toInt()
        }
        return bitmap
    }

    fun drawTextToBitmap1(gContext: Context, bitmap: Bitmap, gText: String
    ): Bitmap? {
        var bitmap = bitmap
        val resources = gContext.resources
        val scale = resources.displayMetrics.density
        var bitmapConfig = bitmap.config
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true)
        val canvas = Canvas(bitmap)
        // new antialised Paint
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // text color - #3D3D3D
        paint.color = Color.WHITE
        // text size in pixels
        paint.textSize = (35 * scale).toInt().toFloat()
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE)

        // draw text to the Canvas center
        val bounds = Rect()
        var noOfLines = 1
//        for (line in gText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
//            noOfLines++
//        }
        paint.getTextBounds(gText, 0, gText.length, bounds)
        val x = 30
        var y = bitmap.height - bounds.height() * noOfLines
        val mPaint = Paint()
        mPaint.color = gContext.getResources().getColor(com.kosherclimate.userapp.R.color.transparentBlack)
        val left = 0
        val top = bitmap.height - bounds.height() * (noOfLines + 2)
        val right = bitmap.width
        val bottom = bitmap.height
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), mPaint)
        canvas.drawText(gText, x.toFloat(), y.toFloat(), paint)

//        for (line in gText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
//            canvas.drawText(line, x.toFloat(), y.toFloat(), paint)
//            y += (paint.descent() - paint.ascent()).toInt()
//        }
        return bitmap
    }
}