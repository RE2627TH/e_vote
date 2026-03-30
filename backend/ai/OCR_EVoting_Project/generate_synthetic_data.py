import os
import random
import numpy as np
import cv2
from PIL import Image, ImageDraw, ImageFont, ImageFilter

# --- CONFIGURATION ---
DATASET_ROOT = "dataset/train"
COLLEGE_NAMES = [
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
]

INDIAN_NAMES = ["AADITYA", "ARUN", "DEEPAK", "ISHANI", "KARTHIK", "LAKSHMI", "MEENA", "POOJA", "RAHUL", "SNEHA", "VIVEK", "YAMINI"]
FONT_DIR = "C:/Windows/Fonts/"
FONTS = [
    os.path.join(FONT_DIR, "arial.ttf"),
    os.path.join(FONT_DIR, "tahoma.ttf"),
    os.path.join(FONT_DIR, "verdana.ttf"),
    os.path.join(FONT_DIR, "segoeui.ttf")
]

IMG_WIDTH = 400
IMG_HEIGHT = 100
MAX_SAMPLES = 5000 # Total samples for CRNN training

def create_directory(path):
    if not os.path.exists(path):
        os.makedirs(path)

def apply_augmentations(img_np):
    # Grayscale conversion (ensure)
    if len(img_np.shape) == 3:
        img_np = cv2.cvtColor(img_np, cv2.COLOR_BGR2GRAY)
    
    # 1. Perspective Distortion
    h, w = img_np.shape
    pts1 = np.float32([[0,0],[w,0],[0,h],[w,h]])
    offset = random.randint(0, 10)
    pts2 = np.float32([[offset,offset],[w-offset,offset],[offset,h-offset],[w-offset,h-offset]])
    M = cv2.getPerspectiveTransform(pts1, pts2)
    img_np = cv2.warpPerspective(img_np, M, (w, h), borderValue=255)

    # 2. Blur / Noise
    if random.random() < 0.3:
        img_np = cv2.GaussianBlur(img_np, (3, 3), 0)
    
    # 3. Random Brightness/Contrast
    alpha = random.uniform(0.7, 1.3) # Contrast
    beta = random.randint(-30, 30)   # Brightness
    img_np = cv2.convertScaleAbs(img_np, alpha=alpha, beta=beta)
    
    # 4. Salt & Pepper Noise
    if random.random() < 0.2:
        noise = np.random.randint(0, 2, img_np.shape).astype(np.uint8) * 255
        img_np = cv2.addWeighted(img_np, 0.95, noise, 0.05, 0)

    return img_np

def generate_text_line(text, font_path, size=(IMG_WIDTH, IMG_HEIGHT)):
    # Create white canvas
    img = Image.new('L', size, color=255)
    draw = ImageDraw.Draw(img)
    
    # Random font size relative to container
    font_size = random.randint(24, 40)
    try:
        font = ImageFont.truetype(font_path, font_size)
    except:
        font = ImageFont.load_default()
        
    # Draw text centered vertically, left aligned with padding
    bbox = draw.textbbox((20, 0), text, font=font)
    th = bbox[3] - bbox[1]
    y_pos = (size[1] - th) // 2
    draw.text((20, y_pos), text, font=font, fill=0) # Black text
    
    img_np = np.array(img)
    img_np = apply_augmentations(img_np)
    
    return img_np

def main():
    print(f"Generating {MAX_SAMPLES} synthetic samples for CRNN training...")
    create_directory(DATASET_ROOT)
    
    # Save a labels.txt file for CRNN training
    with open(os.path.join(DATASET_ROOT, "labels.txt"), "w") as f:
        for i in range(MAX_SAMPLES):
            # Select random type: College, Name, or RegNo
            category = random.choice(["college", "name", "regno"])
            
            if category == "college":
                text = random.choice(COLLEGE_NAMES)
            elif category == "name":
                text = f"{random.choice(INDIAN_NAMES)} {random.choice(INDIAN_NAMES)}"
            else:
                # Alphanumeric RegNo
                text = f"202{''.join(random.choices('0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ', k=7))}"
            
            font = random.choice(FONTS)
            img = generate_text_line(text, font)
            
            filename = f"sample_{i}.png"
            cv2.imwrite(os.path.join(DATASET_ROOT, filename), img)
            f.write(f"{filename}\t{text}\n")
            
            if i % 500 == 0:
                print(f"Generated {i} images...")

    print("Synthetic dataset generation complete!")

if __name__ == "__main__":
    main()
