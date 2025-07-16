<?php
include 'supabase_config.php';

header('Content-Type: application/json');

$user_id = $_POST['user_id'] ?? '';
$session_token = $_POST['session_token'] ?? '';

if (empty($user_id) || empty($session_token)) {
    echo json_encode(['status' => 'error', 'message' => 'ID do usuário e token de sessão são obrigatórios.']);
    exit;
}

// Verificar sessão
$session = supabase_request('GET', 'sessions', [], ['user_id' => 'eq.' . $user_id, 'session_token' => 'eq.' . $session_token]);

if (empty($session)) {
    echo json_encode(['status' => 'error', 'message' => 'Sessão inválida.']);
    exit;
}

// Buscar dados do perfil
$user_data = supabase_request('GET', 'users', [], ['id' => 'eq.' . $user_id, 'select' => 'email,plan_expiration']);
$user = $user_data[0];

function obfuscate_email($email) {
    $parts = explode('@', $email);
    $name = $parts[0];
    $domain = $parts[1];

    if (strlen($name) > 3) {
        $name = substr($name, 0, 3) . str_repeat('*', strlen($name) - 3);
    }

    return $name . '@' . $domain;
}

echo json_encode([
    'status' => 'success',
    'email' => obfuscate_email($user['email']),
    'plan_expiration' => $user['plan_expiration']
]);
?>
