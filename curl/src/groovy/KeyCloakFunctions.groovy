String initToken(keyCloakUrl, username, password) {

    def fullUrl = "${keyCloakUrl}/auth/realms/master/protocol/openid-connect/token"
    def command = """
        curl -k -i --location "${fullUrl}" \
        -H 'Content-Type: application/x-www-form-urlencoded' \
        -H 'Cookie: KEYCLOAK_LOCALE=ru' \
        -d 'username=${username}' \
        -d 'password=${password}' \
        -d 'grant_type=password' \
        -d 'client_id=admin-cli'
        """

    def jsonResponse = Utils.executeSh(command, "Не удалось выполнить curl.")
    def codeAndBody = HTTPHandler.getCodeAndBody(jsonResponse)

    if (codeAndBody['code'] == "200") {
        return Utils.getValueByKey(codeAndBody['body'], "access_token")
    } else {
        error "Не удалось получить токен. ${codeAndBody['body']}"
    }
}

void createUser(keyCloakUrl, username, token) {

    def fullUrl = "${keyCloakUrl}/auth/realms/PlatformAuth/platform/user-scim-attributes/Users"
    def command = """
        curl -k -i --location "${fullUrl}" \
        -H 'Content-Type: application/json' \
        -H 'Authorization: Bearer ${token}' \
        --data-raw '{"firstName":"username","lastName":"temp", "email":"test@test.com", "enabled":"true", "username":"username"}'
        """
    def jsonResponse = Utils.executeSh(command, "Не удалось выполнить curl.")
    def codeAndBody = HTTPHandler.getCodeAndBody(jsonResponse)

    if (codeAndBody['code'] == "201") {
        echo "Успешно создан пользователь"
    } else if (codeAndBody['code'] == "409") {
        echo "${codeAndBody['body']}"
    } else {
        error "Не удалось создать пользователя. ${codeAndBody['body']}"
    }
}

return this
