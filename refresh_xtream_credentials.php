<?php
include 'supabase_config.php';
include_once 'init_db.php';

initialize_database();
header('Content-Type: application/json');

$user_id = $_POST['user_id'] ?? '';
$session_token = $_POST['session_token'] ?? '';

if (empty($user_id) || empty($session_token)) {
    echo json_encode(['status' => 'error', 'message' => 'ID do usuário e token de sessão são obrigatórios.']);
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

// Verificar sessão
$session = supabase_request('GET', 'sessions', [], [
    'user_id' => 'eq.' . $user_id,
    'session_token' => 'eq.' . $session_token
]);

if (empty($session)) {
    echo json_encode(['status' => 'error', 'message' => 'Sessão inválida.']);
    exit;
}

$user_data = supabase_request('GET', 'users', [], ['id' => 'eq.' . $user_id, 'select' => 'plan_expiration,is_banned']);
$user = $user_data[0];

if ($user['is_banned']) {
    echo json_encode(['status' => 'banned', 'message' => 'Este usuário está banido.']);
    exit;
}

if (strtotime($user['plan_expiration']) < time()) {
    echo json_encode(['status' => 'expired', 'message' => 'Seu plano expirou.']);
    exit;
}

// Obter dados do arquivo .compact
$compact_url = 'https://github.com/DEYVIDYT/Server_vplay/raw/refs/heads/main/listas.compact';
$login = obter_login_aleatorio_compactado($compact_url);

if ($login) {
    echo json_encode([
        'status' => 'success',
        'xtream_server' => $login['server'],
        'xtream_username' => $login['username'],
        'xtream_password' => $login['password']
    ]);
} else {
    echo json_encode(['status' => 'error', 'message' => 'Nenhum login IPTV disponível.']);
}
?>
