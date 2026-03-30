import os
import numpy as np
import tensorflow as tf
from tensorflow.keras import layers, models
from tensorflow.keras.preprocessing.image import ImageDataGenerator
import shutil

# Configuration
DATASET_DIR = "dataset"
MODEL_PATH = "id_card_ocr_model.h5"
TFLITE_MODEL_PATH = "id_card_ocr_model.tflite"
IMG_SIZE = 28
BATCH_SIZE = 32
EPOCHS = 10
NUM_CLASSES = 36 # 0-9, A-Z

def load_data():
    print("Loading data from directory...")
    
    # Use Keras utility to load data
    # Rescaling 1./255 normalizes pixels to 0-1 range
    datagen = ImageDataGenerator(
        rescale=1./255, 
        validation_split=0.2 # 80% Train, 20% Validation
    )

    train_generator = datagen.flow_from_directory(
        DATASET_DIR,
        target_size=(IMG_SIZE, IMG_SIZE),
        batch_size=BATCH_SIZE,
        color_mode='grayscale',
        class_mode='categorical',
        subset='training'
    )

    validation_generator = datagen.flow_from_directory(
        DATASET_DIR,
        target_size=(IMG_SIZE, IMG_SIZE),
        batch_size=BATCH_SIZE,
        color_mode='grayscale',
        class_mode='categorical',
        subset='validation'
    )

    return train_generator, validation_generator

def build_model():
    print("Building CNN Model...")
    model = models.Sequential([
        # input shape: 28x28 images, 1 channel (grayscale)
        layers.Input(shape=(IMG_SIZE, IMG_SIZE, 1)),
        
        # First Convolutional Block
        layers.Conv2D(32, (3, 3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        
        # Second Convolutional Block
        layers.Conv2D(64, (3, 3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        
        # Flatten and Dense
        layers.Flatten(),
        layers.Dense(128, activation='relu'),
        layers.Dropout(0.5), # Regularization to prevent overfitting
        
        # Output Layer
        layers.Dense(NUM_CLASSES, activation='softmax')
    ])

    model.compile(optimizer='adam',
                  loss='categorical_crossentropy',
                  metrics=['accuracy'])
    
    model.summary()
    return model

def train(model, train_gen, val_gen):
    print("Starting training...")
    
    # Stop training when validation loss stops improving
    early_stop = tf.keras.callbacks.EarlyStopping(
        monitor='val_loss', 
        patience=3, 
        restore_best_weights=True
    )
    
    history = model.fit(
        train_gen,
        epochs=EPOCHS,
        validation_data=val_gen,
        callbacks=[early_stop]
    )
    return history

def convert_to_tflite(model):
    print("Converting to TensorFlow Lite...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Optimization: Quantization (makes it smaller and faster for mobile)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    tflite_model = converter.convert()
    
    with open(TFLITE_MODEL_PATH, 'wb') as f:
        f.write(tflite_model)
    
    print(f"TFLite model saved to {TFLITE_MODEL_PATH}")

def main():
    if not os.path.exists(DATASET_DIR):
        print(f"Error: Dataset directory '{DATASET_DIR}' not found. Run generate_dataset.py first.")
        return

    train_gen, val_gen = load_data()
    
    # Verify classes
    print("Class mapping:", train_gen.class_indices)
    
    model = build_model()
    train(model, train_gen, val_gen)
    
    print(f"Saving Keras model to {MODEL_PATH}...")
    model.save(MODEL_PATH)
    
    convert_to_tflite(model)
    print("Done! You can now copy the .tflite file to your Android project.")

if __name__ == "__main__":
    main()
