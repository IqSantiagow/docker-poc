#!/bin/bash
# Script to build the Jenkins slave image with integrated Selenium Grid

echo "Building Jenkins slave image..."
cd slave
docker build -t jenkins-slave:latest .

if [ $? -eq 0 ]; then
    echo "Jenkins slave image built successfully!"
    echo "Image: jenkins-slave:latest"
else
    echo "Failed to build Jenkins slave image"
    exit 1
fi