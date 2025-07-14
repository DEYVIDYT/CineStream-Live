<?php
include 'database.php';

header('Content-Type: application/json');

$email = $_POST['email'];
$password = $_POST['password'];

if (empty($email) || empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Email and password are required']);
    exit();
}

$sql = "SELECT id, password, expiration_date FROM users WHERE email = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    if (password_verify($password, $user['password'])) {
        if (strtotime($user['expiration_date']) >= time()) {
            $session_token = bin2hex(random_bytes(32));
            $user_id = $user['id'];

            $ip_address = $_SERVER['REMOTE_ADDR'];
            $user_agent = $_SERVER['HTTP_USER_AGENT'];

            $update_sql = "UPDATE users SET session_token = ?, last_login = CURRENT_TIMESTAMP, ip_address = ?, user_agent = ? WHERE id = ?";
            $update_stmt = $conn->prepare($update_sql);
            $update_stmt->bind_param("ssssi", $session_token, $ip_address, $user_agent, $user_id);
            $update_stmt->execute();

            echo json_encode(['success' => true, 'token' => $session_token]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Your plan has expired']);
        }
    } else {
        echo json_encode(['success' => false, 'message' => 'Invalid password']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'User not found']);
}

$stmt->close();
$conn->close();
?>
