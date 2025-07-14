<?php
$servername = "sql201.x10mx.com";
$username = "mybrasiltv";
$password = "Jg@19921992";
$dbname = "mybrasiltv_dados";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
?>
