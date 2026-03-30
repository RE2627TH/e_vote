<?php
header('Content-Type: application/json');
include 'db_config.php';

$user_id = $_GET['user_id'] ?? null;

if (!$user_id) {
    echo json_encode(["success" => false, "message" => "User ID required"]);
    exit;
}

$sql = "SELECT * FROM notifications WHERE user_id = '$user_id' ORDER BY created_at DESC";
$result = $conn->query($sql);

$notifications = [];
if ($result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $notifications[] = $row;
    }
}

// Also mark as read
$conn->query("UPDATE notifications SET is_read = 1 WHERE user_id = '$user_id'");

echo json_encode(["success" => true, "notifications" => $notifications]);

$conn->close();
?>
