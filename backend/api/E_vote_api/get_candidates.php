<?php
header('Content-Type: application/json');
include 'db_config.php';

$status = isset($_GET['status']) ? $_GET['status'] : null;
$position = isset($_GET['position']) ? $_GET['position'] : 'ALL';

$sql = "SELECT u.id, u.name, u.email, u.role, u.department, u.student_id, u.college, u.dob,
               IFNULL(u.profile_photo, cd.photo) AS profile_photo, cd.position, cd.status AS application_status, cd.status
        FROM users u 
        LEFT JOIN candidate_details cd ON u.id = cd.user_id 
        WHERE u.role = 'candidate'";

if ($status && $status !== 'ALL') {
    $sql .= " AND cd.status = '$status'";
}

if ($position && $position !== 'ALL') {
    $sql .= " AND cd.position = '$position'";
}

$sql .= " ORDER BY u.name ASC";

$result = $conn->query($sql);
$candidates = [];

if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $candidates[] = $row;
    }
}

echo json_encode($candidates);
$conn->close();
?>