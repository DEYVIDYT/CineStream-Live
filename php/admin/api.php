<?php
include '../db_config.php';

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
        getStats($conn);
        break;
    case 'get_users':
        getUsers($conn);
        break;
    case 'add_plan':
        addPlan($conn);
        break;
    case 'toggle_ban':
        toggleBan($conn);
        break;
    case 'delete_user':
        deleteUser($conn);
        break;
    case 'remove_days':
        removeDays($conn);
        break;
    default:
        echo json_encode(['status' => 'error', 'message' => 'Ação inválida.']);
}

function getStats($conn) {
    // Usuários online (sessões ativas nos últimos 5 minutos)
    $sql = "SELECT COUNT(DISTINCT user_id) as online_users FROM sessions WHERE created_at >= NOW() - INTERVAL 5 MINUTE";
    $result = $conn->query($sql);
    $online_users = $result->fetch_assoc()['online_users'];

    // Usuários hoje
    $sql = "SELECT COUNT(DISTINCT user_id) as today_users FROM activity_logs WHERE login_time >= CURDATE()";
    $result = $conn->query($sql);
    $today_users = $result->fetch_assoc()['today_users'];

    echo json_encode([
        'online_users' => $online_users,
        'today_users' => $today_users
    ]);
}

function getUsers($conn) {
    $sql = "SELECT id, email, plan_expiration, is_banned FROM users";
    $result = $conn->query($sql);
    $users = [];
    while ($row = $result->fetch_assoc()) {
        $users[] = $row;
    }
    echo json_encode($users);
}

function toggleBan($conn) {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['user_id'] ?? 0;

    if ($userId <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'ID de usuário inválido.']);
        return;
    }

    $sql = "UPDATE users SET is_banned = 1 - is_banned WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $userId);

    if ($stmt->execute()) {
        echo json_encode(['status' => 'success']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erro ao alterar o status de banido.']);
    }

    $stmt->close();
}

function removeDays($conn) {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['user_id'] ?? 0;

    if ($userId <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'ID de usuário inválido.']);
        return;
    }

    $sql = "UPDATE users SET plan_expiration = '1970-01-01' WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $userId);

    if ($stmt->execute()) {
        echo json_encode(['status' => 'success']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erro ao remover os dias.']);
    }

    $stmt->close();
}

function deleteUser($conn) {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['user_id'] ?? 0;

    if ($userId <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'ID de usuário inválido.']);
        return;
    }

    $sql = "DELETE FROM users WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $userId);

    if ($stmt->execute()) {
        echo json_encode(['status' => 'success']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erro ao remover o usuário.']);
    }

    $stmt->close();
}

function addPlan($conn) {
    $data = json_decode(file_get_contents('php://input'), true);
    $email = $data['email'] ?? '';
    $days = $data['days'] ?? 0;

    if (empty($email) || $days <= 0) {
        echo json_encode(['status' => 'error', 'message' => 'Email e duração são obrigatórios.']);
        return;
    }

    // Obter data de expiração atual
    $sql = "SELECT plan_expiration FROM users WHERE email = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->bind_result($current_expiration);
    $stmt->fetch();
    $stmt->close();

    $new_expiration = date('Y-m-d', strtotime("+$days days"));
    if ($current_expiration && strtotime($current_expiration) > time()) {
        // Se o plano atual ainda for válido, adicione dias à data de expiração existente
        $new_expiration = date('Y-m-d', strtotime("$current_expiration +$days days"));
    }

    // Atualizar plano do usuário
    $sql = "UPDATE users SET plan_expiration = ? WHERE email = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ss", $new_expiration, $email);

    if ($stmt->execute()) {
        echo json_encode(['status' => 'success']);
    } else {
        echo json_encode(['status' => 'error', 'message' => 'Erro ao adicionar o plano.']);
    }

    $stmt->close();
}

$conn->close();
?>
