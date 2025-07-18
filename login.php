<?php
include 'supabase_config.php';
include_once 'init_db.php';
session_start();

initialize_database();
header('Content-Type: application/json');

// Parâmetros do POST
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';
$device_id = $_POST['device_id'] ?? '';

if (empty($email) || empty($password) || empty($device_id)) {
    echo json_encode(['status' => 'error', 'message' => 'Email, senha e ID do dispositivo são obrigatórios.']);
    exit;
}

// Função para obter login de arquivo .compact
function obter_login_aleatorio_compactado($url_compact) {
    $dados_compactados = @file_get_contents($url_compact);
    if ($dados_compactados === false) return null;

    $dados = @gzinflate(substr($dados_compactados, 2));
    if ($dados === false) return null;

    $linhas = array_filter(array_map('trim', explode("\n", $dados)));
    if (empty($linhas)) return null;

    $linha = $linhas[array_rand($linhas)];
    $url_partes = parse_url($linha);
    parse_str($url_partes['query'] ?? '', $params);

    return [
        'server' => $url_partes['scheme'] . '://' . $url_partes['host'],
        'username' => $params['username'] ?? '',
        'password' => $params['password'] ?? '',
    ];
}

// Buscar usuário
$users = supabase_request('GET', 'users', [], ['email' => 'eq.' . $email]);

if (!empty($users)) {
    $user = $users[0];
    if (password_verify($password, $user['password'])) {
        if ($user['is_banned']) {
            echo json_encode(['status' => 'banned', 'message' => 'Este usuário está banido.']);
            exit;
        }

        $banned_devices = supabase_request('GET', 'users', [], [
            'device_id' => 'eq.' . $device_id,
            'is_banned' => 'eq.true'
        ]);

        if (!empty($banned_devices)) {
            echo json_encode(['status' => 'banned', 'message' => 'Este dispositivo está banido.']);
            exit;
        }

        $sessions = supabase_request('GET', 'sessions', [], [
            'user_id' => 'eq.' . $user['id'],
            'device_id' => 'eq.' . $device_id
        ]);

        if (empty($sessions)) {
            supabase_request('DELETE', 'sessions', [], ['user_id' => 'eq.' . $user['id']]);
        }

        $session_token = bin2hex(random_bytes(32));

        supabase_request('POST', 'sessions', [
            'user_id' => $user['id'],
            'session_token' => $session_token,
            'device_id' => $device_id
        ]);

        supabase_request('POST', 'activity_logs', ['user_id' => $user['id']]);

        $response = [
            'status' => 'success',
            'message' => 'Login bem-sucedido.',
            'user_id' => $user['id'],
            'session_token' => $session_token,
            'plan_expiration' => $user['plan_expiration'],
        ];

        // 🔄 Fornece Xtream somente se plano ainda ativo
        if (strtotime($user['plan_expiration']) >= time()) {
            $compact_url = 'https://github.com/DEYVIDYT/Server_vplay/raw/refs/heads/main/listas.compact';
            $login = obter_login_aleatorio_compactado($compact_url);
            if ($login) {
                $response['xtream_server'] = $login['server'];
                $response['xtream_username'] = $login['username'];
                $response['xtream_password'] = $login['password'];
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
