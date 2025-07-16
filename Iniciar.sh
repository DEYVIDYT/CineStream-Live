#!/data/data/com.termux/files/usr/bin/bash

# Cores ANSI
VERDE="\033[1;32m"
AZUL="\033[1;34m"
AMARELO="\033[1;33m"
VERMELHO="\033[1;31m"
RESET="\033[0m"

# Caminho do servidor
DIRETORIO="/storage/emulated/0/.sketchware/data/947/files/php/"

echo -e "${AZUL}[*] Acessando diretório do servidor...${RESET}"
cd "$DIRETORIO" || { echo -e "${VERMELHO}[!] Erro: Diretório não encontrado.${RESET}"; exit 1; }

# Iniciar PHP em segundo plano
echo -e "${VERDE}[✓] Servidor PHP iniciado em 127.0.0.1:8080${RESET}"
php -S 127.0.0.1:8080 > /dev/null 2>&1 &

sleep 2

echo -e "${AZUL}[*] Criando túnel com Cloudflared...${RESET}"

# Executar Cloudflared e capturar saída em tempo real
cloudflared tunnel --url http://127.0.0.1:8080 | while read line; do
    echo "$line"

    # Detectar e exibir URL
    if echo "$line" | grep -q "trycloudflare.com"; then
        URL=$(echo "$line" | grep -o 'https://[a-zA-Z0-9.-]*\.trycloudflare\.com')
        echo -e "${AMARELO}[🌐] Sua URL pública:${RESET} ${VERDE}$URL${RESET}"
    fi
done
