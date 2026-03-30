<?php
header('Content-Type: application/json');
include 'db_config.php';

// Status is calculated dynamically based on server time to ensure absolute sync
$sql = "SELECT *, 
        CASE 
            WHEN status = 'CANCELLED' THEN 'CANCELLED'
            WHEN status = 'CLOSED' THEN 'CLOSED'
            WHEN NOW() >= end_date THEN 'CLOSED'
            WHEN NOW() >= start_date THEN 'ACTIVE'
            ELSE 'UPCOMING'
        END as status
        FROM elections 
        WHERE status != 'CANCELLED' 
        ORDER BY created_at DESC";
$result = $conn->query($sql);

$elections = [];
if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $elections[] = $row;
    }
}

echo json_encode($elections);
$conn->close();
?>
