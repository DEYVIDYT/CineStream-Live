<?php
include 'database.php';

header('Content-Type: application/json');

$token = $_POST['token'];

if (empty($token)) {
    echo json_encode(['success' => false, 'message' => 'Session token is required']);
    exit();
}

$sql = "SELECT email, expiration_date FROM users WHERE session_token = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $token);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    echo json_encode(['success' => true, 'email' => $user['email'], 'expiration_date' => $user['expiration_date']]);
} else {
    echo json_encode(['success' => false, 'message' => 'Invalid session token']);
}

$stmt->close();
$conn->close();
?>
