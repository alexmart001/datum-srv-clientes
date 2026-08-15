#!/usr/bin/env bash
#
# Exemplo de teste via curl do fluxo OAuth2 + JWT do datum-srv-auth /
# datum-srv-clientes, como alternativa ao Postman.
#
# Pré-requisitos:
#   - MariaDB rodando (mesma base usada pelos dois serviços)
#   - datum-srv-auth no ar (porta 9000)
#   - datum-srv-clientes no ar (porta 8080)
#   - jq instalado (brew install jq)
#
# Uso: ./scripts/test-api.sh

set -e

AUTH_URL="http://localhost:9000"
API_URL="http://localhost:8080"
CLIENT_ID="postman-client"
CLIENT_SECRET="postman-secret"

echo "== 1) Obtendo Access Token do usuário ADMIN =="
ADMIN_TOKEN=$(curl -s -u "$CLIENT_ID:$CLIENT_SECRET" \
  -d "grant_type=password&username=admin&password=admin123" \
  "$AUTH_URL/oauth2/token" | jq -r .access_token)
echo "ADMIN_TOKEN obtido (${#ADMIN_TOKEN} caracteres)"
echo

echo "== 2) Obtendo Access Token do usuário USER =="
USER_TOKEN=$(curl -s -u "$CLIENT_ID:$CLIENT_SECRET" \
  -d "grant_type=password&username=user&password=user123" \
  "$AUTH_URL/oauth2/token" | jq -r .access_token)
echo "USER_TOKEN obtido (${#USER_TOKEN} caracteres)"
echo

echo "== 3) GET /customers sem token -> esperado 401 =="
curl -s -o /dev/null -w "HTTP %{http_code}\n" "$API_URL/customers"
echo

echo "== 4) GET /customers com token USER -> esperado 200 =="
curl -s -w "\nHTTP %{http_code}\n" -H "Authorization: Bearer $USER_TOKEN" "$API_URL/customers"
echo

echo "== 5) POST /customers com token USER -> esperado 403 (USER não pode escrever) =="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST \
  -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" \
  -d '{"nome":"Teste Bloqueado","cpf":"11144477735","email":"teste@teste.com","status":"ACTIVE"}' \
  "$API_URL/customers"
echo

echo "== 6) POST /customers com token ADMIN -> esperado 200/201 =="
CREATE_RESPONSE=$(curl -s -X POST \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"nome":"Alexandre Teste","cpf":"11144477735","email":"teste@teste.com","status":"ACTIVE"}' \
  "$API_URL/customers")
echo "$CREATE_RESPONSE"
NEW_ID=$(echo "$CREATE_RESPONSE" | jq -r .id)
echo

echo "== 7) GET /customers/search?status=ACTIVE com token USER -> esperado 200 =="
curl -s -w "\nHTTP %{http_code}\n" -H "Authorization: Bearer $USER_TOKEN" \
  "$API_URL/customers/search?status=ACTIVE"
echo

echo "== 8) DELETE /customers/$NEW_ID com token USER -> esperado 403 =="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X DELETE \
  -H "Authorization: Bearer $USER_TOKEN" "$API_URL/customers/$NEW_ID"
echo

echo "== 9) DELETE /customers/$NEW_ID com token ADMIN -> esperado 204 (limpa o registro de teste) =="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X DELETE \
  -H "Authorization: Bearer $ADMIN_TOKEN" "$API_URL/customers/$NEW_ID"
echo
