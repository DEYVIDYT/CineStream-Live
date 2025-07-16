<?php
// Configuração do Supabase
$supabase_url = 'https://hblfvznhufuyzwvnngcw.supabase.co';
$supabase_key = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhibGZ2em5odWZ1eXp3dm5uZ2N3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTI2NzUyODAsImV4cCI6MjA2ODI1MTI4MH0.nbQUQm_N8jRFncrigCJJu8Nq8Gae0TkBpdvmc9LG5qA';

// Função para fazer requisições à API do Supabase
function supabase_request($method, $table, $data = [], $params = []) {
    global $supabase_url, $supabase_key;

    $url = "$supabase_url/rest/v1/$table";

    if (!empty($params)) {
        $url .= '?' . http_build_query($params);
    }

    $headers = [
        'apikey: ' . $supabase_key,
        'Authorization: Bearer ' . $supabase_key,
        'Content-Type: application/json',
        'Prefer: return=representation',
    ];

    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

    if ($method === 'POST') {
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    } elseif ($method === 'PATCH') {
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'PATCH');
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
    } elseif ($method === 'DELETE') {
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, 'DELETE');
    }

    $response = curl_exec($ch);
    curl_close($ch);

    return json_decode($response, true);
}
?>
