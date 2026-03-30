<?php
header('Content-Type: application/json');
include 'db_config.php';

$user_id = $_GET['user_id'];

// Get user basic info first
// Get user basic info first, alias status to avoid ambiguity
$sql = "SELECT u.id, u.name, u.email, u.role, u.status AS user_status, u.student_id, u.department, u.dob, u.profile_photo, u.college,
               cd.status AS candidate_status, cd.position, cd.manifesto, cd.photo, cd.symbol, 
               cd.course, cd.college AS candidate_college, cd.tagline, cd.goals, cd.pledges, cd.symbol_name
        FROM users u 
        LEFT JOIN candidate_details cd ON u.id = cd.user_id 
        WHERE u.id = '$user_id'";
        
$result = $conn->query($sql);

if (!$result) {
    echo json_encode(["success" => false, "message" => "SQL Error: " . $conn->error]);
    exit;
}

if ($row = $result->fetch_assoc()) {
    $candidateId = $row['id'];
    
    // 1. Fetch Vote Count
    $voteSql = "SELECT COUNT(*) as total_votes FROM votes WHERE candidate_id = '$candidateId'";
    $voteResult = $conn->query($voteSql);
    $voteCount = 0;
    if ($voteResult && $vRow = $voteResult->fetch_assoc()) {
        $voteCount = (int)$vRow['total_votes'];
    }

    // 2. Fetch Feedback
    $feedbackSql = "SELECT id, user_name, rating, comment, created_at FROM feedback WHERE candidate_id = '$candidateId' ORDER BY created_at DESC LIMIT 5";
    $feedbackResult = $conn->query($feedbackSql);
    $feedbackList = [];
    if ($feedbackResult) {
        while ($fb = $feedbackResult->fetch_assoc()) {
            // Basic time ago format or just return created_at
            $fb['time_ago'] = "Recently"; 
            $feedbackList[] = $fb;
        }
    }

    $user = [
        "id" => $row['id'],
        "name" => $row['name'],
        "email" => $row['email'],
        "role" => $row['role'],
        "status" => $row['user_status'],
        "college" => $row['college'],
        "dob" => $row['dob'],
        "student_id" => $row['student_id'],
        "department" => $row['department'],
        "profile_photo" => !empty($row['profile_photo']) ? $row['profile_photo'] : (isset($row['photo']) ? $row['photo'] : null),
        "candidate_details" => [
            "id" => $row['id'], // Added redundant ID for easier mapping
            "position" => $row['position'],
            "manifesto" => $row['manifesto'],
            "status" => $row['candidate_status'],
            "student_id" => $row['student_id'],
            "department" => $row['department'],
            "photo" => $row['photo'],
            "symbol" => $row['symbol'],
            "course" => $row['course'],
            "college" => $row['college'],
            "tagline" => $row['tagline'],
            "goals" => $row['goals'],
            "pledges" => $row['pledges'],
            "symbol_name" => $row['symbol_name'],
            "vote_count" => $voteCount,
            "feedback" => $feedbackList
        ]
    ];
    echo json_encode(["success" => true, "user" => $user]);
} else {
    echo json_encode(["success" => false, "message" => "User not found"]);
}
?>
