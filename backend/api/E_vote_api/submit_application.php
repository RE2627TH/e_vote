<?php
header('Content-Type: application/json');
ob_start();
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

$user_id = $data['user_id'];
$name = $data['name'];
$student_id = $data['student_id'];
$position = $data['position'];
$manifesto = $data['manifesto'];
$course = $data['course'] ?? '';
$college = $data['college'] ?? '';
$goals = $data['goals'] ?? '';
$pledges = $data['pledges'] ?? '';
$symbol_name = $data['symbol_name'] ?? '';
$photo = $data['photo'] ?? 'default_candidate.jpg';
$symbol = $data['symbol'] ?? 'default_symbol.jpg';

$sql = "INSERT INTO candidate_details (user_id, name, student_id, position, manifesto, course, college, goals, pledges, symbol_name, photo, symbol, status, application_completed) 
        VALUES ('$user_id', '$name', '$student_id', '$position', '$manifesto', '$course', '$college', '$goals', '$pledges', '$symbol_name', '$photo', '$symbol', 'SUBMITTED', 1)
        ON DUPLICATE KEY UPDATE name='$name', student_id='$student_id', position='$position', manifesto='$manifesto', course='$course', college='$college', goals='$goals', pledges='$pledges', symbol_name='$symbol_name', photo='$photo', symbol='$symbol', status='SUBMITTED', application_completed=1";

if ($conn->query($sql)) {
    $inserted_id = (string)$conn->insert_id;
    
    // 🚀 RESPOND EARLY TO PREVENT TIMEOUTS
    ignore_user_abort(true); // Keep running even if client disconnects
    set_time_limit(300);    // Give background tasks 5 mins
    
    $response = json_encode(["success" => true, "message" => "Application Submitted. Processing notifications...", "application_id" => $inserted_id]);
    
    header('Connection: close');
    header('Content-Length: ' . strlen($response));
    echo $response;
    
    // Flush all buffers
    while (ob_get_level() > 0) ob_end_flush();
    flush();
    
    // Optional: for FastCGI environments
    if (function_exists('fastcgi_finish_request')) {
        fastcgi_finish_request();
    }

    // --- EVERYTHING BELOW THIS LINE RUNS IN THE BACKGROUND ---
    set_error_handler(function($errno, $errstr) {
        error_log("Notification Error [$errno]: $errstr");
    });

    // Send Email Notification
    include 'send_email.php';
    
    // Get candidate email from users table
    $userIdInt = intval($user_id);
    $userQuery = $conn->query("SELECT email FROM users WHERE id = $userIdInt");
    
    if ($userQuery && $userQuery->num_rows > 0) {
        $userData = $userQuery->fetch_assoc();
        $to = $userData['email'];
        $subject = "Registration Successful - E-Vote";
        $body = "<h2>Hello $name,</h2><p>You have successfully registered as a candidate.</p><p><b>Status: Waiting for Admin Approval.</b></p><p>You will receive another email once your account is approved.</p>";
        
        if (!sendEmail($to, $subject, $body)) {
            error_log("Failed to send candidate confirmation email to: $to");
        }
        
        // Notify Admin
        $adminEmail = "rethikasenthilkumar76@gmail.com";
        $adminSubject = "New Candidate Application: $name";
        $adminBody = "<h3>New Candidate Application</h3>
                     <p><b>Candidate:</b> $name</p>
                     <p><b>Position:</b> $position</p>
                     <p>Please login to the Admin Panel to review this application.</p>";
        
        sendEmail($adminEmail, $adminSubject, $adminBody);

        // 🔔 NEW: FCM & DB NOTIFICATION FOR ADMINS
        include_once 'fcm_helper.php';
        $fcm = new FcmHelper();
        $adminTitle = "New Candidate Request!";
        $adminMsg = "$name has applied for the $position role.";
        
        // Fetch all admins
        $adminRes = $conn->query("SELECT id, fcm_token FROM users WHERE role = 'admin'");
        while ($adminRow = $adminRes->fetch_assoc()) {
            $adminId = $adminRow['id'];
            $token = $adminRow['fcm_token'];
            
            // 1. Insert into notifications table
            $notifSql = "INSERT INTO notifications (user_id, title, body, screen, data_id) 
                         VALUES ('$adminId', '$adminTitle', '$adminMsg', 'MANAGE_CANDIDATES', '$user_id')";
            if (!$conn->query($notifSql)) {
                error_log("DB Notification Error: " . $conn->error);
            }
            
            // 2. Send Push Notification
            if ($token) {
                if (!$fcm->sendNotification($token, $adminTitle, $adminMsg, [
                    "screen" => "MANAGE_CANDIDATES",
                    "data_id" => (string)$user_id
                ])) {
                    error_log("FCM Notification failed for user ID: $adminId");
                }
            }
        }
    }
    restore_error_handler();
    // No need to echo anything else here
} else {
    echo json_encode(["success" => false, "message" => "Database Error"]);
}
?>