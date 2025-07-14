<?php
$servername = "localhost";
$username = "hvnivrhy_CineStream";
$password = "EEcwPCF8tcN9NKWSEn8q";
$dbname = "hvnivrhy_CineStream";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    header('Content-Type: application/json');
    echo json_encode(['success' => false, 'message' => 'Database connection failed: ' . $conn->connect_error]);
    exit();
}
?>
