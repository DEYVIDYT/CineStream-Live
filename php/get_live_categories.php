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

// Credenciais do Xtream - PREENCHA COM SEUS DADOS
$xtream_server = 'http://xtream.example.com';
$xtream_username = 'your_username';
$xtream_password = 'your_password';

$url = "$xtream_server/player_api.php?username=$xtream_username&password=$xtream_password&action=get_live_categories";

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
$output = curl_exec($ch);
curl_close($ch);

echo $output;
?>