# Docker POC - Java Selenium Testing with Jenkins Dynamic Slaves

This project demonstrates a Java-based Docker POC for Selenium WebDriver testing with Jenkins dynamic slave architecture. Each pipeline execution gets its own isolated Selenium Grid environment for complete test isolation.

## Project Overview

A Maven-based Java project that uses:
- **TestNG** for test execution and parallel test management
- **Selenium WebDriver** for browser automation (Chrome/Firefox support)
- **Allure** for comprehensive test reporting
- **Docker** for containerized Selenium Grid environments
- **Jenkins** with dynamic slave nodes for CI/CD pipeline isolation

## Architecture

### Main Test Environment
The project includes a standalone Selenium Grid setup for local development:
- **Selenium Hub** on ports 4442, 4443, 4444
- **Chrome nodes** with optimized configuration
- **Network isolation** using BUILD_NUMBER-specific containers and networks

### Jenkins Dynamic Slave Architecture
For CI/CD, the project uses Jenkins with dynamic slave provisioning:

**Jenkins Master:**
- Runs with Docker plugin enabled
- Provisions dynamic slave containers for each pipeline run
- Handles job scheduling and result collection

**Jenkins Slaves:**
- Each slave is a Docker container with Maven, Docker, and Selenium Grid configuration
- Slaves spawn their own isolated Selenium Grid (hub + chrome nodes)
- Complete isolation between parallel pipeline runs

## Project Structure

```
docker-poc/
├── CLAUDE.md                    # Project guidance for Claude Code
├── Jenkinsfile                  # Main pipeline (uses dynamic slaves)
├── README.md                    # This file
├── docker-compose.yaml          # Standalone Selenium Grid for development
├── pom.xml                      # Maven configuration
├── configuration.properties     # Test configuration
├── src/
│   ├── main/java/
│   └── test/
│       ├── java/docker_test/
│       │   ├── BaseTest.java           # Base test class with WebDriver setup
│       │   └── PerformanceTest.java    # Test implementation
│       └── resources/
│           └── testng.xml              # TestNG suite configuration
├── jenkins-dind/                       # Jenkins dynamic slave setup
│   ├── Dockerfile                      # Jenkins master with plugins
│   ├── plugins.txt                     # Required Jenkins plugins
│   ├── docker-compose.yaml             # Jenkins master configuration  
│   ├── build-slave.sh                  # Script to build slave image
│   ├── README.md                       # Jenkins-specific documentation
│   └── slave/
│       ├── Dockerfile                  # Jenkins slave with Maven + Docker
│       └── test-environment/
│           └── docker-compose.yml      # Selenium Grid for slaves
├── allure-results/                     # Allure test results
└── target/                             # Maven build output
```

## Setup and Usage

### Local Development
1. **Start Selenium Grid:**
   ```bash
   docker compose up -d
   ```

2. **Run tests:**
   ```bash
   mvn test
   ```

3. **Generate Allure reports:**
   ```bash
   mvn allure:serve
   ```

### Jenkins CI/CD Setup
1. **Build Jenkins slave image:**
   ```bash
   cd jenkins-dind
   ./build-slave.sh
   ```

2. **Start Jenkins master:**
   ```bash
   cd jenkins-dind
   docker-compose up -d
   ```

3. **Access Jenkins:**
   - Open http://localhost
   - Configure with initial setup
   - Pipelines automatically use dynamic slaves

## Key Features

### Test Configuration
- **BaseTest** handles WebDriver initialization with automatic local/remote switching
- **Headless browser** support for container environments
- **Parameterized tests** using TestNG DataProvider
- **Allure integration** for step-by-step reporting

### Network Isolation
- **Unique networks** per build: `selenium-network-${BUILD_NUMBER}`
- **Container isolation** with BUILD_NUMBER suffixes
- **Port standardization** (4442, 4443, 4444) within isolated networks

### Pipeline Benefits
- **Complete Isolation:** Each pipeline gets its own Grid environment
- **Parallel Execution:** Multiple pipelines run simultaneously without conflicts
- **Resource Efficiency:** Slaves created on-demand and cleaned up automatically
- **Scalability:** Easy horizontal scaling with additional slave capacity

## Key Commands

```bash
# Development
mvn clean install -DskipTests    # Build without tests
mvn test                         # Run all tests
docker compose up -d             # Start Selenium Grid

# Jenkins Setup
cd jenkins-dind && ./build-slave.sh     # Build slave image
docker-compose up -d                     # Start Jenkins master

# Monitoring
docker ps --filter "name=selenium"      # Check running containers
docker logs selenium-hub-${BUILD_NUMBER} # Check hub logs
```

## Configuration

The project supports both local and containerized execution:
- **Local mode:** Direct WebDriver execution
- **Docker mode:** Remote WebDriver via Selenium Grid
- **Automatic switching** based on environment detection
- **Configurable hub URL** via `selenium.hub.url` system property