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

// Buscar dados do usuário
$user = findByField($users, 'id', (int)$user_id);
if (!$user) {
    echo json_encode(['status' => 'error', 'message' => 'Usuário não encontrado.']);
    exit;
}

if ($user['is_banned']) {
    echo json_encode(['status' => 'banned', 'message' => 'Este usuário está banido.']);
    exit;
}

if (strtotime($user['plan_expiration']) < time()) {
    echo json_encode(['status' => 'expired', 'message' => 'Seu plano expirou.']);
    exit;
}

// Ler logins do Xtream
$xtream_logins_file = 'xtream_logins.json';
if (file_exists($xtream_logins_file)) {
    $xtream_logins = json_decode(file_get_contents($xtream_logins_file), true);
    if (!empty($xtream_logins)) {
        $random_login = $xtream_logins[array_rand($xtream_logins)];
        $xtream_server = $random_login['server'];
        $xtream_username = $random_login['username'];
        $xtream_password = $random_login['password'];
    }
}

if (isset($xtream_server)) {
    echo json_encode([
        'status' => 'success',
        'xtream_server' => $xtream_server,
        'xtream_username' => $xtream_username,
        'xtream_password' => $xtream_password
    ]);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Nenhum login do Xtream disponível.']);
}
?>