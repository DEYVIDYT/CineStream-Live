<?php
include_once 'supabase_config.php';

function initialize_database() {
    // Verificar se a tabela 'users' já existe
    $users_table = supabase_request('GET', 'users', [], ['select' => 'count']);
    if (isset($users_table[0]['count']) && $users_table[0]['count'] > 0) {
        return; // O banco de dados já foi inicializado
    }

    // Ler o arquivo de estrutura do banco de dados
    $sql = file_get_contents('db_structure.sql');

    // Executar o SQL para criar as tabelas
    supabase_sql_request($sql);

    // Inserir o plano padrão
    supabase_request('POST', 'plans', [
        'id' => 1,
        'name' => 'Plano Padrão',
        'duration_days' => 30
    ]);
}
?>
