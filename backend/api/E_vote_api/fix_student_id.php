<?php
header('Content-Type: text/plain');
include 'db_config.php';

$student_id = '71712212';

echo "Checking for Student ID: $student_id...\n";

$check_sql = "SELECT * FROM users WHERE student_id = '$student_id'";
$result = $conn->query($check_sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo "SUCCESS: Student ID $student_id FOUND in database.\n";
    echo "Name: " . $row['name'] . "\n";
    echo "Role: " . $row['role'] . "\n";
    echo "Status: " . $row['status'] . "\n";
} else {
    echo "MISSING: Student ID $student_id NOT FOUND.\n";
    echo "Attempting to insert now...\n";
    
    $insert_sql = "INSERT INTO users (name, dob, student_id, department, email, password, role, status)
                   VALUES ('Test Student', '2000-01-01', '71712212', 'CSE', 'test_71712212@example.com', 'password123', 'student', 'approved')";
                   
    if ($conn->query($insert_sql) === TRUE) {
        echo "FIXED: Successfully inserted Student ID $student_id.\n";
    } else {
        echo "ERROR: Could not insert student. MySQL Error: " . $conn->error . "\n";
    }
}
?>
