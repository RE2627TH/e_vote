<?php
header('Content-Type: application/json');
include 'db_config.php';

// Get the most relevant election using dynamic status calculation
$sql = "SELECT * FROM (
            SELECT id, title, start_date, end_date, 
            CASE 
                WHEN status = 'CANCELLED' THEN 'CANCELLED'
                WHEN status = 'CLOSED' THEN 'CLOSED'
                WHEN NOW() >= end_date THEN 'CLOSED'
                WHEN NOW() >= start_date THEN 'ACTIVE'
                ELSE 'UPCOMING'
            END as current_status,
            is_published 
            FROM elections
        ) as election_calc
        ORDER BY id DESC 
        LIMIT 1";

$result = $conn->query($sql);

if ($result && $result->num_rows > 0) {
    $row = $result->fetch_assoc();
    // Rename current_status back to status for the frontend
    $row['status'] = $row['current_status'];
    unset($row['current_status']);
    
    // Explicitly cast to boolean for strict mapping in Android
    $row['is_published'] = (bool)$row['is_published'];
    
    echo json_encode($row);
} else {
    // Return empty or default
    echo json_encode(["title" => "No Active Election", "end_date" => null, "status" => "none", "is_published" => 0]);
}
?>
