<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['title']) && isset($data['start_date']) && isset($data['end_date'])) {
    
    $title = $data['title'];
    $start_date = $data['start_date'];
    $end_date = $data['end_date'];
    
    $now = date('Y-m-d H:i:s');
    $status = 'UPCOMING';
    if ($now >= $end_date) {
        $status = 'CLOSED';
    } elseif ($now >= $start_date) {
        $status = 'ACTIVE';
    }

    // Create table if not exists (Lazy init for POC)
    $create_table = "CREATE TABLE IF NOT EXISTS elections (
        id INT AUTO_INCREMENT PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        start_date DATETIME,
        end_date DATETIME,
        status VARCHAR(50),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )";
    $conn->query($create_table);

    $sql = "INSERT INTO elections (title, start_date, end_date, status) VALUES ('$title', '$start_date', '$end_date', '$status')";

    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Election created successfully"]);

        // Notify all students/candidates about the new election
        include_once 'fcm_helper.php';
        $fcm = new FcmHelper();
        $title = "New Election Scheduled: $title";
        $body = "A new election has been scheduled to start on $start_date. Get ready to vote!";
        
        $tokens = $conn->query("SELECT fcm_token FROM users WHERE fcm_token IS NOT NULL");
        while($t_row = $tokens->fetch_assoc()) {
            $fcm->sendNotification($t_row['fcm_token'], $title, $body, ["screen" => "HOME"]);
        }
    } else {
        echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
    }

} else {
     echo json_encode(["success" => false, "message" => "Invalid input data"]);
}
?>
