#!groovy

pipeline {
    agent any

    environment {
        SELENIUM_HUB_URL = "http://localhost:4444/wd/hub"
        NODE_COMPOSE_FILE = "docker-compose-node.yaml"
        HUB_COMPOSE_FILE = "docker-compose-grid.yaml"
        MAX_HUB_START_ATTEMPTS = "3"
        MAX_HUB_HEALTH_CHECKS = "30"
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

        stage("Ensure Selenium Hub is Running") {
            steps {
                script {
                    echo "Checking Selenium Hub status..."

                    def hubStarted = false
                    def attempts = 0
                    def maxAttempts = env.MAX_HUB_START_ATTEMPTS.toInteger()

                    while (!hubStarted && attempts < maxAttempts) {
                        attempts++
                        echo "Attempt ${attempts}/${maxAttempts} to ensure hub is running"

                        // Check if hub container exists and is running
                        def hubStatus = sh(
                                script: '''
                                if docker ps --filter name=selenium-hub-persistent --format "{{.Status}}" | grep -q "Up"; then
                                    echo "running"
                                elif docker ps -a --filter name=selenium-hub-persistent --format "{{.Status}}" | grep -q "Exited"; then
                                    echo "exited"
                                else
                                    echo "not_found"
                                fi
                            ''',
                                returnStdout: true
                        ).trim()

                        echo "Hub container status: ${hubStatus}"

                        if (hubStatus == "running") {
                            // Check if hub is healthy
                            def isHealthy = sh(
                                    script: 'curl -sf http://localhost:4444/wd/hub/status > /dev/null 2>&1 && echo "true" || echo "false"',
                                    returnStdout: true
                            ).trim()

                            if (isHealthy == "true") {
                                echo "Selenium Hub is running and healthy!"
                                hubStarted = true
                            } else {
                                echo "Hub is running but not healthy. Restarting..."
                                sh 'docker restart selenium-hub-persistent'
                                waitForHub()
                                hubStarted = checkHubHealth()
                            }
                        } else if (hubStatus == "exited") {
                            echo "Hub container exists but is stopped. Starting it..."
                            sh 'docker start selenium-hub-persistent'
                            waitForHub()
                            hubStarted = checkHubHealth()
                        } else {
                            echo "Hub container not found. Creating and starting it..."

                            // Ensure network exists
                            sh '''
                                docker network ls | grep -q selenium-grid-network || \
                                docker network create selenium-grid-network
                            '''

                            // Start the hub
                            def startResult = sh(
                                    script: "docker compose -f ${HUB_COMPOSE_FILE} up -d",
                                    returnStatus: true
                            )

                            if (startResult == 0) {
                                waitForHub()
                                hubStarted = checkHubHealth()
                            } else {
                                echo "Failed to start Selenium Hub"
                            }
                        }

                        if (!hubStarted && attempts < maxAttempts) {
                            echo "Hub not ready, waiting 10 seconds before retry..."
                            sleep(10)
                        }
                    }

                    if (!hubStarted) {
                        error("Failed to start Selenium Hub after ${maxAttempts} attempts. Stopping pipeline.")
                    }

                    // Display hub info
                    sh '''
                        echo "Hub container info:"
                        docker ps --filter name=selenium-hub-persistent
                        echo "\nHub network info:"
                        docker network inspect selenium-grid-network --format '{{json .Containers}}' | python3 -m json.tool || true
                    '''
                }
            }
        }

        stage("Build and Test") {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage("Start Chrome Node") {
            steps {
                script {
                    echo "Starting Chrome node for build ${BUILD_NUMBER}"

                    // Ensure the network exists
                    sh '''
                        docker network ls | grep -q selenium-grid-network || \
                        (echo "ERROR: Selenium network not found!" && exit 1)
                    '''

                    // Start the Chrome node
                    def nodeStarted = sh(
                            script: """
                            export BUILD_NUMBER=${BUILD_NUMBER}
                            docker compose -f ${NODE_COMPOSE_FILE} up -d
                        """,
                            returnStatus: true
                    )

                    if (nodeStarted != 0) {
                        error("Failed to start Chrome node. Stopping pipeline.")
                    }

                    // Wait for node to register with hub
                    def nodeRegistered = false
                    def checks = 0
                    def maxChecks = 20

                    while (!nodeRegistered && checks < maxChecks) {
                        checks++
                        sleep(2)

                        def nodeCount = sh(
                                script: '''
                                curl -s http://localhost:4444/status 2>/dev/null | \
                                python3 -c "import sys, json; data=json.load(sys.stdin); print(len(data.get('value', {}).get('nodes', [])))" 2>/dev/null || echo "0"
                            ''',
                                returnStdout: true
                        ).trim()

                        if (nodeCount.toInteger() > 0) {
                            echo "Chrome node registered successfully! Total nodes: ${nodeCount}"
                            nodeRegistered = true
                        } else {
                            echo "Waiting for node registration... attempt ${checks}/${maxChecks}"
                        }
                    }

                    if (!nodeRegistered) {
                        // Get node logs for debugging
                        sh "docker logs chrome-node-${BUILD_NUMBER} || true"
                        error("Chrome node failed to register with hub after ${maxChecks} attempts. Stopping pipeline.")
                    }

                    // Log current grid status
                    sh '''
                        echo "Current Grid Status:"
                        curl -s http://localhost:4444/status | python3 -m json.tool || true
                    '''
                }
            }
        }

        stage("Run Tests") {
            steps {
                script {
                    echo "Running tests against Selenium Hub at ${SELENIUM_HUB_URL}"

                    sh "mvn test -Dselenium.hub.url=${SELENIUM_HUB_URL}"
                }
            }
        }
    }

    post {
        always {
            script {
                echo "Cleaning up Chrome node for build ${BUILD_NUMBER}"

                // Stop and remove the Chrome node
                sh """
                    export BUILD_NUMBER=${BUILD_NUMBER}
                    docker compose -f ${NODE_COMPOSE_FILE} down --remove-orphans || true
                    
                    # Ensure the specific container is removed
                    docker rm -f chrome-node-${BUILD_NUMBER} 2>/dev/null || true
                """

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
        failure {
            echo "Pipeline failed. Check the logs for details."
        }
    }
}

// Helper function to wait for hub to be ready
def waitForHub() {
    echo "Waiting for Selenium Hub to start..."
    sleep(5)  // Initial wait for container to start
}

// Helper function to check hub health
def checkHubHealth() {
    echo "Checking hub health..."
    def maxChecks = env.MAX_HUB_HEALTH_CHECKS.toInteger()
    def checks = 0

    while (checks < maxChecks) {
        checks++
        def isHealthy = sh(
                script: 'curl -sf http://localhost:4444/wd/hub/status > /dev/null 2>&1 && echo "true" || echo "false"',
                returnStdout: true
        ).trim()

        if (isHealthy == "true") {
            echo "Hub is healthy!"
            return true
        }

        echo "Hub health check ${checks}/${maxChecks}..."
        sleep(2)
    }

    echo "Hub failed health checks after ${maxChecks} attempts"
    return false
}