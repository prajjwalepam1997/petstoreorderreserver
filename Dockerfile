# Build stage - using Maven to build the function
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage - using Azure Functions Java base image
FROM mcr.microsoft.com/azure-functions/java:4-java21-appservice

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true

# Copy the built Azure Functions artifacts
COPY --from=build /app/target/azure-functions/ /home/site/wwwroot/

# Verify the deployment
RUN ls -la /home/site/wwwroot/