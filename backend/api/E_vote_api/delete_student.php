<?php
header('Content-Type: application/json');
include 'db_config.php';

// Check request method
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["success" => false, "message" => "Invalid request method"]);
    exit;
}

// Get input data
$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['user_id'])) {
    echo json_encode(["success" => false, "message" => "User ID is required"]);
    exit;
}

$user_id = $conn->real_escape_string($data['user_id']);

// Start transaction to ensure data integrity
$conn->begin_transaction();

try {
    // 1. Delete votes cast by this user (voter_id)
    $sql1 = "DELETE FROM votes WHERE voter_id = '$user_id'";
    if (!$conn->query($sql1)) {
        throw new Exception("Error deleting user's cast votes: " . $conn->error);
    }

    // 2. Delete votes received by this user (if they were a candidate)
    $sql2 = "DELETE FROM votes WHERE candidate_id = '$user_id'";
    if (!$conn->query($sql2)) {
        throw new Exception("Error deleting votes received by candidate: " . $conn->error);
    }

    // 3. Delete the user (This will cascade to candidate_details and feedback due to ON DELETE CASCADE)
    $sql3 = "DELETE FROM users WHERE id = '$user_id'";
    if (!$conn->query($sql3)) {
        throw new Exception("Error deleting user: " . $conn->error);
    }

    // Commit transaction
    $conn->commit();
    echo json_encode(["success" => true, "message" => "Student and all associated records deleted successfully"]);

} catch (Exception $e) {
    // Rollback transaction on error
    $conn->rollback();
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}

$conn->close();
?>
