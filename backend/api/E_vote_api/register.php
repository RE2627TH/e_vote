<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

// Get all fields from Android App
$name = $data['name'];
$dob = $data['dob'];
$student_id = $data['student_id'];
$department = $data['department'];
$email = $data['email'];
$password = password_hash($data['password'], PASSWORD_DEFAULT);
$role = $data['role']; // 'student' or 'candidate'

$status = ($role == 'candidate') ? 'pending' : 'approved';

$checkEmail = $conn->query("SELECT id FROM users WHERE email='$email' OR student_id='$student_id'");

if ($checkEmail->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Email or Student ID already exists"]);
} else {
    // Insert all details into the users table shown in your phpMyAdmin image
    $sql = "INSERT INTO users (name, dob, student_id, department, email, password, role, status) 
            VALUES ('$name', '$dob', '$student_id', '$department', '$email', '$password', '$role', '$status')";
    
    if ($conn->query($sql)) {
        $new_user_id = $conn->insert_id;
        
        // If candidate, initialize candidate_details table too
        if ($role === 'candidate') {
            $conn->query("INSERT INTO candidate_details (user_id, status, application_completed) VALUES ('$new_user_id', 'NOT_SUBMITTED', 0)");
        }

        echo json_encode(["success" => true, "message" => "Registration successful", "user_id" => (string)$new_user_id]);
    } else {
        echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
    }
}
?>