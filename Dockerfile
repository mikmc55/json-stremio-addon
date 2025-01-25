# Step 1: Build the application JAR
FROM infotechsoft/maven:3.8.6-openjdk-17 AS builder
WORKDIR /app

# Copy project files into the container
COPY . .

# Build the application (skip tests to speed up build)
RUN mvn clean package -DskipTests

# Step 2: Create the runtime image
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/stremio-addon-0.0.2.jar /app/app.jar

# Copy .env file (if used for environment variables)
COPY .env /app/.env

# Expose the port the application will run on
EXPOSE 8080

# Set environment variables for database connection and other app configurations
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql-hy123-hank-hy.e.aivencloud.com:11613/defaultdb
ENV SPRING_DATASOURCE_USERNAME=avnadmin
ENV SPRING_DATASOURCE_PASSWORD=AVNS_NNVAF8kXbsWL1mORFUU
ENV ADDON_NAME="Addon Torrent"
ENV JACKett_API_KEY="nn6a4ekizom285jrsgs5koivj7wlyx9s"

# Optional: Expose additional ports or set more variables as needed

# Volume for config files (if any external config files are to be persisted)
VOLUME /config

# Run the application with the built JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
