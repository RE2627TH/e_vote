<?php
include 'db_config.php';

// 1. Add election_id column if it doesn't exist
$sql_add_col = "ALTER TABLE votes ADD COLUMN IF NOT EXISTS election_id INT DEFAULT 1 AFTER voter_id";
if ($conn->query($sql_add_col)) {
    echo "Column election_id added or already exists.<br>";
} else {
    echo "Error adding column: " . $conn->error . "<br>";
}

// 2. Drop the old unique constraint if it exists (usually named 'unique_vote')
// Note: In some versions of MySQL, you might need to drop by name or key
$sql_drop_unique = "ALTER TABLE votes DROP INDEX IF EXISTS unique_vote";
if ($conn->query($sql_drop_unique)) {
    echo "Old unique constraint dropped.<br>";
}

// 3. Add the new unique constraint (voter_id, election_id, position)
$sql_new_unique = "ALTER TABLE votes ADD UNIQUE KEY unique_vote_per_election (voter_id, election_id, position)";
if ($conn->query($sql_new_unique)) {
    echo "New unique constraint (voter_id, election_id, position) added successfully.<br>";
} else {
    echo "Error adding new unique constraint: " . $conn->error . "<br>";
}

echo "Migration completed successfully.";
?>
