<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'db_config.php';

$data = json_decode(file_get_contents("php://input"));

if(isset($data->candidate_id) && isset($data->user_name) && isset($data->rating) && isset($data->comment)){

    $candidate_id = $conn->real_escape_string($data->candidate_id);
    $user_name = $conn->real_escape_string($data->user_name);
    $rating = $conn->real_escape_string($data->rating);
    $comment = $conn->real_escape_string($data->comment);

    $sql = "INSERT INTO feedback (candidate_id, user_name, rating, comment) VALUES ('$candidate_id', '$user_name', '$rating', '$comment')";

    if($conn->query($sql) === TRUE){
        echo json_encode(array("success" => true, "message" => "Feedback submitted successfully."));
    } else{
        echo json_encode(array("success" => false, "message" => "Error: " . $conn->error));
    }

} else{
     echo json_encode(array("success" => false, "message" => "Incomplete data."));
}

$conn->close();
?>
