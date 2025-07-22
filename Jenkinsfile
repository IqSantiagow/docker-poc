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
                    echo "Starting Selenium Grid for build ${BUILD_NUMBER}"

                    // Start the Selenium Grid with unique network
                    sh """
                        export BUILD_NUMBER=${BUILD_NUMBER}
                        docker compose up -d
                    """

                    // Get the container IP address that's accessible from the host
                    def hubIp = sh(
                            script: "docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' selenium-hub-${BUILD_NUMBER}",
                            returnStdout: true
                    ).trim()

                    // Store the IP for use in the next stage
                    env.SELENIUM_HUB_IP = hubIp

                    echo "Selenium Hub IP: ${hubIp}"

                    // Pause for manual debugging
                    input(message: "Selenium Grid is up. Pause for debugging. Verify if it's working and resume once ready.")

                    // Optionally verify the grid status automatically
                    sh """
                        CONTAINER_ID=\$(docker ps --filter "name=selenium-hub-${BUILD_NUMBER}" --format "{{.ID}}")
                        echo "Found Selenium Grid Container: \$CONTAINER_ID"

                        # Curl request to check grid status
                        docker exec \$CONTAINER_ID curl http://localhost:4444/status || echo "Unable to fetch grid status"
                    """

                }
            }
        }
        stage("Run Tests") {
            steps {
                script {
                    // Verify connectivity from host to container
                    sh """
                        echo "Testing connection to Selenium hub at ${env.SELENIUM_HUB_IP}:4444..."
                        curl -f http://${env.SELENIUM_HUB_IP}:4444/wd/hub/status || echo "Connection test failed"
                    """

                    // Run the tests using Maven with localhost hub URL (slave has direct access)
                    sh """
                        mvn test -Dselenium.hub.url=http://${env.SELENIUM_HUB_IP}:4444/wd/hub
                    """
                }
            }
        }
    }
    post {
        always {
            script {
                // Clean up Docker containers and networks
                sh """
                    export BUILD_NUMBER=${BUILD_NUMBER}
                    docker compose down --remove-orphans || true
                    docker network prune -f || true
                """

                // Generate Allure reports
                allure([
                        includeProperties: false,
                        jdk              : '',
                        properties       : [],
                        reportBuildPolicy: 'ALWAYS',
                        results          : [[path: 'target/allure-results']]
                ])
            }
        }
    }
}