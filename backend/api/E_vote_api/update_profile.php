<?php
header('Content-Type: application/json');
include 'db_config.php';

// Check request method
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["success" => false, "message" => "Invalid request method"]);
    exit;
}

// Get input data
$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id'])) {
    echo json_encode(["success" => false, "message" => "User ID is required"]);
    exit;
}

$user_id = $conn->real_escape_string($data['user_id']);
$name = isset($data['name']) ? $conn->real_escape_string($data['name']) : null;
$department = isset($data['department']) ? $conn->real_escape_string($data['department']) : null;
$dob = isset($data['dob']) ? $conn->real_escape_string($data['dob']) : null;
$email = isset($data['email']) ? $conn->real_escape_string($data['email']) : null;
$student_id = isset($data['student_id']) ? $conn->real_escape_string($data['student_id']) : null;
$college = isset($data['college']) ? $conn->real_escape_string($data['college']) : null;
$profile_photo_base64 = isset($data['profile_photo']) ? $data['profile_photo'] : null;

$profile_photo_path = null;
if ($profile_photo_base64) {
    // Decode base64 photo
    $photo_data = explode(',', $profile_photo_base64);
    $decoded_photo = base64_decode(end($photo_data));
    $photo_name = "profile_" . $user_id . "_" . time() . ".jpg";
    $target_dir = "profile_photos/";
    
    if (!is_dir($target_dir)) {
        mkdir($target_dir, 0777, true);
    }
    
    $target_file = $target_dir . $photo_name;
    if (file_put_contents($target_file, $decoded_photo)) {
        $profile_photo_path = $target_file;
    }
}

// Construct SQL query dynamically based on provided fields
$updates = [];
if ($name !== null) $updates[] = "name = '$name'";
if ($department !== null) $updates[] = "department = '$department'";
if ($dob !== null) $updates[] = "dob = '$dob'";
if ($email !== null) $updates[] = "email = '$email'";
if ($student_id !== null) $updates[] = "student_id = '$student_id'";
if ($college !== null) $updates[] = "college = '$college'";
if ($profile_photo_path !== null) $updates[] = "profile_photo = '$profile_photo_path'";

if (empty($updates)) {
    echo json_encode(["success" => false, "message" => "No fields to update"]);
    exit;
}

$sql = "UPDATE users SET " . implode(", ", $updates) . " WHERE id = '$user_id'";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["success" => true, "message" => "Profile updated successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Error updating profile: " . $conn->error]);
}

$conn->close();
?>
