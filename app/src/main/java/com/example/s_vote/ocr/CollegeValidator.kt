package com.example.s_vote.ocr

import kotlin.math.min

object CollegeValidator {

    fun validate(ocrText: String): String? {
        val cleanedOcr = ocrText.replace(Regex("[^A-Z0-9]"), "")

        // 1. Exact/Substring Match
        for (college in CollegeList.colleges) {
            val cleanCollege = college.replace(Regex("[^A-Z0-9]"), "")
            if (cleanedOcr.contains(cleanCollege)) {
                return college
            }
        }

        // 2. Fuzzy Match (Levenshtein)
        var bestMatch: String? = null
        var minDistance = Int.MAX_VALUE

        for (college in CollegeList.colleges) {
            val cleanCollege = college.replace(Regex("[^A-Z0-9]"), "")
            
            // Calculate distance against the whole OCR string (or best substring? simplify: whole string)
            // Note: If OCR has noise at start/end, direct distance is high.
            // But usually CropUtils isolates the name.
            
            val distance = calculateLevenshteinDistance(cleanedOcr, cleanCollege)
            
            val threshold = (cleanCollege.length * 0.4).toInt() // 40% tolerance for OCR errors

            if (distance <= threshold && distance < minDistance) {
                minDistance = distance
                bestMatch = college
            }
        }

        if (bestMatch != null) return bestMatch

        // 3. Fallback: Check for "SAVEETHA" keyword
        if (cleanedOcr.contains("SAVEETHA")) {
            // Return a default or the first one as a fallback to allow proceeding
            return "SAVEETHA ENGINEERING COLLEGE"
        }

        return null // INVALID
    }

    private fun calculateLevenshteinDistance(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        val s1Len = s1.length
        val s2Len = s2.length
        
        var cost = IntArray(s2Len + 1) { it }
        var newCost = IntArray(s2Len + 1)

        for (i in 0 until s1Len) {
            newCost[0] = i + 1
            for (j in 0 until s2Len) {
                val match = if (s1[i] == s2[j]) 0 else 1
                val costReplace = cost[j] + match
                val costInsert = cost[j + 1] + 1
                val costDelete = newCost[j] + 1
                newCost[j + 1] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[s2Len]
    }
}
