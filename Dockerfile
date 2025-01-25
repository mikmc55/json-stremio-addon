# Base image with Java 17 runtime
FROM openjdk:17-jdk-alpine

# Create and set working directory
WORKDIR /app

# Set up volume for configuration
VOLUME /config

# Copy the application JAR file into the container
ARG JAR_FILE=target/stremio-addon-0.0.2.jar
COPY ${JAR_FILE} app.jar

# Expose the application's port
EXPOSE 8080

# Set default environment variable for configuration file
ENV CONFIG_PATH=/config/torrent_searchers.json

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]
