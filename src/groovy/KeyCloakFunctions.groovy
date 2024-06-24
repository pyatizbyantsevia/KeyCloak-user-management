import net.sf.json.JSONObject
import groovy.json.JsonOutput

/**
 * Получение токена доступа клиента admin-cli от сервера Keycloak с использованием авторизации по имени пользователя и паролю.
 *
 * @param currentStand JSON-объект с информацией о текущей среде Keycloak.
 * @param username Имя пользователя для аутентификации. Пользователь находится в master realm.
 * @param password Пароль пользователя для аутентификации.
 * @return access token, полученный от сервера Keycloak. Используется для последующих запросов.
 */
String initToken(JSONObject currentStand, String username, String password) {

    def fullUrl = "${currentStand.keycloak.url}/realms/master/protocol/openid-connect/token"

    def body = [
        "grant_type":"password",
        "username":"${username}",
        "password":"${password}",
        "client_id":"admin-cli"
    ].collect { k, v -> "$k=$v" }.join('&')

    def httpResponse = httpRequest(
                        customHeaders: [[name: 'Content-Type', value: 'application/x-www-form-urlencoded'],
                                        [name: 'Cookie', value: 'KEYCLOAK_LOCALE=ru']],
                        requestBody: body,
                        ignoreSslErrors: true,
                        httpMode: 'POST',
                        validResponseCodes: '101:504',
                        quiet: !params.DEBUG,
                        url: fullUrl
                      )

    if (httpResponse.status != 200) {
        Utils.log("Не удалось получить токен", LogLevel.levels.ERROR)
        error "${httpResponse.content}"
    }

    return Utils.getValueByKey(httpResponse.content, "access_token")
}

/**
 * Получение информации о пользователе от сервера Keycloak по его username.
 * 
 * @param currentStand JSON-объект с информацией о текущей среде Keycloak.
 * @param username Имя пользователя, информацию о котором необходимо получить.
 * @param token Токен доступа, необходимый для аутентификации запроса к серверу Keycloak.
 * @return JSON-строка, содержащая информацию о пользователе.
 */
String getUserByUsername(JSONObject currentStand, String username, String token) {

    def fullUrl = "${currentStand.keycloak.url}/admin/realms/master/users"

    def body = [
        "exact":"true",
        "username":"${username}"
    ].collect { k, v -> "$k=$v" }.join('&')
    fullUrl += "?${body}"

    def httpResponse = httpRequest(
                        customHeaders: [[name: 'Content-Type', value: 'application/json'],
                                        [name: 'Authorization', value: "Bearer ${token}"]],
                        ignoreSslErrors: true,
                        httpMode: 'GET',
                        validResponseCodes: '101:504',
                        quiet: !params.DEBUG,
                        url: fullUrl
                      )

    if (httpResponse.status != 200) {
        Utils.log("Не удалось получить пользователя", LogLevel.levels.ERROR)
        error "${httpResponse.content}"
    }

    // Возвращаемое значение объясняется тем, что endpoint users отдает массив пользователей
    // Возвращаемый массив состоит из одного элемента, так как запрос ищет точное совпадение по username, и username уникален
    // Поле httpResponse.content является типом String (https://github.com/jenkinsci/http-request-plugin/blob/eb71312ad6a42d084e985e48542749c0cbc7cbc5/src/main/java/jenkins/plugins/http_request/ResponseContentSupplier.java#L42)
    // Поэтому отсекаются первый символ '[' и последний ']'

    return httpResponse.content.substring(1, httpResponse.content.length() - 1)
}

/**
 * Создание нового пользователя на сервере Keycloak.
 * 
 * @param currentStand JSON-объект с информацией о текущей среде Keycloak.
 * @param username Имя пользователя, который будет создан.
 * @param password Пароль для нового пользователя.
 * @param token Токен доступа, необходимый для аутентификации запроса к серверу Keycloak.
 */
void createUser(JSONObject currentStand, String username, String password, String token) {

    def fullUrl = "${currentStand.keycloak.url}/realms/master/platform/user-scim-attributes/Users"

    def httpResponse = httpRequest(
                        customHeaders: [[name: 'Content-Type', value: 'application/json'],
                                        [name: 'Authorization', value: "Bearer ${token}"]],
                        requestBody: Utils.generateRandomUserData(username, password),
                        ignoreSslErrors: true,
                        httpMode: 'POST',
                        validResponseCodes: '101:504',
                        quiet: !params.DEBUG,
                        url: fullUrl
                      )

    if (httpResponse.status != 201) {
        Utils.log("Не удалось создать пользователя", LogLevel.levels.ERROR)
        error "${httpResponse.content}"
    }
}

/**
 * Получает список всех ролей клиента {some_client} с сервера Keycloak.
 * 
 * @param currentStand JSON-объект с информацией о текущей среде Keycloak.
 * @param token Токен доступа, необходимый для аутентификации запроса к серверу Keycloak.
 * @return JSON-строка, содержащая список ролей клиента PlatformAuth-Proxy.
 */
String getAllClientRoles(JSONObject currentStand, String token) {

    def fullUrl = "${currentStand.keycloak.url}/admin/realms/master/clients/${currentStand.keycloak.client_id у которого брать назначаемые роли}/roles"

    def httpResponse = httpRequest(
                        customHeaders: [[name: 'Content-Type', value: 'application/json'],
                                        [name: 'Authorization', value: "Bearer ${token}"]],
                        ignoreSslErrors: true,
                        httpMode: 'GET',
                        validResponseCodes: '101:504',
                        quiet: !params.DEBUG,
                        url: fullUrl
                      )

    if (httpResponse.status != 200) {
        Utils.log("Не удалось получить роли клиента {some_client}", LogLevel.levels.ERROR)
        error "${httpResponse.content}"
    }

    return httpResponse.content
}

/**
 * Wrap-function для функции mapRoleToUser().
 * 
 * @param currentStand JSON-объект с информацией о текущей среде Keycloak.
 * @param username Имя пользователя, которому нужно присвоить роли.
 * @param roles Список ролей для присвоения пользователю.
 * @param token Токен доступа, необходимый для аутентификации запроса к серверу Keycloak.
 */
void mapRolesToUser(JSONObject currentStand, String username, List<String> roles, String token) {

    def userData = getUserByUsername(currentStand, username, token)
    def userId = Utils.getValueByKey(userData, "id")

    def allClientRoles = getAllClientRoles(currentStand, token)

    roles.each { role ->
        roleId = Utils.getRoleIdByRolename(allClientRoles, role)
        mapRoleToUser(currentStand, userId, roleId, role, token)
    }

}

/**
 * Присваивает роль пользователю на сервере Keycloak.
 * 
 * @param currentStand JSON-объект с информацией о текущей среде Keycloak.
 * @param userId ID пользователя, которому нужно присвоить роль.
 * @param roleId ID роли, которую нужно присвоить пользователю.
 * @param rolename Имя роли, которую нужно присвоить пользователю.
 * @param token Токен доступа, необходимый для аутентификации запроса к серверу Keycloak.
 */
void mapRoleToUser(JSONObject currentStand, String userId, String roleId, String rolename, String token) {

    def fullUrl = "${currentStand.keycloak.url}/realms/master/platform/add-client-role/${userId}?client=${currentStand.keycloak.client_id у которого брать назначаемые роли}"

    def body = "{\"role\":[{\"id\":\"${roleId}\",\"name\":\"${rolename}\"}]}"

    def httpResponse = httpRequest(
                        customHeaders: [[name: 'Content-Type', value: 'application/json'],
                                        [name: 'Authorization', value: "Bearer ${token}"]],
                        requestBody: body,
                        ignoreSslErrors: true,
                        httpMode: 'POST',
                        validResponseCodes: '101:504',
                        quiet: !params.DEBUG,
                        url: fullUrl
                      )

    if (httpResponse.status != 204) {
        Utils.log("Не удалось назначить роль ${rolename} на пользователя", LogLevel.levels.ERROR)
        error "${httpResponse.content}"
    }

}

return this
