<?php
include '../supabase_config.php';

header('Content-Type: application/json');

$auth_token = "f3a8e0d8a5f2b7c9f9ad6c2eb37dd28cb3fa6ff2390b0a6129739e2c5a891d43";
$provided_token = $_GET['token'] ?? '';

if ($provided_token !== $auth_token) {
    echo json_encode(['status' => 'error', 'message' => 'Token de autenticação inválido.']);
    exit;
}

$action = $_GET['action'] ?? '';

switch ($action) {
    case 'get_stats':
        getStats();
        break;
    case 'get_users':
        getUsers();
        break;
    case 'search_users':
        searchUsers();
        break;
    case 'add_plan':
        addPlan();
        break;
    case 'toggle_ban':
        toggleBan();
        break;
    case 'delete_user':
        deleteUser();
        break;
    case 'remove_days':
        removeDays();
        break;
    default:
        echo json_encode(['status' => 'error', 'message' => 'Ação inválida.']);
}

function getStats() {
    // Usuários online (sessões ativas nos últimos 5 minutos)
    $online_users_data = supabase_request('GET', 'sessions', [], [
        'created_at' => 'gte.' . date('Y-m-d\TH:i:s', strtotime('-5 minutes'))
    ]);
    $online_users = count(array_unique(array_column($online_users_data, 'user_id')));

    // Usuários hoje
    $today_users_data = supabase_request('GET', 'activity_logs', [], [
        'login_time' => 'gte.' . date('Y-m-d')
    ]);
    $today_users = count(array_unique(array_column($today_users_data, 'user_id')));

    // Total de usuários
    $total_users_data = supabase_request('GET', 'users', [], ['select' => 'count']);
    $total_users = $total_users_data[0]['count'] ?? 0;

    // Usuários com plano ativo
    $active_plans_data = supabase_request('GET', 'users', [], [
        'plan_expiration' => 'gte.' . date('Y-m-d'),
        'select' => 'count'
    ]);
    $active_plans = $active_plans_data[0]['count'] ?? 0;

    // Usuários banidos
    $banned_users_data = supabase_request('GET', 'users', [], [
        'is_banned' => 'eq.true',
        'select' => 'count'
    ]);
    $banned_users = $banned_users_data[0]['count'] ?? 0;

    echo json_encode([
        'online_users' => $online_users,
        'today_users' => $today_users,
        'total_users' => $total_users,
        'active_plans' => $active_plans,
        'banned_users' => $banned_users
    ]);
}

function getUsers() {
    $search = $_GET['search'] ?? '';
    $status = $_GET['status'] ?? 'all';
    $plan_status = $_GET['plan_status'] ?? 'all';

    $params = [];

    // Filtro por email
    if (!empty($search)) {
        $params['email'] = 'like.*' . $search . '*';
    }

    // Filtro por status de ban
    if ($status === 'banned') {
        $params['is_banned'] = 'eq.true';
    } elseif ($status === 'active') {
        $params['is_banned'] = 'eq.false';
    }

    // Filtro por status do plano
    if ($plan_status === 'active') {
        $params['plan_expiration'] = 'gte.' . date('Y-m-d');
    } elseif ($plan_status === 'expired') {
        $params['plan_expiration'] = 'lt.' . date('Y-m-d');
    }

    $users = supabase_request('GET', 'users', [], $params);

    foreach ($users as &$user) {
        $user['plan_active'] = ($user['plan_expiration'] &&
                              $user['plan_expiration'] !== '1970-01-01' &&
                              strtotime($user['plan_expiration']) >= time());
    }

    echo json_encode($users);
}

function toggleBan() {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['user_id'] ?? 0;

    if ($userId <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'ID de usuário inválido.']);
        return;
    }

    $user = supabase_request('GET', 'users', [], ['id' => 'eq.' . $userId])[0];
    $new_ban_status = !$user['is_banned'];

    $result = supabase_request('PATCH', 'users', ['is_banned' => $new_ban_status], ['id' => 'eq.' . $userId]);

    if (!isset($result['error'])) {
        echo json_encode(['status' => 'success']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erro ao alterar o status de banido.']);
    }
}

function removeDays() {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['user_id'] ?? 0;

    if ($userId <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'ID de usuário inválido.']);
        return;
    }

    $result = supabase_request('PATCH', 'users', ['plan_expiration' => '1970-01-01'], ['id' => 'eq.' . $userId]);

    if (!isset($result['error'])) {
        echo json_encode(['status' => 'success']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erro ao remover os dias.']);
    }
}

function deleteUser() {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['user_id'] ?? 0;

    if ($userId <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'ID de usuário inválido.']);
        return;
    }

    $result = supabase_request('DELETE', 'users', [], ['id' => 'eq.' . $userId]);

    if (!isset($result['error'])) {
        echo json_encode(['status' => 'success']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erro ao remover o usuário.']);
    }
}

function addPlan() {
    $data = json_decode(file_get_contents('php://input'), true);
    $email = $data['email'] ?? '';
    $days = $data['days'] ?? 0;

    if (empty($email) || $days <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'Email e duração são obrigatórios.']);
        return;
    }

    // Obter data de expiração atual
    $user_data = supabase_request('GET', 'users', [], ['email' => 'eq.' . $email]);
    $current_expiration = $user_data[0]['plan_expiration'] ?? null;

    $new_expiration = date('Y-m-d', strtotime("+$days days"));
    if ($current_expiration && strtotime($current_expiration) > time()) {
        // Se o plano atual ainda for válido, adicione dias à data de expiração existente
        $new_expiration = date('Y-m-d', strtotime("$current_expiration +$days days"));
    }

    // Atualizar plano do usuário
    $result = supabase_request('PATCH', 'users', ['plan_expiration' => $new_expiration], ['email' => 'eq.' . $email]);

    if (!isset($result['error'])) {
        echo json_encode(['status' => 'success']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erro ao adicionar o plano.']);
    }
}
?>
