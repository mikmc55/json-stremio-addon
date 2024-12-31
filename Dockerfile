FROM openjdk:17-jdk-alpine
VOLUME /config
ARG JAR_FILE=target/stremio-addon-0.0.2.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]