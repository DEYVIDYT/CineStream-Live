<?php
include 'database.php';

header('Content-Type: application/json');

$email = $_POST['email'];
$password = $_POST['password'];

if (empty($email) || empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Email and password are required']);
    exit();
}

// if (!function_exists('password_hash')) {
//     echo json_encode(['success' => false, 'message' => 'password_hash function does not exist']);
//     exit();
// }

$hashed_password = $password; // Storing password in plain text for debugging
$expiration_date = date('Y-m-d', strtotime('+2 days'));

$ip_address = $_SERVER['REMOTE_ADDR'];
$user_agent = $_SERVER['HTTP_USER_AGENT'];

$sql = "INSERT INTO users (email, password, expiration_date, ip_address, user_agent) VALUES ('$email', '$hashed_password', '$expiration_date', '$ip_address', '$user_agent')";

if ($conn->query($sql) === TRUE) {
    echo json_encode(['success' => true, 'message' => 'User registered successfully']);
} else {
    echo json_encode(['success' => false, 'message' => 'Error registering user: ' . $conn->error]);
}
$conn->close();
?>
