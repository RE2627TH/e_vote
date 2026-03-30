<?php
include 'db_config.php';

$sql = "ALTER TABLE users ADD COLUMN college VARCHAR(255) AFTER department";

if ($conn->query($sql) === TRUE) {
    echo "Column college added successfully";
} else {
    echo "Error adding column: " . $conn->error;
}
?>
