<?php
include 'supabase_config.php';

header('Content-Type: application/json');

$user_id = $_POST['user_id'] ?? '';
$device_id = $_POST['device_id'] ?? '';

if (empty($user_id) || empty($device_id)) {
    echo json_encode(['status' => 'error', 'message' => 'ID do usuário e ID do dispositivo são obrigatórios.']);
    exit;
}

// Verificar se o usuário está banido
$user_data = supabase_request('GET', 'users', [], ['id' => 'eq.' . $user_id, 'select' => 'is_banned']);
if (!empty($user_data) && $user_data[0]['is_banned']) {
    echo json_encode(['status' => 'banned', 'message' => 'Este usuário está banido.']);
    exit;
}

// Verificar se o dispositivo está banido
$device_data = supabase_request('GET', 'users', [], ['device_id' => 'eq.' . $device_id, 'is_banned' => 'eq.true']);
if (!empty($device_data)) {
    echo json_encode(['status' => 'banned', 'message' => 'Este dispositivo está banido.']);
    exit;
}

echo json_encode(['status' => 'ok']);
?>
