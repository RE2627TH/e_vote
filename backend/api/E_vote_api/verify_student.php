<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);
$student_id = trim($data['studentId']);

$sql = "SELECT name, department, status 
        FROM users 
        WHERE student_id = '$student_id'";

$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $row = $result->fetch_assoc();
    echo json_encode([
        "success" => true,
        "message" => "Student Verified",
        "studentData" => [
            "name" => $row['name'],
            "studentId" => $student_id,
            "department" => $row['department']
        ]
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Student ID not found in records"]);
}
?>