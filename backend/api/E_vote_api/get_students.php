<?php
header('Content-Type: application/json');
include 'db_config.php';

/**
 * Endpoint to fetch all students for the admin dashboard.
 * Joins users with candidate_details to get potential profile photos.
 */

// SQL to fetch all students
// We use a LEFT JOIN with candidate_details because photos are currently stored there
// or in a profile_photo column if it was added to the users table.
// To be safe, we check both.
$sql = "SELECT u.id, u.name, u.email, u.role, u.department, u.student_id, u.college, u.dob,
               IFNULL(u.profile_photo, cd.photo) AS profile_photo
        FROM users u 
        LEFT JOIN candidate_details cd ON u.id = cd.user_id
        WHERE u.role = 'student' 
        ORDER BY u.name ASC";

$result = $conn->query($sql);
$students = [];

if (!$result) {
    echo json_encode(["success" => false, "message" => "Database error: " . $conn->error]);
    exit;
}

while ($row = $result->fetch_assoc()) {
    // Ensure the response structure matches AppUser model expectations
    $students[] = [
        "id" => $row['id'],
        "name" => $row['name'],
        "email" => $row['email'],
        "role" => $row['role'],
        "department" => $row['department'],
        "student_id" => $row['student_id'],
        "college" => $row['college'],
        "dob" => $row['dob'],
        "profile_photo" => $row['profile_photo']
    ];
}

echo json_encode($students);

$conn->close();
?>
