FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Instalar curl para health check
RUN apk add --no-cache curl

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
