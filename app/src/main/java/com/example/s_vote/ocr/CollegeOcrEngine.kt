package com.example.s_vote.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color

object CollegeOcrEngine {

    fun readCollegeName(context: Context, bitmap: Bitmap): String {
        val dataset = DatasetLoader.loadDataset(context)
        val letterDataset = dataset.filterKeys { it.isLetter() }
        
        // AUTO-INVERT Logic
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var whiteCount = 0
        for (p in pixels) {
            if ((p and 0xFF) > 128) whiteCount++
        }
        
        val targetBitmap = if (whiteCount > (pixels.size / 2)) {
            for (i in pixels.indices) {
                val p = pixels[i]
                val r = 255 - (p and 0xFF)
                pixels[i] = (0xFF shl 24) or (r shl 16) or (r shl 8) or r
            }
            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        } else {
            bitmap
        }
        
        val letters = LetterSplitter.splitLetters(targetBitmap)
        val result = StringBuilder()
        
        for (letter in letters) {
            val char = LetterMatcher.matchLetter(letter, letterDataset)
            result.append(char)
        }
        
        return result.toString()
    }
}
