FROM mcr.microsoft.com/azure-functions/java:4-java17
COPY --from=build /app/target/petstoreorderreserver /home/site/wwwroot/