FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM mcr.microsoft.com/azure-functions/java:4-java21-appservice

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true

# Copy from the subdirectory to the root
COPY --from=build /app/target/azure-functions/orderitemreserver/ /home/site/wwwroot/

# Verify the structure
RUN echo "=== Final wwwroot structure ===" && \
    ls -la /home/site/wwwroot/ && \
    echo "=== Checking host.json location ===" && \
    ls -la /home/site/wwwroot/host.json && \
    echo "=== Checking JAR files ===" && \
    ls -la /home/site/wwwroot/*.jar