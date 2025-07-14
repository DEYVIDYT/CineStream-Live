# Arquitetura do Servidor CineStream

Este documento descreve a arquitetura do servidor para o aplicativo CineStream, projetado para ser escalável, portátil e robusto.

## Visão Geral da Arquitetura

A arquitetura do servidor é baseada em microservices, com cada serviço sendo responsável por uma área de negócio específica. Os serviços são conteinerizados usando Docker, o que facilita a implantação e a portabilidade.

### Tecnologias Utilizadas

- **Backend:** Node.js com NestJS (TypeScript)
- **Banco de Dados:** PostgreSQL (a ser integrado)
- **Autenticação:** JSON Web Tokens (JWT)
- **Conteinerização:** Docker e Docker Compose

### Microservices

1.  **Serviço de Autenticação (`auth-service`):**
    -   Responsável pelo registro e login de usuários.
    -   Gera tokens JWT para autenticação.
    -   Porta: `3001`

2.  **Serviço de Gerenciamento de Usuários (`user-service`):**
    -   Gerencia os dados do perfil do usuário.
    -   Controla os planos de assinatura dos usuários.
    -   Monitora a atividade do usuário.
    -   Porta: `3002`

3.  **Serviço do Painel (`dashboard-service`):**
    -   Fornece dados para o painel de suporte.
    -   Permite que os administradores visualizem estatísticas e gerenciem usuários.
    -   Porta: `3003`

---

## Documentação da API

### Serviço de Autenticação (`/auth`)

-   **`POST /auth/register`**: Registra um novo usuário.
    -   Corpo: `{ "email": "user@example.com", "password": "password123" }`
-   **`POST /auth/login`**: Autentica um usuário e retorna um token JWT.
    -   Corpo: `{ "email": "user@example.com", "password": "password123" }`

### Serviço de Gerenciamento de Usuários (`/users`)

-   **`GET /users`**: Retorna uma lista de todos os usuários.
-   **`GET /users/:id`**: Retorna um único usuário por ID.
-   **`POST /users`**: Cria um novo usuário.
-   **`PUT /users/:id`**: Atualiza um usuário existente.
-   **`DELETE /users/:id`**: Remove um usuário.
-   **`POST /users/:id/plan`**: Atualiza o plano de um usuário.

### Serviço do Painel (`/dashboard`)

-   **`GET /dashboard/stats/active-now`**: Retorna o número de usuários ativos no momento.
-   **`GET /dashboard/stats/active-today`**: Retorna o número de usuários que estiveram ativos hoje.
-   **`GET /dashboard/users`**: Retorna uma lista de usuários para o painel.
-   **`POST /dashboard/users/:id/grant-free-plan`**: Concede um plano gratuito a um usuário.

---

## Como Executar os Serviços (Usando Docker Compose)

Para executar todos os serviços, você precisará do Docker e do Docker Compose instalados.

1.  **Crie um arquivo `docker-compose.yml`** na raiz do projeto com o seguinte conteúdo:

    ```yaml
    version: '3.8'
    services:
      auth-service:
        build: ./server/auth-service
        ports:
          - "3001:3001"
      user-service:
        build: ./server/user-service
        ports:
          - "3002:3002"
      dashboard-service:
        build: ./server/dashboard-service
        ports:
          - "3003:3003"
    ```

2.  **Crie um `Dockerfile`** em cada diretório de serviço (`auth-service`, `user-service`, `dashboard-service`) com o seguinte conteúdo:

    ```dockerfile
    FROM node:16
    WORKDIR /usr/src/app
    COPY package*.json ./
    RUN npm install
    COPY . .
    RUN npm run build
    CMD ["node", "dist/main"]
    ```

3.  **Execute os serviços** a partir da raiz do projeto:

    ```bash
    docker-compose up -d --build
    ```
