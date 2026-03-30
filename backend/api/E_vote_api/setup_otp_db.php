<?php
header('Content-Type: application/json');
include 'db_config.php';

$sql = "CREATE TABLE IF NOT EXISTS email_otps (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)";

if ($conn->query($sql)) {
    echo json_encode(["success" => true, "message" => "OTP table created successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Error creating table: " . $conn->error]);
}
?>
