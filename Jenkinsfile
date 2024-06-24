List<String> stands = [ "first" ]
def currentStand

String token
Boolean isUserExisted
List<String> rolesToAssignee = []

pipeline {
    agent any
    parameters {
        choice(
            name: 'STAND',
            choices: stands,
            description: 'Стенд'
        )
        validatingString(
            name: 'USERNAME',
            description: 'Пользователь',
            regex: '.+',
            failedValidationMessage: 'Имя пользователя должно быть непустым',
        )
        string(
            name: 'PASSWORD',
            description: 'Оставьте пустым, если не требуется создание пользователя'
        )
        text(
            name: 'ROLES',
            description: 'Назначаемые роли'
        )
        booleanParam(
            name: 'DEBUG', 
            description: 'Включить debug для отображения HTTP запросов'
        )
    }
    options {
        ansiColor('xterm')
        timestamps()
        disableConcurrentBuilds()
    }
    stages {
        stage('Инициализация пайплайна') {
            steps {
                script {
                    KeyCloakFunctions = load 'src/groovy/KeyCloakFunctions.groovy'
                    Utils = load 'src/groovy/Utils.groovy'
                    LogLevel = load 'src/groovy/LogLevel.groovy'
                    currentStand = readJSON(file: 'src/resources/stands.json')[params.STAND]
                }
            }
        }
        stage('Получение токена') {
            steps {
                script {
                    Utils.log("Получение токена", LogLevel.levels.INFO)

                    withCredentials([usernamePassword(credentialsId: "kcse-user-management-${params.STAND}",
                                        usernameVariable: 'ADMIN_USERNAME',
                                        passwordVariable: 'ADMIN_PASSWORD')]) {
                        token = KeyCloakFunctions.initToken(currentStand, ADMIN_USERNAME, ADMIN_PASSWORD)
                    }

                    Utils.log("Токен получен", LogLevel.levels.INFO)
                }
            }
        }
        stage('Проверка пользователя') {
            steps {
                script {
                    Utils.log("Проверка указанного пользователя", LogLevel.levels.INFO)

                    isUserExisted = Utils.isUserExisted(currentStand, params.USERNAME, token)

                    isUserExisted ? Utils.log("Пользователь " + params.USERNAME + " существует", LogLevel.levels.INFO) : Utils.log("Пользователь " + params.USERNAME + " будет создан", LogLevel.levels.INFO) 
                }
            }
        }
        stage('Создание пользователя') {
            when {
                expression { !isUserExisted ? (params.PASSWORD.isEmpty() ? error("Не заполнен параметр PASSWORD") : true) : false }
            }
            steps {
                script {
                    Utils.log("Создание пользователя", LogLevel.levels.INFO)

                    KeyCloakFunctions.createUser(currentStand, params.USERNAME, params.PASSWORD, token)

                    Utils.log("Пользователь успешно создан", LogLevel.levels.INFO)
                }
            }
        }
        stage('Проверка ролей') {
            steps {
                script {
                    Utils.log("Проверка указанных ролей", LogLevel.levels.INFO)
                    
                    rolesToAssignee = Utils.getExistedRolesFromAllRoles(currentStand, token)
                }
            }
        }
        stage('Назначение ролей') {
            when {
                expression { rolesToAssignee.isEmpty() ? Utils.log("Нет ролей для назначения", LogLevel.levels.INFO) : true }
            }
            steps {
                script {
                    Utils.log("Будут назначены роли: ${rolesToAssignee}", LogLevel.levels.INFO)

                    KeyCloakFunctions.mapRolesToUser(currentStand, params.USERNAME, rolesToAssignee, token)

                    Utils.log("Роли успешно назначены", LogLevel.levels.INFO)
                }
            }
        }
    }
}
