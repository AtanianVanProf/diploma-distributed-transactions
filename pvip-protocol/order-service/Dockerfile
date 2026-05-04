FROM gradle:8.13-jdk21 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8111
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
