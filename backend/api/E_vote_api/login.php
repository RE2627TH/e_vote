<?php
header("Content-Type: application/json");
include "db_config.php";

$data = json_decode(file_get_contents("php://input"), true);

$email = $data["email"] ?? "";
$password = $data["password"] ?? "";

if (empty($email) || empty($password)) {
    echo json_encode([
        "success" => false,
        "message" => "Email and password required"
    ]);
    exit;
}

/* 1. Get user */
$stmt = $conn->prepare("SELECT * FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "success" => false,
        "message" => "User not found"
    ]);
    exit;
}

$user = $result->fetch_assoc();

/* 2. Verify password */
if (!password_verify($password, $user["password"])) {
    echo json_encode([
        "success" => false,
        "message" => "Incorrect password"
    ]);
    exit;
}

/* 3. Candidate approval check */
$candidateDetails = null;

if ($user["role"] === "candidate") {
    $stmt2 = $conn->prepare(
        "SELECT * FROM candidate_details WHERE user_id = ?"
    );
    $stmt2->bind_param("i", $user["id"]);
    $stmt2->execute();
    $candidateResult = $stmt2->get_result();

    if ($candidateResult->num_rows === 0) {
        echo json_encode([
            "success" => false,
            "message" => "Candidate application not found"
        ]);
        exit;
    }

    $candidateDetails = $candidateResult->fetch_assoc();
    
    // Workflow status check
    if ($candidateDetails["status"] === "NOT_SUBMITTED") {
        echo json_encode([
            "success" => true,
            "message" => "Application incomplete. Redirecting...",
            "redirect_to_form" => true,
            "user_id" => (int)$user["id"],
            "role" => "candidate"
        ]);
        exit;
    }

    if ($candidateDetails["status"] === "SUBMITTED") {
        echo json_encode([
            "success" => false,
            "message" => "Your application is under review. Please wait for official approval."
        ]);
        exit;
    }

    if ($candidateDetails["status"] === "REJECTED") {
        echo json_encode([
            "success" => false,
            "message" => "Your application was rejected. Please contact the administrator.",
            "reason" => $candidateDetails["rejection_reason"]
        ]);
        exit;
    }
    
    // Status MUST be 'ACCEPTED' to continue standard login
    if ($candidateDetails["status"] !== "ACCEPTED") {
        echo json_encode([
            "success" => false,
            "message" => "Unauthorized application status."
        ]);
        exit;
    }
}

/* 4. Success response */
$token = bin2hex(random_bytes(32));

// ✅ Save token to database
$updateToken = $conn->prepare("UPDATE users SET token = ? WHERE id = ?");
$updateToken->bind_param("si", $token, $user["id"]);
$updateToken->execute();

echo json_encode([
    "success" => true,
    "message" => "Login successful",
    "user_id" => (int)$user["id"],
    "student_id" => $user["student_id"],
    "role" => $user["role"],
    "token" => $token,
    "is_profile_completed" => (int)$user["is_profile_completed"],
    "is_subscribed" => (int)($user["is_subscribed"] ?? 0),
    "user" => [
        "id" => (int)$user["id"],
        "name" => $user["name"],
        "student_id" => $user["student_id"],
        "email" => $user["email"],
        "role" => $user["role"],
        "is_profile_completed" => (int)$user["is_profile_completed"],
        "token" => $token,
        "is_subscribed" => (int)($user["is_subscribed"] ?? 0),
        "payment_id" => $user["payment_id"] ?? null,
        "candidate_details" => $candidateDetails
    ]
]);
