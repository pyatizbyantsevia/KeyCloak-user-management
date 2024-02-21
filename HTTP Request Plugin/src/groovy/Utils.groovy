import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import net.sf.json.JSONObject

/**
 * Извлекает значение из JSON-строки по заданному ключу.
 * 
 * @param json JSON-строка, из которой нужно извлечь Value.
 * @param key Key, по которому нужно извлечь Value.
 * @return Value, извлеченное из JSON-строки по Key.
 */
String getValueByKey(String json, String key) {
    
    def jsonResponse = new JsonSlurper().parseText(json)

    return jsonResponse[key]
}

/**
 * Генерирует JSON-строку с случайными данными пользователя для использования при создании нового пользователя.
 * 
 * @param username Имя пользователя.
 * @param password Пароль пользователя.
 * @return JSON-строка с случайными данными пользователя.
 */
String generateRandomUserData(String username, String password) {

    def randomNumber = new Random().nextInt(900000) + 100000
    def body = [
        "username":"${username}",
        "credentials": [
            ["type":"password",
            "value":"${password}"]
        ],
        "enabled":"true",
        "firstName":"${username}",
        "middleName":"${username}",
        "lastName":"${username}",
        "email":"${username}@email.com",
        "externalEmail":"${username}@email.com",
        "internalPhone":"${randomNumber}",
        "mobilePhone":"${randomNumber}",
        "title":"${randomNumber}",
        "attributes": [
            "SUDIR-GUID": ["${username}-${randomNumber}"],
            "deptName":["${username}"],
            "employeeNumber":["${username}-${randomNumber}"],
            "patronymic":["${username}"],
            "orgCode":["${username}"],
            "divCodes":["${username}"],
            "name":["${username}"],
            "loginSigma":["${username}"],
            "position":["${username}"],
            "userType":["commonUser"],
            "deptCode":["${username}"],
            "fos": ["${randomNumber}"]
        ]
    ]
    return JsonOutput.toJson(body)
}

/**
 * Проверяет, существует ли пользователь на сервере Keycloak по заданному имени пользователя.
 * 
 * @param currentStand JSON-объект с информацией о текущей среде Keycloak.
 * @param username Имя проверяемого пользователя.
 * @param token Токен доступа, необходимый для аутентификации запроса к серверу Keycloak.
 * @return true, если пользователь существует, и false в противном случае.
 */
Boolean isUserExisted(JSONObject currentStand, String username, String token) {

    return !(KeyCloakFunctions.getUserByUsername(currentStand, username, token).isEmpty())
}

/**
 * Получает список существующих ролей из списка всех ролей клиента {some_client} с сервера Keycloak.
 * 
 * @param currentStand JSON-объект с информацией о текущей среде Keycloak.
 * @param token Токен доступа, необходимый для аутентификации запроса к серверу Keycloak.
 * @return Список ролей, которые существуют.
 */
List<String> getExistedRolesFromAllRoles(JSONObject currentStand, String token) {
    List<String> rolesToAssignee = []
    def allExistedRoles = KeyCloakFunctions.getAllClientRoles(currentStand, token)

    params.ROLES.tokenize(" \n").each { role ->
        if (Utils.getRoleIdByRolename(allExistedRoles, role) == null) {
            Utils.log("Роль ${role} не найдена", LogLevel.levels.WARNING)
            currentBuild.result = 'UNSTABLE'
        } else {
            rolesToAssignee.add(role)
        }
    }
    return rolesToAssignee
}

/**
 * Получает ID роли по ее имени из списка ролей на сервере Keycloak.
 * 
 * @param existedRoles JSON-строка со списком ролей на сервере Keycloak.
 * @param rolename Имя роли, для которой нужно получить ID.
 * @return ID роли или null, если роль не найдена.
 */
def getRoleIdByRolename(existedRoles, String rolename) {

    def jsonList = new groovy.json.JsonSlurper().parseText(existedRoles)
    return jsonList.find { it.name == rolename }?.id
}

/**
 * Выводит сообщение в консоль pipeline с указанным уровнем логирования. ANSI-последовательность окрашивает соотвествующий уровень соответствующим цветом.
 * 
 * @param message Сообщение для вывода в консоль.
 * @param level Уровень логирования (INFO, ERROR, WARNING).
 */
def log(String message, level) {
    switch (level) {
        case LogLevel.levels.INFO:
            echo "\033[0;32m\033[1m[INFO]" + message + "\033[0m"
            break
        case LogLevel.levels.ERROR:
            echo "\033[0;31m\033[1m[ERROR]" + message + "\033[0m"
            break
        case LogLevel.levels.WARNING:
            echo "\033[0;33m\033[1m[WARNING]" + message + "\033[0m"
            break
    }
}

return this
