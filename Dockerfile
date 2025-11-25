# Use the Azure Functions base image
FROM mcr.microsoft.com/azure-functions/java:4-java17-appservice

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true

# Copy the function app
COPY target/azure-functions/petstoreorderreserver-*/ /home/site/wwwroot/

# Install any additional dependencies if needed
RUN cd /home/site/wwwroot