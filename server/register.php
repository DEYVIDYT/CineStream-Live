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
$expiration_date = date('Y-m-d', strtotime('+7 days'));

$sql = "INSERT INTO users (email, password, expiration_date) VALUES (?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("sss", $email, $hashed_password, $expiration_date);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'User registered successfully']);
} else {
    echo json_encode(['success' => false, 'message' => 'Error registering user']);
}

$stmt->close();
$conn->close();
?>
