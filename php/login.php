<?php
include 'json_config.php';
session_start();

header('Content-Type: application/json');

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$device_id = $_POST['device_id'] ?? '';

if (empty($email) || empty($password) || empty($device_id)) {
    echo json_encode(['status' => 'error', 'message' => 'Email, senha e ID do dispositivo são obrigatórios.']);
    exit;
}

// Carregar dados dos usuários
$users = loadJsonData(USERS_FILE);

// Buscar usuário
$user = findByField($users, 'email', $email);

if ($user) {
    if (password_verify($password, $user['password'])) {
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

        // Carregar dados das sessões
        $sessions = loadJsonData(SESSIONS_FILE);

        // Verificar se já existe uma sessão para este dispositivo
        $existingSession = null;
        foreach ($sessions as $session) {
            if ($session['user_id'] == $user['id'] && $session['device_id'] === $device_id) {
                $existingSession = $session;
                break;
            }
        }

        if (!$existingSession) {
            // Excluir sessões antigas para outros dispositivos
            $sessions = deleteAllByField($sessions, 'user_id', $user['id']);
        }

        // Gerar token de sessão
        $session_token = bin2hex(random_bytes(32));

        // Inserir nova sessão
        $sessions[] = [
            'id' => getNextId($sessions),
            'user_id' => $user['id'],
            'session_token' => $session_token,
            'device_id' => $device_id,
            'created_at' => date('Y-m-d H:i:s')
        ];

        saveJsonData(SESSIONS_FILE, $sessions);

        // Registrar atividade
        $activity_logs = loadJsonData(ACTIVITY_LOGS_FILE);
        $activity_logs[] = [
            'id' => getNextId($activity_logs),
            'user_id' => $user['id'],
            'login_time' => date('Y-m-d H:i:s')
        ];
        saveJsonData(ACTIVITY_LOGS_FILE, $activity_logs);

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
                    $response['iptv_info'] = 'O CineStream Live coleta e disponibiliza múltiplas listas IPTV de alta qualidade. O sistema seleciona automaticamente a melhor lista disponível para você.';
                }
            }
        } else {
            $response['plan_expired'] = true;
            $response['renewal_price'] = 'R$ 4,00';
            $response['renewal_url'] = 'https://t.me/VPlay0';
            $response['iptv_description'] = 'O CineStream Live coleta diversas listas IPTV premium e seleciona automaticamente a melhor opção disponível. Com o plano ativo, você terá acesso a centenas de canais de qualidade HD/Full HD com atualizações automáticas.';
        }

        echo json_encode($response);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Senha incorreta.']);
    }
} else {
    echo json_encode(['status' => 'error', 'message' => 'Usuário não encontrado.']);
}
?>