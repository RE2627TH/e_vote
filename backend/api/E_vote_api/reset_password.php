<?php
header("Content-Type: application/json");
include "db_config.php";

$data = json_decode(file_get_contents("php://input"), true);

$student_id = trim($data["student_id"] ?? "");
$email = trim($data["email"] ?? "");
$new_password = $data["password"] ?? "";

if (empty($student_id) || empty($email) || empty($new_password)) {
    echo json_encode([
        "success" => false,
        "message" => "Student ID, Email, and New Password are required"
    ]);
    exit;
}

// 1. Check if user exists with matching student_id and email
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ? AND student_id = ?");
$stmt->bind_param("ss", $email, $student_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "success" => false,
        "message" => "Invalid Student ID or Email"
    ]);
    exit;
}

$user = $result->fetch_assoc();

// 2. Hash the new password securely
$hashed_password = password_hash($new_password, PASSWORD_DEFAULT);

// 3. Update password in the database
$updateStmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
$updateStmt->bind_param("si", $hashed_password, $user["id"]);

if ($updateStmt->execute()) {
    echo json_encode([
        "success" => true,
        "message" => "Password reset successfully. You can now login."
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => "Failed to update password. Please try again."
    ]);
}

$stmt->close();
$updateStmt->close();
$conn->close();
?>
