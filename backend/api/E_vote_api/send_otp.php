<?php
header('Content-Type: application/json');
include 'db_config.php';
include 'send_email.php';

$data = json_decode(file_get_contents("php://input"), true);
$email = $data['email'];

if (empty($email)) {
    echo json_encode(["success" => false, "message" => "Email is required"]);
    exit;
}

// Generate 6-digit OTP
$otp = str_pad(rand(0, 999999), 6, '0', STR_PAD_LEFT);

// Store or Update OTP in DB
$check = $conn->query("SELECT id FROM email_otps WHERE email='$email'");
if ($check->num_rows > 0) {
    $sql = "UPDATE email_otps SET otp='$otp', created_at=CURRENT_TIMESTAMP WHERE email='$email'";
} else {
    $sql = "INSERT INTO email_otps (email, otp) VALUES ('$email', '$otp')";
}

if ($conn->query($sql)) {
    $subject = "Your Verification Code - E-Vote";
    $body = "<h2>Welcome to E-Vote</h2>
             <p>Your verification code is: <strong>$otp</strong></p>
             <p>This code will expire in 5 minutes.</p>";
    
    if (sendEmail($email, $subject, $body)) {
        echo json_encode(["success" => true, "message" => "OTP sent to your email!"]);
    } else {
        // Fallback: If SMTP fails (e.g. Google block), show OTP so user isn't stuck
        echo json_encode([
            "success" => true, 
            "message" => "OTP generated: $otp (Email failed - check App Password)",
            "dev_otp" => $otp 
        ]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Database error: " . $conn->error]);
}
?>
