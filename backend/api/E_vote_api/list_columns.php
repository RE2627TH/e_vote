<?php
include 'db_config.php';

$table = 'users';
$sql = "SHOW COLUMNS FROM $table";
$result = $conn->query($sql);

if ($result) {
    while($row = $result->fetch_assoc()) {
        echo $row['Field'] . " - " . $row['Type'] . "\n";
    }
} else {
    echo "Error: " . $conn->error;
}
?>
