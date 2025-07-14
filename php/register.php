<?php
include 'db_config.php';

header('Content-Type: application/json');

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($email) || empty($password)) {
    echo json_encode(['status' => 'error', 'message' => 'Email e senha são obrigatórios.']);
    exit;
}

// Verificar se o e-mail já existe
$sql = "SELECT id FROM users WHERE email = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    echo json_encode(['status' => 'error', 'message' => 'Este e-mail já está cadastrado.']);
    $stmt->close();
    $conn->close();
    exit;
}

$stmt->close();

// Hash da senha
$hashed_password = password_hash($password, PASSWORD_DEFAULT);

// Inserir novo usuário
$sql = "INSERT INTO users (email, password) VALUES (?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $email, $hashed_password);

if ($stmt->execute()) {
    echo json_encode(['status' => 'success', 'message' => 'Usuário registrado com sucesso.']);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Erro ao registrar o usuário.']);
}

$stmt->close();
$conn->close();
?>
