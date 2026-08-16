#!/usr/bin/env bash
#
# Exemplo de teste via curl do fluxo OAuth2 + JWT do datum-srv-auth /
# datum-srv-clientes, como alternativa ao Postman.
#
# Pré-requisitos:
#   - MariaDB rodando (mesma base usada pelos dois serviços)
#   - RabbitMQ rodando, com plugin de management ativo (porta 15672)
#   - datum-srv-auth no ar (porta 9000)
#   - datum-srv-clientes no ar (porta 8080)
#   - datum-srv-score-cliente no ar (porta 8090)
#   - jq instalado (brew install jq)
#
# Uso: ./scripts/test-api.sh

set -e

AUTH_URL="http://localhost:9000"
API_URL="http://localhost:8080"
CLIENT_ID="postman-client"
CLIENT_SECRET="postman-secret"
RABBITMQ_MGMT_URL="http://localhost:15672"
RABBITMQ_USER="guest"
RABBITMQ_PASSWORD="guest"
RABBITMQ_QUEUE="process-datum001"
RABBITMQ_STATUS_CHANGE_QUEUE="customer_status_changed"

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

echo "== 6.1) Evento CUSTOMER_CREATED publicado na fila $RABBITMQ_QUEUE (RabbitMQ) =="
curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASSWORD" -X POST \
  "$RABBITMQ_MGMT_URL/api/queues/%2F/$RABBITMQ_QUEUE/get" \
  -H "Content-Type: application/json" \
  -d '{"count":5,"ackmode":"ack_requeue_true","encoding":"auto"}' \
  | jq -r '.[] | select(.payload | contains("\"customerId\":'"$NEW_ID"'")) | .payload'
echo

echo "== 7) GET /customers/search?status=ACTIVE com token USER -> esperado 200 =="
curl -s -w "\nHTTP %{http_code}\n" -H "Authorization: Bearer $USER_TOKEN" \
  "$API_URL/customers/search?status=ACTIVE"
echo

echo "== 7.1) Publicando CUSTOMER_STATUS_CHANGE (INACTIVE) para o cliente $NEW_ID na fila $RABBITMQ_STATUS_CHANGE_QUEUE =="
STATUS_CHANGE_PAYLOAD=$(jq -nc --arg eventId "$(python3 -c 'import uuid; print(uuid.uuid4())')" --argjson customerId "$NEW_ID" \
  '{eventId:$eventId, eventType:"CUSTOMER_STATUS_CHANGE", customerId:$customerId, status:"INACTIVE"}')
curl -s -u "$RABBITMQ_USER:$RABBITMQ_PASSWORD" -X POST \
  "$RABBITMQ_MGMT_URL/api/exchanges/%2F/amq.default/publish" \
  -H "Content-Type: application/json" \
  -d "$(jq -nc --arg rk "$RABBITMQ_STATUS_CHANGE_QUEUE" --arg payload "$STATUS_CHANGE_PAYLOAD" \
        '{properties:{}, routing_key:$rk, payload:$payload, payload_encoding:"string"}')"
echo
sleep 1

echo "== 7.2) Conferindo se o status do cliente $NEW_ID virou INACTIVE =="
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "$API_URL/customers/$NEW_ID"
echo

echo "== 7.3) GET /customers/\$id/score com token USER -> esperado 200 (chama datum-srv-score-cliente) =="
curl -s -w "\nHTTP %{http_code}\n" -H "Authorization: Bearer $USER_TOKEN" "$API_URL/customers/$NEW_ID/score"
echo

echo "== 8) DELETE /customers/$NEW_ID com token USER -> esperado 403 =="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X DELETE \
  -H "Authorization: Bearer $USER_TOKEN" "$API_URL/customers/$NEW_ID"
echo

echo "== 9) DELETE /customers/$NEW_ID com token ADMIN -> esperado 204 (limpa o registro de teste) =="
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X DELETE \
  -H "Authorization: Bearer $ADMIN_TOKEN" "$API_URL/customers/$NEW_ID"
echo
