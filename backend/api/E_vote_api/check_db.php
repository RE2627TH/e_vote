<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

$conn = new mysqli("localhost", "root", "");

if ($conn->connect_error) {
    die("<h2>MySQL Connection Failed!</h2><p>Error: " . $conn->connect_error . "</p><p>Make sure XAMPP MySQL is STARTed.</p>");
}

echo "<h2>MySQL Connected Successfully!</h2>";
echo "<h3>Available Databases:</h3><ul>";

$result = $conn->query("SHOW DATABASES");
if ($result) {
    while($row = $result->fetch_assoc()) {
        echo "<li>" . $row['Database'] . "</li>";
    }
}
echo "</ul>";

echo "<h3>Currently Configured in db_config.php:</h3>";
echo "<p>Database: <strong>E_vote_api</strong></p>";
echo "<p>User: <strong>root</strong></p>";
echo "<p>Password: <strong>(empty)</strong></p>";

echo "<hr><p><em>If your database name in the list above is different from 'E_vote_api', please update line 7 in db_config.php.</em></p>";
?>
