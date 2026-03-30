<?php
header('Content-Type: application/json');
include 'db_config.php';

// 1. Total Students
$sql_students = "SELECT COUNT(*) as count FROM users WHERE role='student'";
$result_students = $conn->query($sql_students);
$students_count = ($result_students->num_rows > 0) ? $result_students->fetch_assoc()['count'] : 0;

// 2. Total Candidates
$sql_candidates = "SELECT COUNT(*) as count FROM users WHERE role='candidate'";
$result_candidates = $conn->query($sql_candidates);
$candidates_count = ($result_candidates->num_rows > 0) ? $result_candidates->fetch_assoc()['count'] : 0;

// 3. Active Elections
$sql_elections = "SELECT COUNT(*) as count FROM elections WHERE status='active'";
$result_elections = $conn->query($sql_elections);
$active_elections = ($result_elections->num_rows > 0) ? $result_elections->fetch_assoc()['count'] : 0;

// 4. Votes Cast
$sql_votes = "SELECT COUNT(*) as count FROM votes";
$result_votes = $conn->query($sql_votes);
$votes_cast = ($result_votes->num_rows > 0) ? $result_votes->fetch_assoc()['count'] : 0;

// 5. Pending Candidates (For Notifications)
$sql_pending = "SELECT COUNT(*) as count FROM candidate_details WHERE status='pending'";
$result_pending = $conn->query($sql_pending);
$pending_count = ($result_pending->num_rows > 0) ? $result_pending->fetch_assoc()['count'] : 0;

echo json_encode([
    "success" => true,
    "stats" => [
        "students_count" => $students_count,
        "candidates_count" => $candidates_count,
        "active_elections" => $active_elections,
        "votes_cast" => $votes_cast,
        "pending_candidates" => $pending_count
    ]
]);
?>
