<?php
header('Content-Type: application/json');
include 'db_config.php';

$user_id = $_GET['user_id'] ?? null;

if (!$user_id) {
    echo json_encode(["error" => "User ID required"]);
    exit;
}

// 1. Get user role
$userRes = $conn->query("SELECT role FROM users WHERE id = '$user_id'");
$user = $userRes->fetch_assoc();
$role = $user['role'] ?? null;

// 2. Get latest election status
$electionRes = $conn->query("SELECT status, is_published FROM elections ORDER BY id DESC LIMIT 1");
$election = $electionRes->fetch_assoc();
$isEnded = ($election['status'] ?? '') === 'closed';
$isPublished = ($election['is_published'] ?? 0) == 1;

// 3. Security Check: Non-admins only see results if election is ended AND published
if ($role !== 'admin') {
    if (!$isEnded || !$isPublished) {
        // Return empty results if not authorized to see them yet
        echo json_encode([]);
        exit;
    }
}

// 4. Fetch Election Title to filter candidates by position
$election_id = $_GET['election_id'] ?? null;
if (!$election_id) {
    $latestRes = $conn->query("SELECT id, title FROM elections ORDER BY id DESC LIMIT 1");
    if ($latestRow = $latestRes->fetch_assoc()) {
        $election_id = $latestRow['id'];
        $election_title = $latestRow['title'];
    }
} else {
    $titleRes = $conn->query("SELECT title FROM elections WHERE id = '$election_id'");
    $titleRow = $titleRes->fetch_assoc();
    $election_title = $titleRow['title'] ?? '';
}

$sql = "SELECT u.id as user_id, u.name, cd.position, COUNT(v.id) as vote_count 
        FROM candidate_details cd
        JOIN users u ON cd.user_id = u.id
        LEFT JOIN votes v ON u.id = v.candidate_id AND v.election_id = '$election_id'
        WHERE cd.status = 'ACCEPTED' AND cd.position = '$election_title'
        GROUP BY u.id, cd.position
        ORDER BY vote_count DESC";

$result = $conn->query($sql);
$results = [];

while ($row = $result->fetch_assoc()) {
    $results[] = $row;
}

echo json_encode($results);
?>