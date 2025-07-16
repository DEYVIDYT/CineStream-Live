<?php
include 'supabase_config.php';
include_once 'init_db.php';

// Inicializar o banco de dados, se necessário
initialize_database();

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

// Buscar dados do usuário
$user_data = supabase_request('GET', 'users', [], ['id' => 'eq.' . $user_id, 'select' => 'plan_expiration,is_banned']);
$user = $user_data[0];

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
