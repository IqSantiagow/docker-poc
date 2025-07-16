#!groovy

pipeline {
    agent any
    tools{
        maven 'MAVEN_HOME'
        jdk 'JAVA_HOME'
    }
    stages {
        stage("Cleanup and checkout") {
            steps {
                script {
                    // Clean workspace
                    cleanWs()

                    // Checkout the code from the repository
                    checkout scm
                }
            }
        }
        stage("Build and Test") {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
        stage("Run selenium grid"){
            steps {
                // Start the Selenium Grid
                sh 'docker-compose up -d'
            }
        }
        stage("Run Tests") {
            steps {
                // Run the tests using Maven
                sh 'mvn test'
            }
        }
    }
    post{
        always{
            script{
                allure([
                        includeProperties: false,
                        jdk: '',
                        properties: [],
                        reportBuildPolicy: 'ALWAYS',
                        results: [[path: 'target/allure-results']]
                ])
            }
        }
    }
}