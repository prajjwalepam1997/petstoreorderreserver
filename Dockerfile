# Stage 1: Build the Java function app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package

# Stage 2: Copy the built function app to the Azure Functions base image
FROM mcr.microsoft.com/azure-functions/java:4-java17
COPY --from=build /app/target/OrderItemsReserver /home/site/wwwroot/