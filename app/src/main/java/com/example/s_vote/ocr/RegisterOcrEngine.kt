package com.example.s_vote.ocr

import android.content.Context
import android.graphics.Bitmap

object RegisterOcrEngine {

    fun readRegisterNumber(context: Context, bitmap: Bitmap): String {
        // 1. Dataset
        val dataset = DatasetLoader.loadDataset(context)
        
        // 2. Preprocess (Enhance & Binarize)
        val preprocessed = ImagePreprocessor.applyThreshold(bitmap) // Uses new smart threshold
        
        // 3. Blob Extraction (Segmentation)
        // This is much better than vertical projection for finding digits
        val blobs = BlobExtractor.extractBlobs(preprocessed)
        
        val result = StringBuilder()
        
        for (blob in blobs) {
            // 4. Recognition
            val match = SmartOcrMatcher.recognizeDigit(blob.bitmap, dataset)
            
        // 4. Line Grouping & Recognition
        val candidates = mutableListOf<String>()
        var currentSequence = StringBuilder()
        var lastY = -1
        var lastX = -1
        val NEW_LINE_THRESHOLD = 30 // pixels
        val NEW_WORD_THRESHOLD = 50 // pixels gap

        for (i in blobs.indices) {
            val blob = blobs[i]
            
            // Check for Line Break (Vertical Gap)
            if (lastY != -1 && Math.abs(blob.y - lastY) > NEW_LINE_THRESHOLD) {
                // End of line
                if (currentSequence.length >= 5) candidates.add(currentSequence.toString())
                currentSequence.clear()
            } 
            // Check for Word Break (Horizontal Gap on same line)
            else if (lastX != -1 && (blob.x - (lastX + 20)) > NEW_WORD_THRESHOLD) { // +20 assumes avg char width
                 // End of word
                 if (currentSequence.length >= 5) candidates.add(currentSequence.toString())
                 currentSequence.clear()
            }

            val match = SmartOcrMatcher.recognizeDigit(blob.bitmap, dataset)
            
            // Relaxed confidence
            if (match.confidence > 0.45f) {
                currentSequence.append(match.char)
            }
            
            lastY = blob.y
            lastX = blob.x + blob.width
        }
        // Add final sequence
        if (currentSequence.length >= 5) candidates.add(currentSequence.toString())

        // 5. Select Best Candidate
        // User said "numbers at the last". So we prefer the last VALID candidate.
        // Valid = contains at least 6 digits
        val bestMatch = candidates.lastOrNull { it.length >= 6 } ?: candidates.lastOrNull() ?: ""
        
        return bestMatch
    }
}
