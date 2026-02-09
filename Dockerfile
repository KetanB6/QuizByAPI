# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src
# Build the JAR file, skipping tests to save time and memory
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set the PORT dynamically for Render
ENV PORT 8080
EXPOSE 8080

# Run with memory limits optimized for 512MB RAM
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]