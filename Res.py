import requests
from Crypto.Cipher import AES
from colorama import Fore, init

init(autoreset=True)

# Hex para bytes
def to_bytes(hex_str):
    return bytes.fromhex(hex_str)

# AES CBC decrypt
def decrypt_cookie(ciphertext, key, iv):
    cipher = AES.new(key, AES.MODE_CBC, iv)
    decrypted = cipher.decrypt(ciphertext)
    return decrypted

# Dados do JS
key_hex = "f655ba9d09a112d4968c63579db590b4"
iv_hex = "98344c2eee86c3994890592585b49f80"
ciphertext_hex = "43395c2b65f195405789ea53add845e0"

# Converter
key = to_bytes(key_hex)
iv = to_bytes(iv_hex)
ciphertext = to_bytes(ciphertext_hex)

# Descriptografar
decrypted = decrypt_cookie(ciphertext, key, iv)

# Cookie (em hex)
cookie_value = decrypted.hex()

# URL alvo
url = "https://vplay.liveblog365.com/login.php?i=1"

# Headers com o cookie gerado
headers = {
    "User-Agent": "Mozilla/5.0",
    "Cookie": f"__test={cookie_value}"
}

# Fazendo a requisição
try:
    response = requests.get(url, headers=headers)
    print(Fore.YELLOW + f"Status Code: {response.status_code}")
    print(Fore.GREEN + response.text)
except requests.exceptions.RequestException as e:
    print(Fore.RED + f"Erro na requisição: {e}")
