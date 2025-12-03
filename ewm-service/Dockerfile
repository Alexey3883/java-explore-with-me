FROM amazoncorretto:21-alpine
WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

VOLUME /tmp
EXPOSE 8080

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]