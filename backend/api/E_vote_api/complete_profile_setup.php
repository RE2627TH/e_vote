<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id']) || !isset($data['college'])) {
    echo json_encode(["success" => false, "message" => "Required fields missing"]);
    exit;
}

$user_id = $conn->real_escape_string($data['user_id']);
$college = $conn->real_escape_string($data['college']);
$photo_data = isset($data['profile_photo']) ? $data['profile_photo'] : null;

$photo_path = null;

if ($photo_data) {
    // Create directory if not exists
    $target_dir = "profile_photos/";
    if (!file_exists($target_dir)) {
        mkdir($target_dir, 0777, true);
    }

    // Decode base64 image
    $image_parts = explode(";base64,", $photo_data);
    $image_type_aux = explode("image/", $image_parts[0]);
    $image_type = $image_type_aux[1];
    $image_base64 = base64_decode($image_parts[1]);
    
    $file_name = "profile_" . $user_id . "_" . time() . "." . $image_type;
    $file_path = $target_dir . $file_name;

    if (file_put_contents($file_path, $image_base64)) {
        $photo_path = $file_path;
    }
}

// Update user record
$sql = "UPDATE users SET college = '$college', is_profile_completed = 1";
if ($photo_path) {
    $sql .= ", profile_photo = '$photo_path'";
}
$sql .= " WHERE id = '$user_id'";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["success" => true, "message" => "Profile setup completed successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Error updating profile: " . $conn->error]);
}

$conn->close();
?>
