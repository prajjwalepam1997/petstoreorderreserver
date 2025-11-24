FROM mcr.microsoft.com/azure-functions/java:4-java17
COPY target/azure-functions/OrderItemsReserver/ /home/site/wwwroot