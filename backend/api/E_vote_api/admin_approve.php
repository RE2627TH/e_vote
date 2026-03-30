<?php
header('Content-Type: application/json');
include 'db_config.php';

$user_id = $_GET['user_id'];
$action = $_GET['action']; // 'approve' or 'reject'

if ($action == 'delete') {
    $user_sql = "UPDATE users SET role='student', status='active' WHERE id='$user_id'";
    $candidate_sql = "DELETE FROM candidate_details WHERE user_id='$user_id'";
    
    if ($conn->query($user_sql) && $conn->query($candidate_sql)) {
        echo json_encode(["success" => true, "message" => "Candidate request deleted successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Delete failed: " . $conn->error]);
    }
    exit;
}

$status = ($action == 'approve') ? 'ACCEPTED' : 'REJECTED';
$user_status = ($action == 'approve') ? 'approved' : 'rejected';

$user_sql = "UPDATE users SET status='$user_status' WHERE id='$user_id'";
$candidate_sql = "UPDATE candidate_details SET status='$status' WHERE user_id='$user_id'";

if ($conn->query($user_sql) && $conn->query($candidate_sql)) {
    
    // Fetch user email to send notification
    $email_sql = "SELECT email, name FROM users WHERE id='$user_id'";
    $email_res = $conn->query($email_sql);
    if ($email_res->num_rows > 0) {
        $user_data = $email_res->fetch_assoc();
        
        include 'send_email.php';
        $subject = "Candidate Application Status Update - E-Vote";
        
        if ($status == 'ACCEPTED') {
             $body = "Dear " . $user_data['name'] . ",<br><br><b>Admin approved your request.</b><br>You can now login to the application.<br><br>Regards,<br>E-Vote Admin";
        } else {
             $body = "Dear " . $user_data['name'] . ",<br><br>Your application has been <b>Rejected</b> by the Admin.<br><br>Regards,<br>E-Vote Admin";
        }
        
        sendEmail($user_data['email'], $subject, $body);
    }

    echo json_encode(["success" => true, "message" => "Candidate $status successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Update failed: " . $conn->error]);
}
?>