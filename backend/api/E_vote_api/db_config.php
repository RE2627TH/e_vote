<?php
error_reporting(0);
ini_set('display_errors', 0);
date_default_timezone_set('Asia/Kolkata');
$host = "localhost";
$user = "root";
$pass = "";
$dbname = "e_vote_db";

$conn = new mysqli($host, $user, $pass, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Database Connection Failed"]));
}
$conn->query("SET time_zone = '+05:30'");
?>