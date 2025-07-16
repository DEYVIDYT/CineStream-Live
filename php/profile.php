<?php
include 'json_config.php';

header('Content-Type: application/json');

$user_id = $_POST['user_id'] ?? '';
$session_token = $_POST['session_token'] ?? '';

if (empty($user_id) || empty($session_token)) {
    echo json_encode(['status' => 'error', 'message' => 'ID do usuário e token de sessão são obrigatórios.']);
    exit;
}

// Carregar dados das sessões
$sessions = loadJsonData(SESSIONS_FILE);

// Verificar sessão
$validSession = null;
foreach ($sessions as $session) {
    if ($session['user_id'] == $user_id && $session['session_token'] === $session_token) {
        $validSession = $session;
        break;
    }
}

if (!$validSession) {
    echo json_encode(['status' => 'error', 'message' => 'Sessão inválida.']);
    exit;
}

// Carregar dados dos usuários
$users = loadJsonData(USERS_FILE);

// Buscar dados do perfil
$user = findByField($users, 'id', (int)$user_id);
if (!$user) {
    echo json_encode(['status' => 'error', 'message' => 'Usuário não encontrado.']);
    exit;
}

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