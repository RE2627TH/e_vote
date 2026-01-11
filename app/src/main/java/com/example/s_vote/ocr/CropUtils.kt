package com.example.s_vote.ocr

import android.graphics.Bitmap

object CropUtils {

    // Crop college name (top area)
    fun cropCollege(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            width,
            height / 5
        )
    }

    // Crop student name (middle area)
    fun cropName(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        return Bitmap.createBitmap(
            bitmap,
            0,
            height / 2,
            width,
            height / 6
        )
    }

    // Crop register number (bottom area)
    fun cropRegNo(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        return Bitmap.createBitmap(
            bitmap,
            0,
            height - (height / 6),
            width,
            height / 6
        )
    }
}
