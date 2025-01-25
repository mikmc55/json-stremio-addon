# Step 1: Build the application JAR
FROM infotechsoft/maven:3.8.6-openjdk-17 AS builder
WORKDIR /app

# Copy project files into the container
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Step 2: Create the runtime image
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/stremio-addon-*.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# Set default environment variable
ENV CONFIG_PATH=/config/torrent_searchers.json

# Copy the JAR file to the runtime container (if needed)
ARG JAR_FILE=target/stremio-addon-0.0.2.jar
COPY ${JAR_FILE} app.jar

# Volume for config file
VOLUME /config

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]
