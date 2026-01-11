package com.example.s_vote.ocr

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

object LetterMatcher {

    fun matchLetter(
        input: Bitmap,
        dataset: Map<Char, Bitmap>
    ): Char {

        var bestChar = '?'
        var bestScore = Int.MAX_VALUE

        val resizedInput = Bitmap.createScaledBitmap(input, 20, 20, true)

        for ((char, template) in dataset) {

            val resizedTemplate =
                Bitmap.createScaledBitmap(template, 20, 20, true)

            var diff = 0

            for (x in 0 until 20) {
                for (y in 0 until 20) {
                    val p1 = Color.red(resizedInput.getPixel(x, y))
                    val p2 = Color.red(resizedTemplate.getPixel(x, y))
                    diff += abs(p1 - p2)
                }
            }

            if (diff < bestScore) {
                bestScore = diff
                bestChar = char
            }
        }

        return bestChar
    }
}
