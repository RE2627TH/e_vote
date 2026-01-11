package com.example.s_vote.ocr

import android.graphics.Bitmap
import android.graphics.Color

import android.graphics.Matrix

object ImagePreprocessor {

    // STEP 0.5: Rotate image
    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // STEP 0: Downscale image for performance
    fun resize(bitmap: Bitmap, maxWidth: Int): Bitmap {
        if (bitmap.width <= maxWidth) return bitmap
        val aspectRatio = bitmap.height.toDouble() / bitmap.width.toDouble()
        val targetHeight = (maxWidth * aspectRatio).toInt()
        return Bitmap.createScaledBitmap(bitmap, maxWidth, targetHeight, true)
    }

    // STEP 1: Convert color image to grayscale (Optimized)
    fun toGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            // Average method
            val gray = (r + g + b) / 3
            pixels[i] = (0xFF shl 24) or (gray shl 16) or (gray shl 8) or gray
        }
        
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    // STEP 2: Apply threshold (Smart Histogram-based)
    fun applyThreshold(bitmap: Bitmap, threshold: Int = -1): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // 1. Calculate Min and Max Intensity to gauge contrast
        var minVal = 255
        var maxVal = 0
        var sumVal: Long = 0

        for (p in pixels) {
            val v = (p shr 16) and 0xFF
            if (v < minVal) minVal = v
            if (v > maxVal) maxVal = v
            sumVal += v
        }
        
        val avgVal = (sumVal / pixels.size).toInt()

        // 2. Determine Optimal Threshold
        // For ID cards/Screens: Text is usually the darkest part (minVal)
        // Background is average to max.
        // A good threshold is often closer to the darkness than the brightness if contrast is high.
        // If contrast is low (screen), we need to capture the dark text.
        
        val finalThreshold = if (threshold != -1) {
            threshold
        } else {
            // "Otsu-like" approximation: Average of (Mean and Min)
            // This favors dark text extraction.
            (minVal + avgVal) / 2 + (maxVal - minVal) / 10 // distinct bias
        }

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val gray = (pixel shr 16) and 0xFF 

            // Text is Dark (Low Value). Background is Light (High Value).
            // deeper than threshold -> Text -> Make WHITE (for detection)
            // lighter than threshold -> BG -> Make BLACK
            
            if (gray > finalThreshold) {
                pixels[i] = Color.BLACK // Background
            } else {
                pixels[i] = Color.WHITE // Text
            }
        }
        
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
