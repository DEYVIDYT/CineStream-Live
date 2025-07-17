<?php
include 'supabase_config.php';
include_once 'init_db.php';

// Inicializar o banco de dados, se necessário
initialize_database();

header('Content-Type: application/json');

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$device_id = $_POST['device_id'] ?? '';

if (empty($email) || empty($password) || empty($device_id)) {
    echo json_encode(['status' => 'error', 'message' => 'Todos os campos são obrigatórios.']);
    exit;
}

// Verificar se o ID do dispositivo já existe
$existing_device = supabase_request('GET', 'users', [], ['device_id' => 'eq.' . $device_id]);
if (!empty($existing_device)) {
    echo json_encode(['status' => 'error', 'message' => 'Este dispositivo já está registrado.']);
    exit;
}

// Verificar se o e-mail já existe
$existing_email = supabase_request('GET', 'users', [], ['email' => 'eq.' . $email]);
if (!empty($existing_email)) {
    echo json_encode(['status' => 'error', 'message' => 'Este e-mail já está cadastrado.']);
    exit;
}

// Hash da senha
$hashed_password = password_hash($password, PASSWORD_DEFAULT);
$plan_expiration = date('Y-m-d', strtotime('+2 days'));

// Inserir novo usuário
$result = supabase_request('POST', 'users', [
    'email' => $email,
    'password' => $hashed_password,
    'device_id' => $device_id,
    'plan_expiration' => $plan_expiration
]);

if (!isset($result['error'])) {
    echo json_encode(['status' => 'success', 'message' => 'Usuário registrado com sucesso.']);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Erro ao registrar o usuário.']);
}
?>
