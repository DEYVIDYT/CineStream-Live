<?php
include 'database.php';

header('Content-Type: application/json');

$token = $_POST['token'];

if (empty($token)) {
    echo json_encode(['success' => false, 'message' => 'Session token is required']);
    exit();
}

$sql = "SELECT id FROM users WHERE session_token = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    echo json_encode(['success' => true, 'message' => 'Session is valid']);
} else {
    echo json_encode(['success' => false, 'message' => 'Session is invalid or has expired']);
}

$stmt->close();
$conn->close();
?>
