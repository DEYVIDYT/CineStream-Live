<?php
include 'json_config.php';

header('Content-Type: application/json');

$user_id = $_POST['user_id'] ?? '';
$device_id = $_POST['device_id'] ?? '';

if (empty($user_id) || empty($device_id)) {
    echo json_encode(['status' => 'error', 'message' => 'ID do usuário e ID do dispositivo são obrigatórios.']);
    exit;
}

// Carregar dados dos usuários
$users = loadJsonData(USERS_FILE);

// Verificar se o usuário está banido
$user = findByField($users, 'id', (int)$user_id);
if (!$user) {
    echo json_encode(['status' => 'error', 'message' => 'Usuário não encontrado.']);
    exit;
}

if ($user['is_banned']) {
    echo json_encode(['status' => 'banned', 'message' => 'Este usuário está banido.']);
    exit;
}

// Verificar se o dispositivo está banido
$bannedDevice = null;
foreach ($users as $u) {
    if ($u['device_id'] === $device_id && $u['is_banned']) {
        $bannedDevice = $u;
        break;
    }
}

if ($bannedDevice) {
    echo json_encode(['status' => 'banned', 'message' => 'Este dispositivo está banido.']);
    exit;
}

echo json_encode(['status' => 'ok']);
?>