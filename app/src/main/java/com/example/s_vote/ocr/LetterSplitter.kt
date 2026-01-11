package com.example.s_vote.ocr

import android.graphics.Bitmap
import android.graphics.Color

object LetterSplitter {

    fun splitLetters(bitmap: Bitmap): List<Bitmap> {

        val letters = mutableListOf<Bitmap>()

        val width = bitmap.width
        val height = bitmap.height

        var startX = -1

        for (x in 0 until width) {
            var hasWhitePixel = false
            var whitePixelCount = 0
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                if (Color.red(pixel) > 128) { // white pixel (using 128 as safe mid-val)
                     whitePixelCount++
                }
            }
            
            // Require at least 2 pixels to consider this column part of a letter (reduces noise)
            if (whitePixelCount >= 2) hasWhitePixel = true

            if (hasWhitePixel && startX == -1) {
                // letter start
                startX = x
            }

            if (!hasWhitePixel && startX != -1) {
                // letter end
                val letterWidth = x - startX

                if (letterWidth > 5) { // ignore noise
                    val letterBitmap = Bitmap.createBitmap(
                        bitmap,
                        startX,
                        0,
                        letterWidth,
                        height
                    )
                    letters.add(letterBitmap)
                }
                startX = -1
            }
        }

        return letters
    }
}
