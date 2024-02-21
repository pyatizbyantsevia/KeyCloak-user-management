# Полезные запросы:

## Получить токен

#### Через username+password

```bash
curl -i -k --location 'https://fqdn:port/auth/realms/master/protocol/openid-connect/token' \
-H 'Content-Type: application/x-www-form-urlencoded' \
-H 'Cookie: KEYCLOAK_LOCALE=ru' \
-d 'username=' \
-d 'password=' \
-d 'grant_type=password' \
-d 'client_id=admin-cli'
```

#### Через client_secret

```bash
curl -i -k --location 'https://fqdn:port/auth/realms/master/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode "grant_type=client_credentials" \
--data-urlencode "client_id=" \
--data-urlencode "client_secret="
```


## Получить user по username
```bash
curl -k -i --location --request GET 'https://fqdn:port/auth/admin/realms/{realm}/users?exact=true&username={username}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer '
```

## Получить все роли client по client_id

```bash
curl -k -i --location --request GET 'https://fqdn:port/auth/admin/realms/{realm}/clients/{client_id}/roles' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer '
```

## Создать user

```bash
curl -k -i -X 'POST' --location 'https://fqdn:port/auth/realms/{realm}/platform/user-scim-attributes/Users' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ' \
--data-raw '{"username":" ","credentials":[{"type":"password","value":" "}],"attributes":{"SUDIR-GUID":[" "]}}'
```

## Обновить user

```bash
curl -k -i -X 'PUT' --location 'https://fqdn:port/auth/realms/{realm}/platform/user-scim-attributes/{user_id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ' \
--data-raw '{"requiredActions":[]}'
```


## Получить role по id

```bash
curl -k -i -X 'GET' --location 'https://fqdn:port/auth/admin/realms/{realm}/roles-by-id/{role_id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer '
```

```bash
curl -k -i -X 'GET' --location 'https://fqdn:port/auth/admin/realms/{realm}/users/{user_id}/role-mappings/clients/{client_id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer '
```

## Назначить конкретную роль на пользователя

```bash
curl -k -i -X 'POST' --location 'https://fqdn:port/auth/realms/{realm}/platform/add-client-role/{user_id}?client={client_id}' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer ' \
--data-raw '{"role":[{"id":" ","name":" "}]}'
```
