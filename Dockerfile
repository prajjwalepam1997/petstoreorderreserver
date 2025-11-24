# Build stage
FROM mcr.microsoft.com/java/jdk:17-zulu-ubuntu AS installer-env

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory
WORKDIR /src

# Copy project files
COPY pom.xml .
COPY src ./src/

# Build the Azure Function
RUN mvn clean package -DskipTests

# Runtime stage
FROM mcr.microsoft.com/azure-functions/java:4-java17

# Set environment variables
ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true \
    FUNCTIONS_WORKER_RUNTIME=java \
    FUNCTIONS_EXTENSION_VERSION=~4

# Copy the function app
COPY --from=installer-env /src/target/azure-functions/* /home/site/wwwroot/

# Set the working directory
WORKDIR /home/site/wwwroot

# Expose the port the app runs on
EXPOSE 80

# Start the function app
CMD ["java", "-jar", "/azure-functions-host/Microsoft.Azure.WebJobs.Script.WebHost.jar"]