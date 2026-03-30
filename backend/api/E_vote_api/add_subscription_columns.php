<?php
include 'db_config.php';

$sql = "ALTER TABLE users 
        ADD COLUMN is_subscribed TINYINT(1) DEFAULT 0,
        ADD COLUMN payment_id VARCHAR(255) DEFAULT NULL";

if ($conn->query($sql)) {
    echo "Columns is_subscribed and payment_id added successfully";
} else {
    echo "Error adding columns: " . $conn->error;
}
?>
