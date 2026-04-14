FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src src

RUN ./gradlew clean test bootJar --no-daemon && \
    cp build/libs/*.jar app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]