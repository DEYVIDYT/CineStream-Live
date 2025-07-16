<?php
// Configuração para armazenamento JSON
define('DATA_DIR', __DIR__ . '/data/');

// Arquivo dos dados
define('USERS_FILE', DATA_DIR . 'users.json');
define('SESSIONS_FILE', DATA_DIR . 'sessions.json');
define('ACTIVITY_LOGS_FILE', DATA_DIR . 'activity_logs.json');

// Criar pasta de dados se não existir
if (!file_exists(DATA_DIR)) {
    mkdir(DATA_DIR, 0755, true);
}

// Funções utilitárias para manipular JSON

function loadJsonData($file) {
    if (!file_exists($file)) {
        return [];
    }
    $content = file_get_contents($file);
    return json_decode($content, true) ?: [];
}

function saveJsonData($file, $data) {
    return file_put_contents($file, json_encode($data, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
}

function getNextId($data) {
    if (empty($data)) {
        return 1;
    }
    $maxId = 0;
    foreach ($data as $item) {
        if (isset($item['id']) && $item['id'] > $maxId) {
            $maxId = $item['id'];
        }
    }
    return $maxId + 1;
}

function findByField($data, $field, $value) {
    foreach ($data as $item) {
        if (isset($item[$field]) && $item[$field] === $value) {
            return $item;
        }
    }
    return null;
}

function findAllByField($data, $field, $value) {
    $results = [];
    foreach ($data as $item) {
        if (isset($item[$field]) && $item[$field] === $value) {
            $results[] = $item;
        }
    }
    return $results;
}

function updateByField($data, $field, $value, $updates) {
    foreach ($data as &$item) {
        if (isset($item[$field]) && $item[$field] === $value) {
            foreach ($updates as $key => $val) {
                $item[$key] = $val;
            }
            return true;
        }
    }
    return false;
}

function deleteByField($data, $field, $value) {
    foreach ($data as $key => $item) {
        if (isset($item[$field]) && $item[$field] === $value) {
            unset($data[$key]);
            return array_values($data); // Reindexar array
        }
    }
    return $data;
}

function deleteAllByField($data, $field, $value) {
    $filtered = [];
    foreach ($data as $item) {
        if (!isset($item[$field]) || $item[$field] !== $value) {
            $filtered[] = $item;
        }
    }
    return $filtered;
}

// Inicializar arquivos se não existirem
function initializeJsonFiles() {
    if (!file_exists(USERS_FILE)) {
        saveJsonData(USERS_FILE, []);
    }
    
    if (!file_exists(SESSIONS_FILE)) {
        saveJsonData(SESSIONS_FILE, []);
    }
    
    if (!file_exists(ACTIVITY_LOGS_FILE)) {
        saveJsonData(ACTIVITY_LOGS_FILE, []);
    }
}

// Inicializar na primeira execução
initializeJsonFiles();
?>