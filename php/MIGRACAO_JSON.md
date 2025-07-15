# Migração MySQL para JSON - CineStream Live

Este projeto foi migrado do MySQL para utilizar arquivos JSON como banco de dados.

## Arquivos Modificados

### Configuração
- **`json_config.php`** - Substitui `db_config.php`, contém funções para manipular JSON
- **`data/`** - Pasta onde ficam armazenados os dados em JSON

### Arquivos de Dados JSON
- **`data/users.json`** - Armazena dados dos usuários
- **`data/sessions.json`** - Armazena sessões ativas
- **`data/activity_logs.json`** - Armazena logs de atividade

### Arquivos PHP Migrados
1. **`login.php`** - Sistema de login
2. **`register.php`** - Registro de usuários
3. **`check_session.php`** - Verificação de sessão
4. **`check_ban_status.php`** - Verificação de banimento
5. **`profile.php`** - Dados do perfil
6. **`logout.php`** - Sistema de logout
7. **`get_live_categories.php`** - Categorias ao vivo
8. **`get_live_streams.php`** - Streams ao vivo
9. **`refresh_xtream_credentials.php`** - Credenciais Xtream
10. **`admin/api.php`** - API administrativa

## Estrutura de Dados

### Usuários (users.json)
```json
[
  {
    "id": 1,
    "email": "usuario@exemplo.com",
    "password": "hash_da_senha",
    "device_id": "device123",
    "plan_expiration": "2024-12-31",
    "is_banned": 0,
    "created_at": "2024-07-15 10:00:00"
  }
]
```

### Sessões (sessions.json)
```json
[
  {
    "id": 1,
    "user_id": 1,
    "session_token": "token_de_sessao",
    "device_id": "device123",
    "created_at": "2024-07-15 10:00:00"
  }
]
```

### Logs de Atividade (activity_logs.json)
```json
[
  {
    "id": 1,
    "user_id": 1,
    "login_time": "2024-07-15 10:00:00"
  }
]
```

## Funcionalidades Mantidas

✅ **Login de usuários**
✅ **Registro de novos usuários**
✅ **Verificação de sessão**
✅ **Controle de banimento**
✅ **Perfil do usuário**
✅ **Logout**
✅ **Dashboard administrativo**
✅ **Gestão de usuários**
✅ **Gestão de planos**
✅ **Credenciais Xtream**

## Vantagens da Migração

1. **Sem dependência de MySQL** - Funciona em qualquer hospedagem com PHP
2. **Facilidade de backup** - Basta copiar a pasta `data/`
3. **Portabilidade** - Pode ser movido facilmente entre servidores
4. **Simplicidade** - Não precisa configurar banco de dados
5. **Transparência** - Dados ficam em formato legível

## Instalação

1. Faça upload dos arquivos PHP para seu servidor
2. Certifique-se que a pasta `php/data/` tenha permissões de escrita (755 ou 777)
3. Acesse o dashboard administrativo normalmente
4. O sistema criará automaticamente os arquivos JSON necessários

## Permissões de Arquivo

```bash
chmod 755 php/data/
chmod 644 php/data/*.json
```

## Backup

Para fazer backup do sistema, simplesmente copie a pasta `php/data/`:

```bash
cp -r php/data/ backup_$(date +%Y%m%d)/
```

## Observações Importantes

- O dashboard.html permanece exatamente igual, apenas a API backend foi alterada
- Todas as respostas dos arquivos PHP mantêm o mesmo formato JSON
- A funcionalidade é idêntica ao sistema MySQL anterior
- Os tokens de autenticação e sistema de segurança permanecem inalterados

## Suporte

O sistema foi migrado mantendo 100% da compatibilidade com o frontend existente. Todas as funcionalidades do sistema original foram preservadas.