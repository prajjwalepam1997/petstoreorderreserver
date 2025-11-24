package com.azure.OrderItemsReserver;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class SaveJsonToBlobFunction {
    // Set your connection string and container name here
    private static final String CONNECTION_STRING = System.getenv("AzureWebJobsStorage");
    private static final String CONTAINER_NAME = "orderitemreserver";

    @FunctionName("SaveJsonToBlob")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION
            ) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context
    ) {
        try {
            String jsonBody = request.getBody().orElse("");
            ObjectMapper mapper = new ObjectMapper();
            // Parse JSON to get the id field
            String id = mapper.readTree(jsonBody).get("id").asText();

            // Connect to Azure Blob Storage
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(CONNECTION_STRING)
                    .buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);

            // Save JSON as a blob named {id}.json
            BlockBlobClient blobClient = containerClient.getBlobClient(id + ".json").getBlockBlobClient();
            blobClient.upload(BinaryData.fromString(jsonBody), true);

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("File saved as " + id + ".json")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage())
                    .build();
        }
    }
}
