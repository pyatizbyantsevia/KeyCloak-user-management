import groovy.json.JsonSlurper

def executeSh(command, errorMessage) {
    try {
        return sh(script: command, returnStdout: true).trim()
    } catch (Exception e) {
        error "${errorMessage} Ошибка при выполнении команды: ${e.message}"
    }
}

String getValueByKey(json, key) {
    
    def jsonResponse = new JsonSlurper().parseText(json)
    return jsonResponse[key]
}

return this
