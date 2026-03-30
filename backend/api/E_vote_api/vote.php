<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

$voter_id = $data['voter_id'];
$candidate_id = $data['candidate_id'];
$position = $data['position'];
$election_id = isset($data['election_id']) ? $data['election_id'] : 1; // Default to 1 if not provided

// Check if election is ACTIVE (using dynamic status calculation for absolute sync)
$election_check_sql = "SELECT 
    CASE 
        WHEN status = 'CANCELLED' THEN 'CANCELLED'
        WHEN status = 'CLOSED' THEN 'CLOSED'
        WHEN NOW() >= end_date THEN 'CLOSED'
        WHEN NOW() >= start_date THEN 'ACTIVE'
        ELSE 'UPCOMING'
    END as current_status
    FROM elections WHERE id='$election_id'";

$electionCheck = $conn->query($election_check_sql);
$election = $electionCheck->fetch_assoc();
$actual_status = $election['current_status'] ?? 'not found';

if ($actual_status !== 'ACTIVE') {
    echo json_encode(["success" => false, "message" => "Voting is not allowed! Election is " . $actual_status]);
    exit;
}

// Check if already voted for this position in this election
$check = $conn->query("SELECT id FROM votes WHERE voter_id='$voter_id' AND position='$position' AND election_id='$election_id'");

if ($check->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "You have already voted for this position in this election!"]);
} else {
    $sql = "INSERT INTO votes (voter_id, candidate_id, position, election_id) VALUES ('$voter_id', '$candidate_id', '$position', '$election_id')";
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Vote Submitted Successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Voting Failed: " . $conn->error]);
    }
}
?>