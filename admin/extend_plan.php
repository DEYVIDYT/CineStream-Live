<?php
session_start();

if (!isset($_SESSION['loggedin'])) {
    header('Location: login.php');
    exit;
}

include '../server/database.php';

if (isset($_POST['user_id'])) {
    $user_id = $_POST['user_id'];

    $sql = "UPDATE users SET expiration_date = DATE_ADD(expiration_date, INTERVAL 30 DAY) WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
}

header('Location: index.php');
?>
