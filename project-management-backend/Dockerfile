# Multi-stage build for React
FROM node:18 AS frontend-build
WORKDIR /app
COPY src/main/frontend/package*.json ./
RUN npm install
COPY src/main/frontend/ ./
RUN npm run build

# Multi-stage build for Spring Boot
FROM maven:3.9-openjdk-21 AS backend-build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
COPY --from=frontend-build /app/build ./src/main/resources/static
RUN mvn clean package -DskipTests

# Final stage
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]