<?php
session_start();

$admin_password = "admin_password"; // Change this password

if (isset($_POST['password'])) {
    if ($_POST['password'] == $admin_password) {
        $_SESSION['loggedin'] = true;
        header('Location: index.php');
    } else {
        $error = "Invalid password";
    }
}

?>

<!DOCTYPE html>
<html>
<head>
    <title>Admin Login</title>
</head>
<body>

<?php if (isset($error)): ?>
<p><?php echo $error; ?></p>
<?php endif; ?>

<form action="login.php" method="post">
    <label for="password">Password:</label>
    <input type="password" id="password" name="password">
    <input type="submit" value="Login">
</form>

</body>
</html>
