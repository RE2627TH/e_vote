<?php
header('Content-Type: application/json');
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

$user_id = $data['user_id'];
$name = $data['name']; // Update name in users table potentially? Or just local display? Let's assume just updating details for now.
$course = $data['course'];
$college = $data['college'];
$tagline = $data['tagline'];
$goals = $data['goals']; // multiline string
$pledges = $data['pledges']; // multiline string
$symbol_name = $data['symbol_name'];
$photo = isset($data['photo']) ? $data['photo'] : null;
$symbol = isset($data['symbol']) ? $data['symbol'] : null;

// Check if candidate details exist
$check = $conn->query("SELECT id FROM candidate_details WHERE user_id='$user_id'");

if ($check->num_rows > 0) {
    // Update
    $sql = "UPDATE candidate_details SET 
            course='$course', 
            college='$college', 
            tagline='$tagline', 
            goals='$goals', 
            pledges='$pledges',
            symbol_name='$symbol_name'";
            
    if ($photo) {
        $sql .= ", photo='$photo'";
    }
    if ($symbol) {
        $sql .= ", symbol='$symbol'";
    }
    
    $sql .= " WHERE user_id='$user_id'";
    
    if ($conn->query($sql)) {
        // Also update Name in Users table
        $conn->query("UPDATE users SET name='$name' WHERE id='$user_id'");
        echo json_encode(["success" => true, "message" => "Profile Updated Successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error updating record: " . $conn->error]);
    }

} else {
    // Insert (Should rarely happen if application was submitted, but good callback)
    // Assuming position/student_id are already there or this is a fresh partial insert?
    // Let's assume update only for this flow as Application creates the row.
    echo json_encode(["success" => false, "message" => "Candidate profile not found. Please submit application first."]);
}
?>
