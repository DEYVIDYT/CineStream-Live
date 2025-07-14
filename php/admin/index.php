<!DOCTYPE html>
<html>
<head>
    <title>Painel de Administração</title>
    <style>
        body { font-family: sans-serif; }
        .container { width: 800px; margin: 0 auto; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Painel de Administração</h1>

        <h2>Usuários Online</h2>
        <p>Atualmente online: <span id="online-users">0</span></p>

        <h2>Usuários Hoje</h2>
        <p>Total de usuários hoje: <span id="today-users">0</span></p>

        <h2>Gerenciar Planos de Usuários</h2>
        <form id="add-plan-form">
            <input type="email" id="email" placeholder="Email do usuário" required>
            <input type="number" id="days" placeholder="Duração (dias)" required>
            <button type="submit">Adicionar Plano</button>
        </form>

        <h2>Usuários</h2>
        <table id="users-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Email</th>
                    <th>Expiração do Plano</th>
                </tr>
            </thead>
            <tbody>
                <!-- Os dados dos usuários serão inseridos aqui -->
            </tbody>
        </table>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Carregar dados iniciais
            loadStats();
            loadUsers();

            // Adicionar plano
            document.getElementById('add-plan-form').addEventListener('submit', function(e) {
                e.preventDefault();
                const email = document.getElementById('email').value;
                const days = document.getElementById('days').value;

                fetch('api.php?action=add_plan', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, days })
                })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        alert('Plano adicionado com sucesso!');
                        loadUsers();
                    } else {
                        alert('Erro: ' + data.message);
                    }
                });
            });
        });

        function loadStats() {
            fetch('api.php?action=get_stats')
                .then(response => response.json())
                .then(data => {
                    document.getElementById('online-users').textContent = data.online_users;
                    document.getElementById('today-users').textContent = data.today_users;
                });
        }

        function loadUsers() {
            fetch('api.php?action=get_users')
                .then(response => response.json())
                .then(data => {
                    const tbody = document.getElementById('users-table').querySelector('tbody');
                    tbody.innerHTML = '';
                    data.forEach(user => {
                        const row = `<tr>
                            <td>${user.id}</td>
                            <td>${user.email}</td>
                            <td>${user.plan_expiration || 'N/A'}</td>
                        </tr>`;
                        tbody.innerHTML += row;
                    });
                });
        }
    </script>
</body>
</html>
