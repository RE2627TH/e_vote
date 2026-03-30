<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['election_id']) && isset($data['status'])) {
    
    $election_id = $data['election_id'];
    $status = $data['status']; // 'UPCOMING', 'ACTIVE', 'CLOSED', 'CANCELLED'
    $now = date('Y-m-d H:i:s');

    if ($status === 'ACTIVE') {
        // Fetch current election to check if it's an early start
        $check = $conn->query("SELECT start_date, end_date FROM elections WHERE id = '$election_id'");
        $row = $check->fetch_assoc();
        
        if ($row) {
            $scheduled_start = $row['start_date'];
            $scheduled_end = $row['end_date'];
            
            if ($now < $scheduled_start) {
                // Early start: calc duration and shift end date
                $duration = strtotime($scheduled_end) - strtotime($scheduled_start);
                $new_end = date('Y-m-d H:i:s', strtotime($now) + $duration);
                $conn->query("UPDATE elections SET start_date = '$now', end_date = '$new_end' WHERE id = '$election_id'");
            }
        }
    }

    if ($status === 'CLOSED') {
        $sql = "UPDATE elections SET status = '$status', end_date = '$now' WHERE id = '$election_id'";
    } else {
        $sql = "UPDATE elections SET status = '$status' WHERE id = '$election_id'";
    }

    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Election status updated to $status"]);

        // Send notifications if CLOSED
        if ($status === 'CLOSED') {
            include_once 'fcm_helper.php';
            $fcm = new FcmHelper();
            $title = "Election Results Available!";
            $body = "The election has just ended. Check the results now.";
            
            // Fetch all users with a token
            $tokens = $conn->query("SELECT fcm_token FROM users WHERE fcm_token IS NOT NULL");
            while($t_row = $tokens->fetch_assoc()) {
                $fcm->sendNotification($t_row['fcm_token'], $title, $body, ["screen" => "RESULTS"]);
            }
        }
    } else {
        echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
    }

} else {
     echo json_encode(["success" => false, "message" => "Invalid input data"]);
}
?>
