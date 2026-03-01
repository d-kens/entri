# ---------- Stage 1: Build ----------
FROM gradle:8.14-jdk21 AS build
WORKDIR /app

# Copy only Gradle config files first for caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies (cached if unchanged)
RUN gradle dependencies --no-daemon

# Copy source and build jar
COPY src ./src
RUN gradle clean bootJar --no-daemon

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy only the generated boot jar
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port Render will use
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]