<?php
include 'supabase_config.php';
include_once 'init_db.php';
session_start();

// Inicializar o banco de dados, se necessário
initialize_database();

header('Content-Type: application/json');

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$device_id = $_POST['device_id'] ?? '';

if (empty($email) || empty($password) || empty($device_id)) {
    echo json_encode(['status' => 'error', 'message' => 'Email, senha e ID do dispositivo são obrigatórios.']);
    exit;
}

// Buscar usuário no Supabase
$users = supabase_request('GET', 'users', [], ['email' => 'eq.' . $email]);

if (!empty($users)) {
    $user = $users[0];
    if (password_verify($password, $user['password'])) {
        if ($user['is_banned']) {
            echo json_encode(['status' => 'banned', 'message' => 'Este usuário está banido.']);
            exit;
        }

        // Verificar se o dispositivo está banido
        $banned_devices = supabase_request('GET', 'users', [], ['device_id' => 'eq.' . $device_id, 'is_banned' => 'eq.true']);
        if (!empty($banned_devices)) {
            echo json_encode(['status' => 'banned', 'message' => 'Este dispositivo está banido.']);
            exit;
        }

        // Verificar se já existe uma sessão para este dispositivo
        $sessions = supabase_request('GET', 'sessions', [], ['user_id' => 'eq.' . $user['id'], 'device_id' => 'eq.' . $device_id]);

        if (empty($sessions)) {
            // Excluir sessões antigas para outros dispositivos
            supabase_request('DELETE', 'sessions', [], ['user_id' => 'eq.' . $user['id']]);
        }

        // Gerar token de sessão
        $session_token = bin2hex(random_bytes(32));

        // Inserir nova sessão
        supabase_request('POST', 'sessions', [
            'user_id' => $user['id'],
            'session_token' => $session_token,
            'device_id' => $device_id
        ]);

        // Registrar atividade
        supabase_request('POST', 'activity_logs', ['user_id' => $user['id']]);

        $response = [
            'status' => 'success',
            'message' => 'Login bem-sucedido.',
            'user_id' => $user['id'],
            'session_token' => $session_token,
            'plan_expiration' => $user['plan_expiration'],
        ];

        // Se o plano não estiver expirado, fornecer credenciais do Xtream
        if (strtotime($user['plan_expiration']) >= time()) {
            $xtream_logins_file = 'xtream_logins.json';
            if (file_exists($xtream_logins_file)) {
                $xtream_logins = json_decode(file_get_contents($xtream_logins_file), true);
                if (!empty($xtream_logins)) {
                    $random_login = $xtream_logins[array_rand($xtream_logins)];
                    $response['xtream_server'] = $random_login['server'];
                    $response['xtream_username'] = $random_login['username'];
                    $response['xtream_password'] = $random_login['password'];
                }
            }
        }

        echo json_encode($response);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Senha incorreta.']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Usuário não encontrado.']);
}
?>
