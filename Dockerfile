# Step 1: Build the JAR
FROM maven:3.8.6-openjdk-17 as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Create final image
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/stremio-addon-0.0.2.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
