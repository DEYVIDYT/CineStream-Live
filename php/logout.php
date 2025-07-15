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

// Procurar e excluir a sessão específica
$sessionFound = false;
$updatedSessions = [];

foreach ($sessions as $session) {
    if ($session['user_id'] == $user_id && $session['session_token'] === $session_token) {
        $sessionFound = true;
        // Não adiciona a sessão ao array atualizado (efetivamente excluindo)
    } else {
        $updatedSessions[] = $session;
    }
}

if ($sessionFound) {
    if (saveJsonData(SESSIONS_FILE, $updatedSessions)) {
        echo json_encode(['status' => 'success', 'message' => 'Logout bem-sucedido.']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erro ao fazer logout.']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Sessão não encontrada.']);
}
?>