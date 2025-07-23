#!groovy

pipeline {
    agent any

    environment {
        SELENIUM_HUB_URL = "http://localhost:4444/wd/hub"
        NODE_COMPOSE_FILE = "docker-compose-node.yaml"
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

        stage("Check Grid Status") {
            steps {
                script {
                    checkGridStatus()
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
                    startChromeNode()
                }
            }
        }

        stage("Run Tests") {
            steps {
                script {
                    runTests()
                }
            }
        }
    }

    post {
        always {
            script {
                cleanupResources()
                generateAllureReports()
            }
        }
        failure {
            echo "Pipeline failed. Check the logs for details."
        }
    }
}

// Main workflow functions
def checkGridStatus() {
    echo "Checking Selenium Grid status..."
    
    def isGridAvailable = sh(
        script: 'curl -sf http://localhost:4444/wd/hub/status > /dev/null 2>&1 && echo "true" || echo "false"',
        returnStdout: true
    ).trim()
    
    if (isGridAvailable == "true") {
        echo "Selenium Grid is available and ready!"
        displayGridStatus()
    } else {
        error("Selenium Grid is not available at ${SELENIUM_HUB_URL}. Pipeline cancelled.")
    }
}

def startChromeNode() {
    echo "Starting Chrome node for build ${BUILD_NUMBER}"
    
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
    
    echo "Chrome node started successfully"
}

def runTests() {
    echo "Running tests against Selenium Hub at ${SELENIUM_HUB_URL}"
    sh "mvn test -Dselenium.hub.url=${SELENIUM_HUB_URL}"
}

def cleanupResources() {
    echo "Cleaning up Chrome node for build ${BUILD_NUMBER}"
    
    sh """
        export BUILD_NUMBER=${BUILD_NUMBER}
        docker compose -f ${NODE_COMPOSE_FILE} down || true
        
        # Ensure the specific container is removed
        docker rm -f chrome-node-${BUILD_NUMBER} 2>/dev/null || true
    """
}

def generateAllureReports() {
    allure([
        includeProperties: false,
        jdk: '',
        properties: [],
        reportBuildPolicy: 'ALWAYS',
        results: [[path: 'target/allure-results']]
    ])
}

// Helper functions
def displayGridStatus() {
    sh '''
        echo "Current Grid Status:"
        curl -s http://localhost:4444/status | python3 -m json.tool || true
    '''
}