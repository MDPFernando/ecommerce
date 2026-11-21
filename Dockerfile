# Stage 1: Build the JAR
FROM gradle:8-jdk24 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx1024m -XX:MaxMetaspaceSize=512m"
RUN gradle build --no-daemon -x test

# Stage 2: Minimal runtime image
FROM eclipse-temurin:24-jre-alpine
RUN adduser -u 1000 -D user
WORKDIR /app
COPY --chown=user:user --from=build /home/gradle/src/build/libs/*.jar app.jar
COPY --chown=user:user --from=build /home/gradle/src/ecommerce_db*.db /app/
COPY --chown=user:user --from=build /home/gradle/src/product-images /app/product-images
RUN mkdir -p /app/review-images && chown -R user:user /app
USER user
ENV PORT=7860
EXPOSE 7860
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
