import os
import cv2
import numpy as np
import tensorflow as tf
from flask import Flask, request, jsonify
from flask_cors import CORS
import time

app = Flask(__name__)
CORS(app)

# Configuration
MODEL_PATH = "id_card_ocr_model.h5"
IMG_SIZE = 28
CLASSES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

# Load the custom-trained model
print("--- Initializing OCR Server ---")
if os.path.exists(MODEL_PATH):
    try:
        model = tf.keras.models.load_model(MODEL_PATH)
        print(f"SUCCESS: Model loaded from {MODEL_PATH}")
    except Exception as e:
        print(f"ERROR: Could not load model: {e}")
        model = None
else:
    print(f"WARNING: Model {MODEL_PATH} not found. Please train the model first.")
    model = None

def preprocess_image(image_bytes):
    """Convert bytes to a grayscale OpenCV image."""
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if img is None:
        return None
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    return gray

def segment_and_group_lines(roi_gray, label="ROI"):
    """
    Segment characters and group them into lines.
    Returns a list of characters from the 'best' line for that ROI.
    """
    start_time = time.time()
    # 1. Enhanced Preprocessing for noise reduction
    blurred = cv2.GaussianBlur(roi_gray, (3, 3), 0)
    thresh = cv2.adaptiveThreshold(
        blurred, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, 
        cv2.THRESH_BINARY_INV, 11, 2
    )

    # 2. Find contours
    contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    
    char_boxes = []
    for cnt in contours:
        x, y, w, h = cv2.boundingRect(cnt)
        # Stricter filtering for characters on ID card
        area = w * h
        aspect_ratio = w / float(h)
        if 8 < w < 100 and 15 < h < 150 and 100 < area < 5000 and 0.1 < aspect_ratio < 1.5:
            char_boxes.append((x, y, w, h))
    
    if not char_boxes:
        return []

    # 3. Group into Lines (boxes with similar Y)
    char_boxes.sort(key=lambda b: b[1]) # Sort by Y
    lines = []
    if char_boxes:
        current_line = [char_boxes[0]]
        for i in range(1, len(char_boxes)):
            # If Y is close enough to previous line start (within half height)
            if abs(char_boxes[i][1] - current_line[0][1]) < (current_line[0][3] * 0.5):
                current_line.append(char_boxes[i])
            else:
                lines.append(current_line)
                current_line = [char_boxes[i]]
        lines.append(current_line)

    # 4. Pick the "Best" line
    # For ID: usually the one with specific length/numeric density
    # For now, pick the line with the most characters that isn't too long (noise)
    best_line = []
    max_score = -1
    for line in lines:
        if len(line) > 50: continue # Likely noise/keyboard
        score = len(line)
        if score > max_score:
            max_score = score
            best_line = line

    if not best_line:
        return []

    # Sort best line left to right
    best_line.sort(key=lambda b: b[0])
    
    segments = []
    for x, y, w, h in best_line:
        char_img = thresh[y:y+h, x:x+w]
        pad = max(w, h) + 6
        square_char = np.zeros((pad, pad), dtype=np.uint8)
        dx = (pad - w) // 2
        dy = (pad - h) // 2
        square_char[dy:dy+h, dx:dx+w] = char_img
        char_resized = cv2.resize(square_char, (IMG_SIZE, IMG_SIZE), interpolation=cv2.INTER_AREA)
        segments.append(char_resized)
        
    print(f"DEBUG: {label} - Found {len(lines)} lines, Best line has {len(segments)} characters. (Time: {time.time() - start_time:.4f}s)")
    return segments

def predict_text(segments, max_len=20, only_digits=False):
    """Predict characters and clean up results."""
    if model is None or not segments:
        return ""
    
    # Limit segments to avoid massive outputs
    segments = segments[:max_len]
    
    text = ""
    try:
        batch = []
        for char_img in segments:
            img_input = char_img.astype('float32') / 255.0
            img_input = np.expand_dims(img_input, axis=-1)
            batch.append(img_input)
        
        if batch:
            batch_np = np.array(batch)
            preds = model.predict(batch_np, verbose=0)
            for p in preds:
                if only_digits:
                    # Filter for digits 0-9 (first 10 classes)
                    digit_preds = p[:10]
                    idx = np.argmax(digit_preds)
                else:
                    idx = np.argmax(p)
                text += CLASSES[idx]
    except Exception as e:
        print(f"ERROR during prediction: {e}")
        
    return text

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy", "server_time": time.ctime()})

@app.route('/ocr', methods=['POST'])
def ocr():
    request_start = time.time()
    print(f"\n[{time.ctime()}] Received OCR Request (V2 - Strict)")
    
    if 'image' not in request.files:
        return jsonify({"error": "No image uploaded"}), 400
    
    file = request.files['image']
    img_bytes = file.read()
    img_gray = preprocess_image(img_bytes)
    
    if img_gray is None:
        return jsonify({"error": "Invalid image format"}), 400

    height, width = img_gray.shape
    
    # Noise/Brightness Check
    avg_brightness = np.mean(img_gray)
    if avg_brightness < 30:
        return jsonify({"error": "Image is too dark. Please use better lighting."}), 400
    
    # Refined ROIs for College ID
    college_roi = img_gray[int(height*0.02):int(height*0.25), :]
    name_roi = img_gray[int(height*0.35):int(height*0.65), :]
    id_roi = img_gray[int(height*0.75):int(height*0.98), :]
    
    def extract_with_sanity(roi, label, max_len, only_digits=False):
        segs = segment_and_group_lines(roi, label)
        # If we find more than 40 possible characters in a single ROI strip, it's likely noise (keyboard)
        if len(segs) > 40:
            print(f"WARNING: Rejecting {label} due to excessive noise ({len(segs)} chars)")
            return ""
        return predict_text(segs, max_len, only_digits=only_digits)

    scanned_id = extract_with_sanity(id_roi, "ID", 15, only_digits=True)
    
    # QUALITY CHECK:
    # A real student ID usually has a specific structure. 
    # Junk from a keyboard (444122724593216) is usually too long or repetitive.
    digit_count = sum(c.isdigit() for c in scanned_id)
    is_junk = False
    
    # Relaxed filtering for Manual Override mode
    # Even if it looks like junk, we return it if it has at least 3 digits
    # so the user can fix the one or two wrong characters.
    if len(scanned_id) < 3:
        is_junk = True
    elif len(scanned_id) > 20: # Higher limit
        is_junk = True
    elif not any(c.isdigit() for c in scanned_id):
        is_junk = True
    
    # Check for excessive repetition (e.g. 444444) - relaxed to 6
    for i in range(len(scanned_id) - 5):
        if scanned_id[i] == scanned_id[i+1] == scanned_id[i+2] == scanned_id[i+3] == scanned_id[i+4] == scanned_id[i+5]:
            is_junk = True
            break
            
    # If the ID has many 'K' or 'X' from keyboard scans
    if scanned_id.count('K') > 5 or scanned_id.count('X') > 5:
        is_junk = True

    results = {
        "college_name": extract_with_sanity(college_roi, "College", 30),
        "student_name": extract_with_sanity(name_roi, "Name", 25),
        "student_id": scanned_id, # Always return the raw ID, never empty
        "is_quality_scan": not is_junk,
        "version": "2.4",
        "processed_at": time.ctime()
    }
    
    duration = time.time() - request_start
    print(f"SUCCESS: Result: {results} (Time: {duration:.2f}s)")
    return jsonify(results)

if __name__ == '__main__':
    print(f"INFO: Server starting on http://0.0.0.0:5000")
    print(f"INFO: Use your computer's IP (10.57.135.24) to connect from Android.")
    app.run(host='0.0.0.0', port=5000, debug=False) # Debug False for stability
