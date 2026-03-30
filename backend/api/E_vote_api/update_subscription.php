<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);
$user_id = $data['user_id'] ?? null;
$payment_id = $data['payment_id'] ?? 'MOCK_PAYMENT_' . time();

if (!$user_id) {
    echo json_encode(["success" => false, "message" => "User ID required"]);
    exit;
}

$sql = "UPDATE users SET is_subscribed = 1, payment_id = '$payment_id' WHERE id = '$user_id'";

if ($conn->query($sql)) {
    echo json_encode(["success" => true, "message" => "Subscription updated successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
}
?>
