<?php
include 'json_config.php';

header('Content-Type: application/json');

$user_id = $_POST['user_id'] ?? '';
$device_id = $_POST['device_id'] ?? '';

if (empty($user_id) || empty($device_id)) {
    echo json_encode(['status' => 'error', 'message' => 'ID do usuário e ID do dispositivo são obrigatórios.']);
    exit;
}

// Carregar dados das sessões
$sessions = loadJsonData(SESSIONS_FILE);

// Verificar se existe uma sessão para este usuário em um dispositivo diferente
$differentDeviceSession = null;
foreach ($sessions as $session) {
    if ($session['user_id'] == $user_id && $session['device_id'] !== $device_id) {
        $differentDeviceSession = $session;
        break;
    }
}

if ($differentDeviceSession) {
    echo json_encode(['status' => 'error', 'message' => 'Duplo login detectado.']);
} else {
    echo json_encode(['status' => 'success', 'message' => 'Sessão válida.']);
}
?>