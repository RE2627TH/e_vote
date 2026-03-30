import os
import random
import numpy as np
import cv2
from PIL import Image, ImageDraw, ImageFont, ImageFilter

# Configuration
OUTPUT_DIR = "dataset"
IMG_SIZE = 28
SAMPLES_PER_CLASS = 2000

# Updated with Windows System Font Paths
FONT_DIR = "C:/Windows/Fonts/"
FONTS = [
    os.path.join(FONT_DIR, "arial.ttf"), 
    os.path.join(FONT_DIR, "cour.ttf"),   # Courier New
    os.path.join(FONT_DIR, "tahoma.ttf"), 
    os.path.join(FONT_DIR, "verdana.ttf"),
    os.path.join(FONT_DIR, "impact.ttf"),
    os.path.join(FONT_DIR, "segoeui.ttf") # Added Segoe UI
]
CLASSES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

def create_directory(path):
    if not os.path.exists(path):
        os.makedirs(path)

def generate_character_image(char, font_path):
    # 1. Create a larger canvas to avoid clipping during rotation
    full_size = 64
    image = Image.new('L', (full_size, full_size), color=0) # Black background
    draw = ImageDraw.Draw(image)
    
    # 2. Random Font Size
    font_size = random.randint(22, 38)
    try:
        if os.path.exists(font_path):
            font = ImageFont.truetype(font_path, font_size)
        else:
            font = ImageFont.load_default()
    except Exception:
        font = ImageFont.load_default()

    # 3. Draw Character Centered
    bbox = font.getbbox(char) 
    tw, th = bbox[2] - bbox[0], bbox[3] - bbox[1]
    
    x = (full_size - tw) / 2 - bbox[0] + random.randint(-3, 3)
    y = (full_size - th) / 2 - bbox[1] + random.randint(-3, 3)
    
    draw.text((x, y), char, font=font, fill=255)

    # 4. Augmentations
    
    # Rotation
    angle = random.randint(-12, 12)
    image = image.rotate(angle, resample=Image.BILINEAR)

    # Convert to Numpy for OpenCV ops
    img_np = np.array(image)

    # THICKNESS AUGMENTATION (Dilation/Erosion)
    # This simulates bold/thin printing on cards
    if random.random() < 0.3:
        kernel = np.ones((2, 2), np.uint8)
        if random.random() < 0.5:
            img_np = cv2.dilate(img_np, kernel, iterations=1)
        else:
            img_np = cv2.erode(img_np, kernel, iterations=1)

    # Noise (Salt and Pepper)
    noise_prob = 0.015
    noise_mask = np.random.rand(*img_np.shape) < noise_prob
    img_np[noise_mask] = 255 * np.random.randint(0, 2, size=img_np[noise_mask].shape) 

    # Blur / Sharpen
    if random.random() < 0.4:
        img_np = cv2.GaussianBlur(img_np, (3, 3), 0)
    elif random.random() < 0.2:
        # Simple Sharpening kernel
        kernel = np.array([[0, -1, 0], [-1, 5, -1], [0, -1, 0]])
        img_np = cv2.filter2D(img_np, -1, kernel)

    # Resize to final 28x28
    img_final = cv2.resize(img_np, (IMG_SIZE, IMG_SIZE), interpolation=cv2.INTER_AREA)

    return img_final

def main():
    print(f"Generating dataset in '{OUTPUT_DIR}'...")
    create_directory(OUTPUT_DIR)

    # Loop through each class (0-9, A-Z)
    for char_class in CLASSES:
        class_dir = os.path.join(OUTPUT_DIR, char_class)
        create_directory(class_dir)
        
        print(f"Generating {SAMPLES_PER_CLASS} images for class '{char_class}'...")
        
        for i in range(SAMPLES_PER_CLASS):
            # Pick a random font
            font = random.choice(FONTS)
            
            # Generate image
            img = generate_character_image(char_class, font)
            
            # Save
            filename = os.path.join(class_dir, f"{char_class}_{i}.png")
            cv2.imwrite(filename, img)

    print("Dataset generation complete!")

if __name__ == "__main__":
    main()
