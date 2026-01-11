package com.example.s_vote.ocr

import android.graphics.Bitmap
import android.graphics.Color
import java.util.Stack

/**
 * A robust Connected Component Labeling (Blob Detection) engine.
 * This effectively separates characters even if they are skewed or not perfectly aligned vertically.
 */
object BlobExtractor {

    data class Blob(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val bitmap: Bitmap
    )

    fun extractBlobs(source: Bitmap): List<Blob> {
        val width = source.width
        val height = source.height
        val visited = BooleanArray(width * height)
        val blobs = mutableListOf<Blob>()

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Auto-Invert Logic:
        // We expect mostly Black background and White text (sparse).
        // If more than 50% of pixels are White, it means we have White Background.
        // We must invert it to get White Text on Black Background for Blob detection.
        var whiteCount = 0
        for (p in pixels) {
             if ((p and 0xFFFFFF) == 0xFFFFFF) whiteCount++
        }
        
        if (whiteCount > (pixels.size / 2)) {
            // Invert Image
            for (i in pixels.indices) {
                // Invert: White(0xFFFFFF) -> Black(0x000000), Black(0x000000) -> White(0xFFFFFF)
                // Since we only have pure BW from preprocessor, simple toggle works.
                pixels[i] = pixels[i] xor 0xFFFFFF
            }
        }
        
        // Update bitmap source for consistent blob extraction if we want (optional, but good for debug)
        // But we use the 'pixels' array for flood fill below.

        for (i in pixels.indices) {
            if (!visited[i] && isTextPixel(pixels[i])) {
                val blob = floodFill(pixels, visited, width, height, i)
                if (isValidBlob(blob)) {
                    blobs.add(createBlobBitmap(source, blob))
                }
            }
        }

        // Sort blobs to handle multi-line text (Sort by Y first, then X)
        // Groups roughly by line (threshold of 20px for Y)
        blobs.sortWith(Comparator { b1, b2 ->
            if (Math.abs(b1.y - b2.y) > 20) {
                b1.y - b2.y
            } else {
                b1.x - b2.x
            }
        })
        
        return blobs
    }

    private fun isTextPixel(color: Int): Boolean {
        // Check if white-ish (ImagePreprocessor sets pure WHITE for text)
        return (color and 0xFFFFFF) == 0xFFFFFF
    }

    private data class Rect(var minX: Int, var maxX: Int, var minY: Int, var maxY: Int, val points: MutableList<Int>)

    private fun floodFill(pixels: IntArray, visited: BooleanArray, w: Int, h: Int, startIndex: Int): Rect {
        val stack = Stack<Int>()
        stack.push(startIndex)
        visited[startIndex] = true

        val rect = Rect(
            minX = startIndex % w, maxX = startIndex % w,
            minY = startIndex / w, maxY = startIndex / w,
            points = mutableListOf()
        )

        while (stack.isNotEmpty()) {
            val idx = stack.pop()
            rect.points.add(idx)

            val x = idx % w
            val y = idx / w

            if (x < rect.minX) rect.minX = x
            if (x > rect.maxX) rect.maxX = x
            if (y < rect.minY) rect.minY = y
            if (y > rect.maxY) rect.maxY = y

            // 8-way connectivity for better character integrity
            val neighbors = intArrayOf(
                idx - 1, idx + 1, // Left, Right
                idx - w, idx + w, // Up, Down
                idx - w - 1, idx - w + 1, // Diagonals
                idx + w - 1, idx + w + 1
            )

            for (n in neighbors) {
                if (n in pixels.indices && !visited[n]) {
                    // Boundary checks
                    val nx = n % w
                    val ny = n / w
                    // Prevent wrapping around image edges
                    if (Math.abs(nx - x) <= 1 && Math.abs(ny - y) <= 1) {
                         if (isTextPixel(pixels[n])) {
                             visited[n] = true
                             stack.push(n)
                         }
                    }
                }
            }
        }
        return rect
    }

    private fun isValidBlob(rect: Rect): Boolean {
        val w = rect.maxX - rect.minX + 1
        val h = rect.maxY - rect.minY + 1
        
        // Filter Noise
        // 1. Too small (dust) - Relaxed from 3/8 to 2/6 for small screen text
        if (w < 2 || h < 6) return false
        
        // 2. Aspect ratio out of whack
        // Relaxed from 2.0 to 3.0 to allow wide digits (like '0' in some fonts)
        if (w > h * 3) return false
        
        return true
    }

    private fun createBlobBitmap(source: Bitmap, rect: Rect): Blob {
        val w = rect.maxX - rect.minX + 1
        val h = rect.maxY - rect.minY + 1
        
        val bitmap = Bitmap.createBitmap(source, rect.minX, rect.minY, w, h)
        return Blob(rect.minX, rect.minY, w, h, bitmap)
    }
}
