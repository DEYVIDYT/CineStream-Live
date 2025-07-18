import psycopg2
from rich.console import Console
from rich.progress import track

# 🎨 Interface colorida
console = Console()

# 🔐 Conexão PostgreSQL do Supabase
DB_CONFIG = {
    "host": "db.hblfvznhufuyzwvnngcw.supabase.co",
    "port": 5432,
    "dbname": "postgres",
    "user": "postgres",
    "password": "<SUA_SENHA_DO_BANCO_SUPABASE>"
}

# 🧱 Estrutura SQL adaptada para PostgreSQL
SQL_COMMANDS = [
    # Tabela users
    """
    CREATE TABLE IF NOT EXISTS users (
        id SERIAL PRIMARY KEY,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        plan_id INTEGER,
        plan_expiration DATE,
        device_id VARCHAR(255),
        is_banned BOOLEAN NOT NULL DEFAULT FALSE,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        device_fingerprint VARCHAR(255)
    );
    """,

    # Tabela plans
    """
    CREATE TABLE IF NOT EXISTS plans (
        id SERIAL PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        duration_days INTEGER NOT NULL
    );
    """,

    # Inserir plano padrão
    """
    INSERT INTO plans (id, name, duration_days)
    VALUES (1, 'Plano Padrão', 30)
    ON CONFLICT (id) DO NOTHING;
    """,

    # Tabela sessions
    """
    CREATE TABLE IF NOT EXISTS sessions (
        id SERIAL PRIMARY KEY,
        user_id INTEGER NOT NULL,
        session_token VARCHAR(255) NOT NULL,
        device_id VARCHAR(255) NOT NULL,
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_user_sessions FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
    """,

    # Tabela activity_logs
    """
    CREATE TABLE IF NOT EXISTS activity_logs (
        id SERIAL PRIMARY KEY,
        user_id INTEGER NOT NULL,
        login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_user_activity FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
    """
]

def executar_comandos():
    console.rule("[bold cyan]🔗 Conectando ao Supabase PostgreSQL...[/bold cyan]")
    try:
        with psycopg2.connect(**DB_CONFIG) as conn:
            with conn.cursor() as cur:
                for i, sql in enumerate(track(SQL_COMMANDS, description="🚧 Criando tabelas...")):
                    cur.execute(sql)
            conn.commit()
        console.rule("[bold green]✅ Tabelas criadas com sucesso![/bold green]")
    except Exception as e:
        console.print(f"[bold red]Erro ao executar comandos SQL:[/bold red] {e}")

if __name__ == "__main__":
    executar_comandos()
