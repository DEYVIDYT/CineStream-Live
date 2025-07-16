<?php
include 'json_config.php';

header('Content-Type: application/json');

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$device_id = $_POST['device_id'] ?? '';

if (empty($email) || empty($password) || empty($device_id)) {
    echo json_encode(['status' => 'error', 'message' => 'Todos os campos são obrigatórios.']);
    exit;
}

// Carregar dados dos usuários
$users = loadJsonData(USERS_FILE);

// Verificar se o ID do dispositivo já existe
$existingDeviceUser = findByField($users, 'device_id', $device_id);
if ($existingDeviceUser) {
    echo json_encode(['status' => 'error', 'message' => 'Este dispositivo já está registrado.']);
    exit;
}

// Verificar se o e-mail já existe
$existingEmailUser = findByField($users, 'email', $email);
if ($existingEmailUser) {
    echo json_encode(['status' => 'error', 'message' => 'Este e-mail já está cadastrado.']);
    exit;
}

// Hash da senha
$hashed_password = password_hash($password, PASSWORD_DEFAULT);
$plan_expiration = date('Y-m-d', strtotime('+2 days'));

// Inserir novo usuário
$newUser = [
    'id' => getNextId($users),
    'email' => $email,
    'password' => $hashed_password,
    'device_id' => $device_id,
    'plan_expiration' => $plan_expiration,
    'is_banned' => 0,
    'created_at' => date('Y-m-d H:i:s')
];

$users[] = $newUser;

if (saveJsonData(USERS_FILE, $users)) {
    echo json_encode(['status' => 'success', 'message' => 'Usuário registrado com sucesso.']);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Erro ao registrar o usuário.']);
}
?>