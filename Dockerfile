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

# Copy the built original JAR file from the builder stage
COPY --from=builder /app/target/stremio-addon-0.0.2.jar /app/app.jar

# Copy .env file (if used for environment variables)
COPY .env /app/.env

# Expose the port the application will run on
EXPOSE 8080

# Set default environment variables (ensure these are available for Spring Boot)
ENV SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/stremio_db
ENV SPRING_DATASOURCE_USERNAME=stremio_user
ENV SPRING_DATASOURCE_PASSWORD=stremio_password
ENV ADDON_NAME="Addon Torrent"
ENV JACKett_API_KEY="nn6a4ekizom285jrsgs5koivj7wlyx9s"

# Optional: Expose additional ports or set more variables as needed

# Volume for config files
VOLUME /config

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
