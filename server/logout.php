<?php
include 'database.php';

header('Content-Type: application/json');

$token = $_POST['token'];

if (empty($token)) {
    echo json_encode(['success' => false, 'message' => 'Session token is required']);
    exit();
}

$sql = "UPDATE users SET session_token = NULL WHERE session_token = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $token);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Logged out successfully']);
} else {
    echo json_encode(['success' => false, 'message' => 'Error logging out']);
}

$stmt->close();
$conn->close();
?>
