package com.example.s_vote.ocr

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

object SmartOcrMatcher {

    data class MatchResult(val char: Char, val confidence: Float)

    fun recognizeDigit(blob: Bitmap, dataset: Map<Char, Bitmap>): MatchResult {
        // 1. Prepare Blob: Resize to fixed 20x20
        // We match strict digits only for robustness
        val cleanDataset = dataset.filterKeys { it.isDigit() }
        
        // Centering Logic would go here, but Bitmap.createScaledBitmap does a decent "Squash" which works for OCR
        // if aspect ratio is not too wild.
        val input = Bitmap.createScaledBitmap(blob, 20, 20, true)
        
        var bestChar = '?'
        var minDiff = Int.MAX_VALUE
        
        for ((char, template) in cleanDataset) {
            val templateScaled = Bitmap.createScaledBitmap(template, 20, 20, true)
            
            val diff = compareBitmaps(input, templateScaled)
            if (diff < minDiff) {
                minDiff = diff
                bestChar = char
            }
        }
        
        // Calculate Confidence (0.0 to 1.0)
        // Max diff roughly 20*20*255 = 102000
        // Relaxed divisor from 50000 to 80000 for screen moire patterns
        val confidence = (1.0f - (minDiff.toFloat() / 80000f)).coerceIn(0f, 1f)
        
        return MatchResult(bestChar, confidence)
    }

    private fun compareBitmaps(b1: Bitmap, b2: Bitmap): Int {
        var diff = 0
        for (x in 0 until 20) {
            for (y in 0 until 20) {
                // We compare RED channel as they are grayscale/BW
                // Using SAD (Sum of Absolute Differences)
                val v1 = Color.red(b1.getPixel(x, y))
                val v2 = Color.red(b2.getPixel(x, y))
                
                // Weight central pixels higher? No, keep simple.
                diff += abs(v1 - v2)
            }
        }
        return diff
    }
}
