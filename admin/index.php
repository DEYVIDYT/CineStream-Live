<?php
session_start();

if (!isset($_SESSION['loggedin'])) {
    header('Location: login.php');
    exit;
}

include '../server/database.php';

// Get user statistics
$online_users_sql = "SELECT COUNT(*) as count FROM users WHERE last_login > NOW() - INTERVAL 5 MINUTE";
$online_users_result = $conn->query($online_users_sql);
$online_users = $online_users_result->fetch_assoc()['count'];

$today_users_sql = "SELECT COUNT(*) as count FROM users WHERE DATE(last_login) = CURDATE()";
$today_users_result = $conn->query($today_users_sql);
$today_users = $today_users_result->fetch_assoc()['count'];

// Get all users
$users_sql = "SELECT id, email, expiration_date FROM users";
$users_result = $conn->query($users_sql);

?>

<!DOCTYPE html>
<html>
<head>
    <title>Admin Panel</title>
</head>
<body>

<h2>User Statistics</h2>
<p>Users online: <?php echo $online_users; ?></p>
<p>Users today: <?php echo $today_users; ?></p>

<h2>Users</h2>
<table border="1">
    <tr>
        <th>Email</th>
        <th>Expiration Date</th>
        <th>Action</th>
    </tr>
    <?php while($row = $users_result->fetch_assoc()): ?>
    <tr>
        <td><?php echo $row['email']; ?></td>
        <td><?php echo $row['expiration_date']; ?></td>
        <td>
            <form action="extend_plan.php" method="post">
                <input type="hidden" name="user_id" value="<?php echo $row['id']; ?>">
                <input type="submit" value="Extend Plan (30 Days)">
            </form>
        </td>
    </tr>
    <?php endwhile; ?>
</table>

<a href="logout.php">Logout</a>

</body>
</html>

<?php
$conn->close();
?>
