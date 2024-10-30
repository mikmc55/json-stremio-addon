FROM openjdk:17-jdk-alpine
VOLUME /config
ARG JAR_FILE=target/stremio-addon-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENV CONFIG_PATH=/config
ENTRYPOINT ["java","-jar","/app.jar"]