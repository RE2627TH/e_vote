<?php
include 'db_config.php';

// Add is_profile_completed and profile_photo columns to users table
$sql = "ALTER TABLE users 
        ADD COLUMN is_profile_completed TINYINT(1) DEFAULT 0,
        ADD COLUMN profile_photo VARCHAR(255) DEFAULT NULL";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["success" => true, "message" => "Users table updated for profile setup"]);
} else {
    echo json_encode(["success" => false, "message" => "Error updating table: " . $conn->error]);
}

$conn->close();
?>
