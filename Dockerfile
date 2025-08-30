# Этап сборки
FROM eclipse-temurin:23-jdk-alpine AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradlew .
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew build -x test

# Финальный образ
FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
