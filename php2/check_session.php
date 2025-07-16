<?php
include 'supabase_config.php';
include_once 'init_db.php';

// Inicializar o banco de dados, se necessário
initialize_database();

header('Content-Type: application/json');

$user_id = $_POST['user_id'] ?? '';
$device_id = $_POST['device_id'] ?? '';

if (empty($user_id) || empty($device_id)) {
    echo json_encode(['status' => 'error', 'message' => 'ID do usuário e ID do dispositivo são obrigatórios.']);
    exit;
}

// Verificar se existe uma sessão para este usuário em um dispositivo diferente
$sessions = supabase_request('GET', 'sessions', [], ['user_id' => 'eq.' . $user_id, 'device_id' => 'neq.' . $device_id]);

if (!empty($sessions)) {
    echo json_encode(['status' => 'error', 'message' => 'Duplo login detectado.']);
} else {
    echo json_encode(['status' => 'success', 'message' => 'Sessão válida.']);
}
?>
