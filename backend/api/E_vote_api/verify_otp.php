<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);
$email = $data['email'];
$otp = $data['otp'];

if (empty($email) || empty($otp)) {
    echo json_encode(["success" => false, "message" => "Email and OTP are required"]);
    exit;
}

// Check OTP and Expiry (5 minutes)
$sql = "SELECT * FROM email_otps WHERE email='$email' AND otp='$otp' AND created_at >= NOW() - INTERVAL 5 MINUTE";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    echo json_encode(["success" => true, "message" => "OTP verified successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Invalid or expired OTP"]);
}
?>
