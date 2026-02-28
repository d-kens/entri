# ---------- Stage 1: Build ----------
FROM gradle:8.14-jdk21 AS build
WORKDIR /app
# Copy only dependency files first for caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon
# Copy source and build
COPY src ./src
RUN gradle clean bootJar --no-daemon

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy only the boot jar
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]