#!groovy

pipeline {
    agent any
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
        stage("Run selenium grid") {
            steps {
                script {
                    // Start the Selenium Grid
                    sh 'docker compose up -d'

                    // Pause for manual debugging
                    input(message: "Selenium Grid is up. Pause for debugging. Verify if it's working and resume once ready.")

                    // Optionally verify the grid status automatically
                    sh '''
                        CONTAINER_ID=$(docker ps --filter "name=selenium" --format "{{.ID}}")
                        echo "Found Selenium Grid Container: $CONTAINER_ID"

                        # Curl request to check grid status
                        docker exec $CONTAINER_ID curl http://localhost:4444/status || echo "Unable to fetch grid status"
                    '''
                }
            }
        }
        stage("Run Tests") {
            steps {
                // Run the tests using Maven
                sh 'mvn test'
            }
        }
    }
    post {
        always {
            script {
                // Generate Allure reports
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