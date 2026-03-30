<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id']) || !isset($data['fcm_token'])) {
    echo json_encode(["success" => false, "message" => "Required fields missing"]);
    exit;
}

$user_id = $conn->real_escape_string($data['user_id']);
$fcm_token = $conn->real_escape_string($data['fcm_token']);

// Update the user's fcm_token
$sql = "UPDATE users SET fcm_token = '$fcm_token' WHERE id = '$user_id'";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["success" => true, "message" => "FCM token updated successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Error updating FCM token: " . $conn->query_error]);
}

$conn->close();
?>
