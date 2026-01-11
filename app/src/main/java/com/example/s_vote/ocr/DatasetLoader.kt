package com.example.s_vote.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object DatasetLoader {

    fun loadDataset(context: Context): Map<Char, Bitmap> {

        val dataset = mutableMapOf<Char, Bitmap>()

        val chars = ('A'..'Z') + ('0'..'9')
        for (c in chars) {
            val input = try {
                context.assets.open("ocr_chars/$c.png.png")
            } catch (e: Exception) {
                try {
                    context.assets.open("ocr_chars/$c.png")
                } catch (e2: Exception) {
                    null
                }
            }

            if (input != null) {
                val originBitmap = BitmapFactory.decodeStream(input)
                // PREPROCESS THE TEMPLATE
                // 1. Resize to standardize (20x20)
                val scaled = Bitmap.createScaledBitmap(originBitmap, 20, 20, true)
                
                // 2. Binarize (Ensure it's pure Black/White like our input blobs)
                // We assume templates are Black Text on White BG? Or White Text on Black BG?
                // Let's force them to be consistent: White Text on Black BG (matches BlobExtractor output)
                val processed = preprocessTemplate(scaled)
                
                dataset[c] = processed
            }
        }

        return dataset
    }

    private fun preprocessTemplate(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        
        // Auto-detect inversion
        var sum = 0L
        for (p in pixels) {
             val r = (p shr 16) and 0xFF
             sum += r
        }
        val avg = (sum / pixels.size).toInt()
        
        // If Avg > 128, it's White BG. We want Black BG.
        val needInvert = avg > 128
        
        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            
            // Logic: Make TEXT White (0xFFFFFF), BG Black (0x000000)
            if (needInvert) {
                // Input: White BG, Dark Text
                if (r < 128) pixels[i] = android.graphics.Color.WHITE // Text
                else pixels[i] = android.graphics.Color.BLACK // BG
            } else {
                // Input: Black BG, White Text
                if (r > 128) pixels[i] = android.graphics.Color.WHITE // Text
                else pixels[i] = android.graphics.Color.BLACK // BG
            }
        }
        
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }
}
