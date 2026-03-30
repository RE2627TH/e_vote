<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['election_id'])) {
    
    $election_id = $data['election_id'];

    // 1. Delete associated votes first (Cascade)
    $stmt1 = $conn->prepare("DELETE FROM votes WHERE election_id = ?");
    $stmt1->bind_param("i", $election_id);
    $stmt1->execute();

    // 2. Delete the election
    $stmt2 = $conn->prepare("DELETE FROM elections WHERE id = ?");
    $stmt2->bind_param("i", $election_id);

    if ($stmt2->execute()) {
        echo json_encode(["success" => true, "message" => "Election and its votes deleted successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
    }

} else {
     echo json_encode(["success" => false, "message" => "Invalid input data"]);
}
?>
