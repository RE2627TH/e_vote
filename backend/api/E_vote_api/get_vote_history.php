<?php
header('Content-Type: application/json');
include 'db_config.php';

$voter_id = isset($_GET['voter_id']) ? $_GET['voter_id'] : null;

if (!$voter_id) {
    echo json_encode(["success" => false, "message" => "Voter ID required"]);
    exit;
}

// Join votes with users (candidates) and elections to get full details
$sql = "SELECT v.position, v.timestamp, u.name as candidate_name, e.title as election_title
        FROM votes v
        JOIN users u ON v.candidate_id = u.id
        JOIN elections e ON v.election_id = e.id
        WHERE v.voter_id = '$voter_id'
        ORDER BY v.timestamp DESC";

$result = $conn->query($sql);
$history = [];

if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $history[] = $row;
    }
}

echo json_encode($history);
?>
