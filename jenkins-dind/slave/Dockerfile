FROM maven:3.9-openjdk-21-slim

# Install Jenkins agent dependencies and Docker
RUN apt-get update && apt-get install -y \
    openssh-server \
    git \
    curl \
    ca-certificates \
    gnupg \
    lsb-release \
    && rm -rf /var/lib/apt/lists/*

# Add Docker's official GPG key and repository
RUN mkdir -p /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker CE and Docker Compose
RUN apt-get update && apt-get install -y \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-compose-plugin \
    && rm -rf /var/lib/apt/lists/*

# Create Jenkins user
RUN useradd -m -d /home/jenkins -s /bin/bash jenkins \
    && usermod -aG docker jenkins

# Set up SSH for Jenkins agent connection
RUN mkdir /var/run/sshd \
    && echo 'jenkins:jenkins' | chpasswd \
    && echo 'jenkins ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers

# Create SSH directory for Jenkins user
RUN mkdir -p /home/jenkins/.ssh \
    && chown -R jenkins:jenkins /home/jenkins/.ssh \
    && chmod 700 /home/jenkins/.ssh

# Copy Selenium Grid docker-compose configuration
COPY --chown=jenkins:jenkins test-environment/docker-compose.yml /home/jenkins/docker-compose.yaml

# Set working directory
WORKDIR /home/jenkins

# Expose SSH port
EXPOSE 22

# Start SSH daemon
CMD ["/usr/sbin/sshd", "-D"]