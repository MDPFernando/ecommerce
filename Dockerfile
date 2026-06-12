# Stage 1: Build the JAR
FROM gradle:8-jdk24 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test

# Stage 2: Minimal runtime image
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
COPY --from=build /home/gradle/src/ecommerce_db*.db /app/
EXPOSE 8081
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
