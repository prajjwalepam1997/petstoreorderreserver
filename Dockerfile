# Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Debug: Check what was built
RUN echo "=== Build output ===" && \
    find /app/target -type f -name "*" | head -20

# Runtime stage
FROM mcr.microsoft.com/azure-functions/java:4-java21-appservice

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true

# Copy ALL contents from the azure-functions directory to root
COPY --from=build /app/target/azure-functions/ /home/site/wwwroot/

# If the above doesn't work, try this alternative:
# COPY --from=build /app/target/azure-functions/*/ /home/site/wwwroot/

# Verify the structure
RUN echo "=== Contents of wwwroot ===" && \
    ls -la /home/site/wwwroot/ && \
    echo "=== Checking for host.json ===" && \
    find /home/site/wwwroot -name "host.json" && \
    echo "=== Checking for function files ===" && \
    find /home/site/wwwroot -name "*.jar" | head -5