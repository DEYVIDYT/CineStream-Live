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

// Credenciais do Xtream - PREENCHA COM SEUS DADOS
$xtream_server = 'http://xtream.example.com';
$xtream_username = 'your_username';
$xtream_password = 'your_password';

$url = "$xtream_server/player_api.php?username=$xtream_username&password=$xtream_password&action=get_live_streams";

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
$output = curl_exec($ch);
curl_close($ch);

echo $output;
?>
