<?php
header('Content-Type: application/json');

$target_dir = "uploads/";
if (!file_exists($target_dir)) {
    mkdir($target_dir, 0777, true);
}

// Check if file is provided
if (!isset($_FILES['image'])) {
    echo json_encode(["success" => false, "message" => "No image uploaded"]);
    exit();
}

$file_name = basename($_FILES["image"]["name"]);
$target_file = $target_dir . time() . "_" . $file_name; // Unique name
$imageFileType = strtolower(pathinfo($target_file, PATHINFO_EXTENSION));

// Check if image file is actual image
$check = getimagesize($_FILES["image"]["tmp_name"]);
if ($check === false) {
    echo json_encode(["success" => false, "message" => "File is not an image."]);
    exit();
}

// Check file size (5MB limit)
if ($_FILES["image"]["size"] > 5000000) {
    echo json_encode(["success" => false, "message" => "Sorry, your file is too large."]);
    exit();
}

// Allow certain file formats
if ($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg" && $imageFileType != "gif") {
    echo json_encode(["success" => false, "message" => "Sorry, only JPG, JPEG, PNG & GIF files are allowed."]);
    exit();
}

if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_file)) {
    // Return the full URL or relative path
    // Assuming server IP is handled by client, returning relative path
    echo json_encode(["success" => true, "message" => "Upload successful", "file_path" => $target_file]);
} else {
    echo json_encode(["success" => false, "message" => "Sorry, there was an error uploading your file."]);
}
?>
