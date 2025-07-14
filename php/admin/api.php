<?php
include '../db_config.php';

header('Content-Type: application/json');

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
    $sql = "SELECT id, email, plan_expiration FROM users";
    $result = $conn->query($sql);
    $users = [];
    while ($row = $result->fetch_assoc()) {
        $users[] = $row;
    }
    echo json_encode($users);
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
