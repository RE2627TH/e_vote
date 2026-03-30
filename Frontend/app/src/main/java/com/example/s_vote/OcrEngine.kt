package com.example.s_vote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class OcrEngine(private val context: Context) {

    private val detector: Interpreter by lazy {
        loadModel("text_detector.tflite")
    }

    private val recognizer: Interpreter by lazy {
        loadModel("text_recognizer.tflite")
    }

    private val collegeWhitelist = listOf(
        "Saveetha College of Liberal Arts and Sciences",
        "Saveetha Medical College & Hospital",
        "Saveetha Dental College & Hospital",
        "Saveetha School of Engineering",
        "Saveetha School of Law",
        "Saveetha School of Management",
        "Saveetha College of Physiotherapy",
        "Saveetha College of Nursing",
        "Saveetha College of Pharmacy",
        "Saveetha College of Allied Health Sciences",
        "Saveetha College of Occupational Therapy",
        "Saveetha School of Physical Education"
    )

    private fun loadModel(modelPath: String): Interpreter {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val fileInputStream = assetFileDescriptor.createInputStream()
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val buffer = fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        return Interpreter(buffer)
    }

    fun processIdCard(bitmap: Bitmap): OcrResult {
        // 1. Preprocessing
        val grayBitmap = toGrayscale(bitmap)
        val resizedDetectorInput = Bitmap.createScaledBitmap(grayBitmap, 224, 224, true)
        
        // 2. Detection (Localization)
        val detectionBoxes = runDetection(resizedDetectorInput)
        
        // 3. Recognition (Extraction)
        val extractedTexts = mutableListOf<String>()
        val confidences = mutableListOf<Float>()
        
        for (box in detectionBoxes) {
            val roi = cropRoi(grayBitmap, box)
            val (text, confidence) = runRecognition(roi)
            extractedTexts.add(text)
            confidences.add(confidence)
        }
        
        // 4. Post-processing & Validation
        val college = fuzzyMatchCollege(extractedTexts.getOrNull(0) ?: "")
        val name = extractedTexts.getOrNull(1) ?: ""
        val regNo = extractedTexts.getOrNull(2) ?: ""
        
        val isValid = college != null && name.isNotEmpty() && regNo.isNotEmpty()
        
        return OcrResult(
            studentName = name,
            regNumber = regNo,
            collegeName = college ?: "Unknown / Invalid",
            isValid = isValid,
            confidence = confidences.average().toFloat()
        )
    }

    private fun runDetection(bitmap: Bitmap): List<DetectionBox> {
        val byteBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 1 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        
        val pixels = IntArray(224 * 224)
        bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)
        for (pixel in pixels) {
            val normValue = Color.red(pixel) / 255.0f
            byteBuffer.putFloat(normValue)
        }
        
        val output = Array(1) { FloatArray(12) }
        detector.run(byteBuffer, output)
        
        // Reshape output to 3 boxes
        val boxes = mutableListOf<DetectionBox>()
        for (i in 0 until 3) {
            val offset = i * 4
            boxes.add(DetectionBox(
                output[0][offset],     // ymin
                output[0][offset + 1], // xmin
                output[0][offset + 2], // ymax
                output[0][offset + 3]  // xmax
            ))
        }
        return boxes
    }

    private fun runRecognition(roi: Bitmap): Pair<String, Float> {
        val resized = Bitmap.createScaledBitmap(roi, 400, 100, true)
        val byteBuffer = ByteBuffer.allocateDirect(1 * 100 * 400 * 1 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        
        val pixels = IntArray(400 * 100)
        resized.getPixels(pixels, 0, 400, 0, 0, 400, 100)
        for (pixel in pixels) {
            byteBuffer.putFloat(Color.red(pixel) / 255.0f)
        }
        
        // Output from CRNN is (1, 100, 37)
        val output = Array(1) { Array(100) { FloatArray(37) } }
        recognizer.run(byteBuffer, output)
        
        return decodeCTC(output[0])
    }

    private fun decodeCTC(softmax: Array<FloatArray>): Pair<String, Float> {
        val charList = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ -/&."
        var decoded = ""
        var totalConfidence = 0f
        var lastCharIdx = -1
        
        for (timeStep in softmax) {
            val maxIdx = timeStep.indices.maxByOrNull { timeStep[it] } ?: 36
            val prob = timeStep[maxIdx]
            
            if (maxIdx != 36 && maxIdx != lastCharIdx) {
                decoded += charList[maxIdx]
                totalConfidence += prob
            }
            lastCharIdx = maxIdx
        }
        
        val avgConfidence = if (decoded.isNotEmpty()) totalConfidence / decoded.length else 0f
        return decoded.trim() to avgConfidence
    }

    private fun fuzzyMatchCollege(detected: String): String? {
        val threshold = 0.7f
        var bestMatch: String? = null
        var maxScore = 0f
        
        for (college in collegeWhitelist) {
            val score = levenshteinSimilarity(detected.lowercase(), college.lowercase())
            if (score > maxScore && score >= threshold) {
                maxScore = score
                bestMatch = college
            }
        }
        return bestMatch
    }

    private fun levenshteinSimilarity(s1: String, s2: String): Float {
        val maxLength = maxOf(s1.length, s2.length)
        if (maxLength == 0) return 1.0f
        val distance = levenshteinDistance(s1, s2)
        return 1.0f - (distance.toFloat() / maxLength)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }

    private fun toGrayscale(bmp: Bitmap): Bitmap {
        val grayBmp = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.RGB_565)
        for (x in 0 until bmp.width) {
            for (y in 0 until bmp.height) {
                val pixel = bmp.getPixel(x, y)
                val avg = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                grayBmp.setPixel(x, y, Color.rgb(avg, avg, avg))
            }
        }
        return grayBmp
    }

    private fun cropRoi(bitmap: Bitmap, box: DetectionBox): Bitmap {
        val x = (box.xmin * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
        val y = (box.ymin * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
        val w = ((box.xmax - box.xmin) * bitmap.width).toInt().coerceAtLeast(1).coerceAtMost(bitmap.width - x)
        val h = ((box.ymax - box.ymin) * bitmap.height).toInt().coerceAtLeast(1).coerceAtMost(bitmap.height - y)
        return Bitmap.createBitmap(bitmap, x, y, w, h)
    }

    data class DetectionBox(val ymin: Float, val xmin: Float, val ymax: Float, val xmax: Float)
    data class OcrResult(
        val studentName: String,
        val regNumber: String,
        val collegeName: String,
        val isValid: Boolean,
        val confidence: Float
    )
}
