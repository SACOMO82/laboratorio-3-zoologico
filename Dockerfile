FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/zoo-fantastic-app.jar zoo-fantastic-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "zoo-fantastic-app.jar"]