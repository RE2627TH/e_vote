<?php
include 'db_config.php';

$sql = "ALTER TABLE elections ADD COLUMN is_published BOOLEAN DEFAULT FALSE";

if ($conn->query($sql)) {
    echo "Column is_published added successfully";
} else {
    echo "Error adding column: " . $conn->error;
}
?>
