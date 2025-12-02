FROM amazoncorretto:21-alpine
WORKDIR /app

EXPOSE 8080

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]