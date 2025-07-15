<?php
include '../json_config.php';

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
    case 'get_enhanced_stats':
        getEnhancedStats();
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
    $users = loadJsonData(USERS_FILE);
    $sessions = loadJsonData(SESSIONS_FILE);
    $activity_logs = loadJsonData(ACTIVITY_LOGS_FILE);
    
    // Usuários online (sessões ativas nos últimos 5 minutos)
    $online_users = 0;
    $five_minutes_ago = date('Y-m-d H:i:s', strtotime('-5 minutes'));
    foreach ($sessions as $session) {
        if ($session['created_at'] >= $five_minutes_ago) {
            $online_users++;
        }
    }

    // Usuários hoje
    $today_users = 0;
    $today = date('Y-m-d');
    foreach ($activity_logs as $log) {
        if (strpos($log['login_time'], $today) === 0) {
            $today_users++;
        }
    }
    
    // Total de usuários
    $total_users = count($users);
    
    // Usuários com plano ativo
    $active_plans = 0;
    $current_date = date('Y-m-d');
    foreach ($users as $user) {
        if (isset($user['plan_expiration']) && 
            $user['plan_expiration'] >= $current_date && 
            $user['plan_expiration'] !== '1970-01-01') {
            $active_plans++;
        }
    }
    
    // Usuários banidos
    $banned_users = 0;
    foreach ($users as $user) {
        if ($user['is_banned']) {
            $banned_users++;
        }
    }

    echo json_encode([
        'online_users' => $online_users,
        'today_users' => $today_users,
        'total_users' => $total_users,
        'active_plans' => $active_plans,
        'banned_users' => $banned_users
    ]);
}

function getUsers() {
    $users = loadJsonData(USERS_FILE);
    
    $search = $_GET['search'] ?? '';
    $status = $_GET['status'] ?? 'all';
    $plan_status = $_GET['plan_status'] ?? 'all';
    
    $filtered_users = [];
    
    foreach ($users as $user) {
        // Filtro por email
        if (!empty($search) && stripos($user['email'], $search) === false) {
            continue;
        }
        
        // Filtro por status de ban
        if ($status === 'banned' && !$user['is_banned']) {
            continue;
        } elseif ($status === 'active' && $user['is_banned']) {
            continue;
        }
        
        // Filtro por status do plano
        $current_date = date('Y-m-d');
        $plan_active = ($user['plan_expiration'] && 
                       $user['plan_expiration'] !== '1970-01-01' && 
                       $user['plan_expiration'] >= $current_date);
        
        if ($plan_status === 'active' && !$plan_active) {
            continue;
        } elseif ($plan_status === 'expired' && $plan_active) {
            continue;
        }
        
        // Adicionar informação se o plano está ativo
        $user['plan_active'] = $plan_active;
        $filtered_users[] = $user;
    }
    
    // Ordenar por ID desc
    usort($filtered_users, function($a, $b) {
        return $b['id'] - $a['id'];
    });
    
    echo json_encode($filtered_users);
}

function toggleBan() {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['user_id'] ?? 0;

    if ($userId <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'ID de usuário inválido.']);
        return;
    }

    $users = loadJsonData(USERS_FILE);
    
    foreach ($users as &$user) {
        if ($user['id'] == $userId) {
            $user['is_banned'] = $user['is_banned'] ? 0 : 1;
            if (saveJsonData(USERS_FILE, $users)) {
                echo json_encode(['status' => 'success']);
            } else {
                echo json_encode(['status' => 'error', 'message' => 'Erro ao alterar o status de banido.']);
            }
            return;
        }
    }
    
    echo json_encode(['status' => 'error', 'message' => 'Usuário não encontrado.']);
}

function removeDays() {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['user_id'] ?? 0;

    if ($userId <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'ID de usuário inválido.']);
        return;
    }

    $users = loadJsonData(USERS_FILE);
    
    foreach ($users as &$user) {
        if ($user['id'] == $userId) {
            $user['plan_expiration'] = '1970-01-01';
            if (saveJsonData(USERS_FILE, $users)) {
                echo json_encode(['status' => 'success']);
            } else {
                echo json_encode(['status' => 'error', 'message' => 'Erro ao remover os dias.']);
            }
            return;
        }
    }
    
    echo json_encode(['status' => 'error', 'message' => 'Usuário não encontrado.']);
}

function deleteUser() {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['user_id'] ?? 0;

    if ($userId <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'ID de usuário inválido.']);
        return;
    }

    $users = loadJsonData(USERS_FILE);
    $updatedUsers = [];
    $userFound = false;
    
    foreach ($users as $user) {
        if ($user['id'] == $userId) {
            $userFound = true;
            // Não adiciona ao array atualizado (efetivamente remove)
        } else {
            $updatedUsers[] = $user;
        }
    }
    
    if ($userFound) {
        if (saveJsonData(USERS_FILE, $updatedUsers)) {
            // Também remover sessões do usuário
            $sessions = loadJsonData(SESSIONS_FILE);
            $updatedSessions = [];
            foreach ($sessions as $session) {
                if ($session['user_id'] != $userId) {
                    $updatedSessions[] = $session;
                }
            }
            saveJsonData(SESSIONS_FILE, $updatedSessions);
            
            echo json_encode(['status' => 'success']);
        } else {
            echo json_encode(['status' => 'error', 'message' => 'Erro ao remover o usuário.']);
        }
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Usuário não encontrado.']);
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

    $users = loadJsonData(USERS_FILE);
    
    foreach ($users as &$user) {
        if ($user['email'] === $email) {
            $current_expiration = $user['plan_expiration'];
            
            $new_expiration = date('Y-m-d', strtotime("+$days days"));
            if ($current_expiration && strtotime($current_expiration) > time()) {
                // Se o plano atual ainda for válido, adicione dias à data de expiração existente
                $new_expiration = date('Y-m-d', strtotime("$current_expiration +$days days"));
            }
            
            $user['plan_expiration'] = $new_expiration;
            
            if (saveJsonData(USERS_FILE, $users)) {
                echo json_encode(['status' => 'success']);
            } else {
                echo json_encode(['status' => 'error', 'message' => 'Erro ao adicionar o plano.']);
            }
            return;
        }
    }
    
    echo json_encode(['status' => 'error', 'message' => 'Usuário não encontrado.']);
}

function getEnhancedStats() {
    getStats(); // Usa a mesma função
}

function searchUsers() {
    getUsers(); // Usa a mesma função
}
?>