<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "Testing database connection...<br>";

$servername = "localhost";
$username = "hvnivrhy_CineStream";
$password = "EEcwPCF8tcN9NKWSEn8q";
$dbname = "hvnivrhy_CineStream";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    echo "Connection failed: " . $conn->connect_error;
} else {
    echo "Database connection successful!";
}

$conn->close();
?>
