import tensorflow as tf
from tensorflow.keras import layers, models

def build_detector_model(img_size=(224, 224)):
    """
    A lightweight detector to find 3 ROIs: College Name, Student Name, Reg Number.
    Output: 3 bounding boxes (y_min, x_min, y_max, x_max) -> 12 values
    """
    input_img = layers.Input(shape=(img_size[0], img_size[1], 1))
    
    # Feature Extraction
    x = layers.Conv2D(16, (3, 3), activation='relu', padding='same')(input_img)
    x = layers.MaxPooling2D((2, 2))(x) # 112
    
    x = layers.Conv2D(32, (3, 3), activation='relu', padding='same')(x)
    x = layers.MaxPooling2D((2, 2))(x) # 56
    
    x = layers.Conv2D(64, (3, 3), activation='relu', padding='same')(x)
    x = layers.MaxPooling2D((2, 2))(x) # 28
    
    x = layers.Conv2D(128, (3, 3), activation='relu', padding='same')(x)
    x = layers.GlobalAveragePooling2D()(x)
    
    # Regression Head
    x = layers.Dense(128, activation='relu')(x)
    x = layers.Dropout(0.2)(x)
    
    # 3 Boxes * 4 coords = 12 outputs
    outputs = layers.Dense(12, activation='sigmoid')(x) 
    
    model = models.Model(inputs=input_img, outputs=outputs)
    model.compile(optimizer='adam', loss='mse', metrics=['mae'])
    
    return model

if __name__ == "__main__":
    model = build_detector_model()
    model.summary()
