import tensorflow as tf
from tensorflow.keras import layers, models, backend as K

def ctc_loss_lambda_func(args):
    y_true, y_pred, input_length, label_length = args
    return K.ctc_batch_cost(y_true, y_pred, input_length, label_length)

def build_crnn_model(img_width, img_height, num_classes, max_label_len):
    """
    CRNN Model: 
    CNN (Feature Extraction) -> Reshape -> RNN (Sequence Modeling) -> Dense (Time distributed)
    """
    # 1. CNN Feature Extractor
    input_img = layers.Input(shape=(img_height, img_width, 1), name='image_input')
    
    x = layers.Conv2D(32, (3, 3), activation='relu', padding='same')(input_img)
    x = layers.MaxPooling2D((2, 2))(x)
    
    x = layers.Conv2D(64, (3, 3), activation='relu', padding='same')(x)
    x = layers.MaxPooling2D((2, 2))(x)
    
    x = layers.Conv2D(128, (3, 3), activation='relu', padding='same')(x)
    x = layers.MaxPooling2D((1, 2))(x) # Keep width resolution higher
    
    # 2. Reshape for RNN
    # After 3 pooling layers: width = img_width / 4, height = img_height / 8
    # We want (time_steps, features)
    curr_width = img_width // 4
    curr_height = img_height // 8
    
    x = layers.Reshape(target_shape=(curr_width, curr_height * 128))(x)
    
    # 3. RNN (Bi-LSTM)
    x = layers.Bidirectional(layers.LSTM(128, return_sequences=True))(x)
    x = layers.Bidirectional(layers.LSTM(128, return_sequences=True))(x)
    
    # 4. Output Layer
    # num_classes + 1 for CTC blank symbol
    y_pred = layers.Dense(num_classes + 1, activation='softmax', name='softmax_output')(x)
    
    # --- CTC Setup ---
    labels = layers.Input(name='the_labels', shape=[max_label_len], dtype='float32')
    input_length = layers.Input(name='input_length', shape=[1], dtype='int64')
    label_length = layers.Input(name='label_length', shape=[1], dtype='int64')
    
    # CTC loss calculation
    loss_out = layers.Lambda(ctc_loss_lambda_func, output_shape=(1,), name='ctc')([labels, y_pred, input_length, label_length])
    
    # Model for training
    train_model = models.Model(inputs=[input_img, labels, input_length, label_length], outputs=loss_out)
    
    # Model for inference
    prediction_model = models.Model(inputs=input_img, outputs=y_pred)
    
    return train_model, prediction_model

if __name__ == "__main__":
    # Test build
    train, pred = build_crnn_model(400, 100, 37, 32)
    train.summary()
