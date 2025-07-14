<?php
include 'database.php';

header('Content-Type: application/json');

$email = $_POST['email'];
$password = $_POST['password'];

if (empty($email) || empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Email and password are required']);
    exit();
}

$hashed_password = password_hash($password, PASSWORD_DEFAULT);
$expiration_date = date('Y-m-d', strtotime('+2 days'));

$ip_address = $_SERVER['REMOTE_ADDR'];
$user_agent = $_SERVER['HTTP_USER_AGENT'];

$sql = "INSERT INTO users (email, password, expiration_date, ip_address, user_agent) VALUES (?, ?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("sssss", $email, $hashed_password, $expiration_date, $ip_address, $user_agent);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'User registered successfully']);
} else {
    echo json_encode(['success' => false, 'message' => 'Error registering user']);
}

$stmt->close();
$conn->close();
?>
