FROM maven:3.9.11-eclipse-temurin-21-alpine

# Jenkins agent environment variables
ENV AGENT_WORKDIR=/home/jenkins/agent
ENV JENKINS_AGENT_HOME=/home/jenkins
ENV JENKINS_AGENT_WORKDIR=${AGENT_WORKDIR}

# Install Jenkins agent dependencies and Docker
RUN apk update && apk add --no-cache \
    openssh-server \
    git \
    curl \
    ca-certificates \
    gnupg \
    sudo \
    bash \
    shadow \
    docker \
    docker-compose

# Create Jenkins user with proper UID/GID for Docker cloud compatibility
RUN addgroup -g 1000 jenkins \
    && adduser -u 1000 -G jenkins -h /home/jenkins -s /bin/bash -D jenkins \
    && addgroup jenkins docker

# Set up Jenkins agent directories
RUN mkdir -p ${AGENT_WORKDIR} \
    && chown -R jenkins:jenkins ${JENKINS_AGENT_HOME} \
    && chmod 755 ${AGENT_WORKDIR}

# Set up SSH for Jenkins agent connection
RUN mkdir /var/run/sshd \
    && echo 'jenkins:jenkins' | chpasswd \
    && echo 'jenkins ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers \
    && sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config \
    && sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config

# Create SSH directory for Jenkins user
RUN mkdir -p /home/jenkins/.ssh \
    && chown -R jenkins:jenkins /home/jenkins/.ssh \
    && chmod 700 /home/jenkins/.ssh

# Switch to Jenkins user for proper ownership
USER jenkins

# Set working directory to agent workdir
WORKDIR ${AGENT_WORKDIR}

# Switch back to root for startup
USER root

# Expose SSH port
EXPOSE 22

# Start SSH daemon
CMD ["/usr/sbin/sshd", "-D"]