# Custom OCR System Design for College ID Cards

## 1. Project Overview
**Goal**: Build a high-accuracy, privacy-focused, offline OCR system specifically for extracting data from College ID Cards.  
**Constraints**:  
- No pre-trained models (e.g., Tesseract, ML Kit).  
- Custom-trained CNN model from scratch.  
- Fully offline (Android TFLite integration).  
- Domain-specific optimization (ID Cards).

---

## 2. System Architecture

The system is divided into three main phases:
1.  **Phase A: Model Training (Python)** - Generating datasets and training the character classifier.
2.  **Phase B: Image Processing Pipeline (Android/Kotlin)** - Preparing the raw camera image for the model.
3.  **Phase C: Inference & Reconstruction (Android/Kotlin)** - predicting characters and reconstructing text.

---

## 3. Phase A: Custom Model Training (Python)

Since we cannot use pre-trained weights, we will train a **Character Classification CNN** on a synthetic dataset designed to mimic ID card fonts.

### 3.1. Dataset Generation (Synthetic)
Instead of manually labeling thousands of ID cards, we will generate synthetic training data.

**Logic**:
1.  **Fonts**: Select 5-10 standard sans-serif fonts commonly used in ID cards (e.g., Arial, Roboto, Open Sans, Verdana).
2.  **Canvas**: Create a 28x28 pixel grayscale canvas.
3.  **Render**: Draw a single character (A-Z, 0-9) in the center.
4.  **Augmentation**:
    - **Rotation**: Random +/- 5 to 10 degrees.
    - **Scaling**: Zoom in/out by 10-15%.
    - **Translation**: Verify center alignment shift (+/- 2px).
    - **Noise**: Add salt-and-pepper noise to simulate printing artifacts.
    - **Blur**: Gaussian blur (kernel 3x3) to simulate poor focus.

**Output**:
- 36 Classes (0-9, A-Z).
- ~2,000 images per class.
- Total Dataset: ~72,000 images.

### 3.2. CNN Model Architecture
We will use a lightweight CNN suitable for mobile devices.

**Input**: 28 x 28 x 1 (Grayscale)  
**Architecture**:
1.  **Conv2D**: 32 filters, 3x3 kernel, ReLU activation.
2.  **MaxPooling**: 2x2.
3.  **Conv2D**: 64 filters, 3x3 kernel, ReLU activation.
4.  **MaxPooling**: 2x2.
5.  **Flatten**.
6.  **Dense (Fully Connected)**: 128 neurons, ReLU.
7.  **Dropout**: 0.5 (to prevent overfitting).
8.  **Output Layer**: 36 neurons, Softmax activation.

**Training Config**:
- **Loss Function**: Categorical Crossentropy.
- **Optimizer**: Adam.
- **Metrics**: Accuracy.

### 3.3. Export
- Train in Python (TensorFlow/Keras).
- Convert to **TensorFlow Lite (.tflite)** format with quantization (float16 or int8) to reduce size (< 1MB).

---

## 4. Phase B: Image Processing Pipeline (Android)

This occurs on the Android device when the user scans a card.

### 4.1. Preprocessing
1.  **Grayscale Conversion**: Convert bitmap to grayscale (0-255).
2.  **Adaptive Thresholding**:
    - Do NOT use a global threshold.
    - Use **Adaptive Thresholding** (e.g., calculate local mean) to handle uneven lighting/shadows.
    - *Formula*: `Pixel = (Value > (LocalMean - C)) ? 255 : 0`
    - Result: Binary black & white image.
3.  **Noise Removal**:
    - Apply morphological **Opening** (Erosion followed by Dilation) to remove small noise dots.
    - Apply **Closing** (Dilation followed by Erosion) to join broken character segments.

### 4.2. Alignment & Perspective Correction
*Optional but recommended for high accuracy:*
- Detect the 4 corners of the ID card using Canny Edge Detection + Contour finding.
- Apply `PerspectiveTransform` to flatten the card image to a standard aspect ratio (e.g., 800x500 px).

### 4.3. Region of Interest (ROI) Extraction
Based on the fixed ID card layout (from `CropUtils.kt` logic), extract specific strips:
1.  **College Name**: Top 20% height.
2.  **Student Name**: Middle section (~45% to ~65% height).
3.  **Register Number**: Bottom section (~80% to ~100% height).

---

## 5. Phase C: Character Segmentation & Inference

### 5.1. Character Segmentation (The most critical step)
Inside each ROI (e.g., the "Register Number" strip):
1.  **Find Contours**: Detect blobs of connected white pixels against black background.
2.  **Filter Contours**:
    - Discard contours that are too small (noise) or too large (borders).
    - Aspect ratio check: Characters roughly have a 1:2 to 1:1 width-height ratio.
3.  **Bounding Boxes**: Get the bounding rectangle `(x, y, w, h)` for each contour.
4.  **Sort**: Sort bounding boxes by `x` coordinate (left to right) to reconstruct the reading order.
5.  **Crop & Resize**:
    - Crop the character from the binary image using the box.
    - Resize to **28x28** pixels.
    - **Padding**: Ensure the character fits with some padding (approx 4px) so it looks like the training data.

### 5.2. Inference
1.  Normalize pixel values (0-255 -> 0.0-1.0).
2.  Pass the 28x28 buffer to the TFLite Interpreter.
3.  Get an array of 36 probabilities.
4.  **Confidence Check**:
    - `max_prob = max(output_array)`
    - `predicted_index = argmax(output_array)`
    - If `max_prob < 0.6` (threshold), mark as "Uncertain" or `?`.

### 5.3. Text Reconstruction
Concatenate predictions: `['K', 'L', 'U']` -> "KLU".

---

## 6. Phase D: Post-Processing & Validation

### 6.1. Domain-Specific Correction
1.  **Register Number Rules**:
    - Format: Alphanumeric or Numeric.
    - Fix common OCR confusions:
        - `0` (Number) vs `O` (Letter).
        - `1` (Number) vs `I` (Letter) vs `l` (lowercase L).
        - `8` vs `B`.
    - *Validation*: If the field is "Register Number", enforce strict rules (e.g., if existing ID format is known).

2.  **College Name Validation**:
    - Compute **Levenshtein Distance** against a known list of colleges.
    - If "KL University" is detected as "KL Univercity", auto-correct it.
    - If similarity < 80%, flag for manual review.

3.  **Student Name**:
    - Enforce Uppercase (standard for ID cards).
    - Reject numbers (names shouldn't improve digits).

---

## 7. Implementation Roadmap

### Step 1: Python - Dataset & Training
- Create `generate_dataset.py`.
- Create `train_model.py`.
- Run training and export `ocr_model.tflite`.

### Step 2: Android - Integration
- Add `tensorflow-lite` dependency.
- Copy `ocr_model.tflite` to `assets/`.
- Create `OcrEngine` class to load the model.

### Step 3: Android - Image Processing
- Improve `ImagePreprocessor` to implement Adaptive Thresholding.
- Implement `SegmentationUtils` to handle contour detection (using rudimentary pixel flooding or a lightweight library like OpenCV-Android if allowed, otherwise custom DFS/BFS for blob detection).

### Step 4: Testing
- Print dummy ID cards.
- Test under different lighting conditions.
- Tune confidence thresholds.
