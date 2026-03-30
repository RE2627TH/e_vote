<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);
$election_id = $data['election_id'] ?? null;
$action = $data['action'] ?? 'publish'; // 'publish' or 'close'

if (!$election_id) {
    // Get latest if not provided
    $latest = $conn->query("SELECT id FROM elections ORDER BY id DESC LIMIT 1")->fetch_assoc();
    $election_id = $latest['id'] ?? null;
}

if (!$election_id) {
    echo json_encode(["success" => false, "message" => "No election found"]);
    exit;
}

if ($action === 'publish') {
    $sql = "UPDATE elections SET is_published = 1, status = 'closed' WHERE id = '$election_id'";
} else if ($action === 'close') {
    $sql = "UPDATE elections SET status = 'closed' WHERE id = '$election_id'";
} else {
    echo json_encode(["success" => false, "message" => "Invalid action"]);
    exit;
}

if ($conn->query($sql)) {
    // 🔔 SEND PUSH NOTIFICATIONS
    if ($action === 'publish') {
        include 'fcm_helper.php';
        $fcm = new FcmHelper();
        
        // Fetch all student tokens
        $tokensRes = $conn->query("SELECT fcm_token FROM users WHERE fcm_token IS NOT NULL");
        while ($row = $tokensRes->fetch_assoc()) {
            $fcm->sendNotification(
                $row['fcm_token'],
                "Election Results Published!",
                "The final results for the election are now available. Check them out in the Results screen!"
            );
        }
    }

    echo json_encode(["success" => true, "message" => "Results published/closed successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Database Error: " . $conn->error]);
}
?>
