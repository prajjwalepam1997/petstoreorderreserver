package com.azure.OrderItemsReserver;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;


public class SaveJsonToBlobFunction {

    private static final String CONNECTION_STRING = System.getenv("AzureWebJobsStorage");
    private static final String CONTAINER_NAME = "orderitemreserver";

    @FunctionName("SaveJsonToBlob")
    public void run(
            @ServiceBusQueueTrigger(
                    name = "message",
                    queueName = "order",
                    connection = "ServiceBusConnection"
            ) String message,
            final ExecutionContext context) {

        context.getLogger().info("Service Bus Queue trigger processed a message.");

        try {
            // Get JSON from queue message
            String jsonBody = message;
            if (jsonBody == null || jsonBody.isEmpty()) {
                context.getLogger().warning("Queue message is empty");
                return;
            }

            // Parse JSON to get the id field
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonBody);

            if (!jsonNode.has("id")) {
                context.getLogger().warning("Missing 'id' field in JSON");
                return;
            }

            String id = jsonNode.get("id").asText();

            // Validate connection string
            if (CONNECTION_STRING == null || CONNECTION_STRING.isEmpty()) {
                throw new RuntimeException("AzureWebJobsStorage connection string is not configured");
            }

            // Connect to Azure Blob Storage
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(CONNECTION_STRING)
                    .buildClient();

            // Get container client and create if it doesn't exist
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            if (!containerClient.exists()) {
                containerClient.create();
                context.getLogger().info("Created container: " + CONTAINER_NAME);
            }

            // Save JSON as a blob named {id}.json
            BlockBlobClient blobClient = containerClient.getBlobClient(id + ".json").getBlockBlobClient();
            blobClient.upload(BinaryData.fromString(jsonBody), true);

            context.getLogger().info("Successfully saved blob: " + id + ".json");

        } catch (Exception e) {
            context.getLogger().severe("Error processing message: " + e.getMessage());
            throw new RuntimeException("Failed to process queue message", e);
        }
    }
}
