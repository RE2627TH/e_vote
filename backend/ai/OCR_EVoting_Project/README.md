# Custom OCR Model Training Instructions

Follow these steps to generate the dataset and train your custom OCR model from scratch.

## Prerequisites
- Python 3.8 or higher installed.

## Step 1: Install Dependencies
Open your terminal in this directory (`c:\OCR_EVoting_Project`) and run:

```bash
pip install -r requirements.txt
```

## Step 2: Generate Synthetic Dataset
This script will generate ~72,000 synthetic images of characters (A-Z, 0-9) ensuring they look like printed ID card text (with noise, rotation, blur).

```bash
python generate_dataset.py
```
*Output: A `dataset` folder will be created with subfolders for each character.*

## Step 3: Train the Model
This script trains a CNN from scratch on the generated dataset and automatically converts it to TensorFlow Lite format.

```bash
python train_model.py
```
*Output: `id_card_ocr_model.tflite` will be generated.*

## Step 4: Android Integration
Copy the generated `id_card_ocr_model.tflite` file into your Android project's `assets` folder:
`s-vote/app/src/main/assets/`
