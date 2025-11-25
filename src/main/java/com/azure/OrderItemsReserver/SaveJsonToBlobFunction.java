package com.azure.OrderItemsReserver;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class SaveJsonToBlobFunction {

    private static final String CONNECTION_STRING = System.getenv("AzureWebJobsStorage");
    private static final String CONTAINER_NAME = "orderitemreserver";

    @FunctionName("SaveJsonToBlob")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            // Get JSON body
            String jsonBody = request.getBody().orElse("");
            if (jsonBody.isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Request body is empty")
                        .build();
            }

            // Parse JSON to get the id field
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonBody);

            if (!jsonNode.has("id")) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Missing 'id' field in JSON")
                        .build();
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

            // Get container client and create if doesn't exist
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
            if (!containerClient.exists()) {
                containerClient.create();
                context.getLogger().info("Created container: " + CONTAINER_NAME);
            }

            // Save JSON as a blob named {id}.json
            BlockBlobClient blobClient = containerClient.getBlobClient(id + ".json").getBlockBlobClient();
            blobClient.upload(BinaryData.fromString(jsonBody), true);

            context.getLogger().info("Successfully saved blob: " + id + ".json");

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("File saved as " + id + ".json")
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }
}