# syntax=docker/dockerfile:1

# ---- Build stage -----------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy only the files needed to resolve dependencies first, so Docker can
# cache this (slow) layer and skip it on rebuilds that only touch src/.
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Necessary for running on the windows
RUN dos2unix mvnw && chmod +x mvnw

# Now run the maven command
RUN ./mvnw -B dependency:go-offline

# Now copy the actual source and build the executable jar.
# Tests are skipped here on purpose - they run in CI / locally, not on every
# image build - so bringing up the image doesn't depend on a live database.
COPY src src
RUN ./mvnw -B clean package -DskipTests

# ---- Runtime stage ----------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# curl is used by the container HEALTHCHECK below
RUN apk add --no-cache curl \
    && addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring

EXPOSE 8080

# /api-docs is springdoc's OpenAPI JSON endpoint - it's permitAll in
# SecurityConfig and only comes up once the app context is fully ready,
# which makes it a good stand-in for a real health endpoint (no actuator
# dependency is on the classpath yet).
HEALTHCHECK --interval=10s --timeout=5s --start-period=45s --retries=10 \
    CMD curl -f http://localhost:8080/api-docs || exit 1

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]