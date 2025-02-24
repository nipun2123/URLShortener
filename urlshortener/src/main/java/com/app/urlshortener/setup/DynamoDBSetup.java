package com.app.urlshortener.setup;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class DynamoDBSetup {

    private static final String TABLE_NAME = "url_shortener";

    public static void main(String[] args) {
        DynamoDbClient dynamoDb = DynamoDbClient.create();

        // Create table request
        CreateTableRequest request = CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(KeySchemaElement.builder()
                        .attributeName("short_id")
                        .keyType(KeyType.HASH).build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("short_id")
                        .attributeType(ScalarAttributeType.S).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        dynamoDb.createTable(request);
        System.out.println("DynamoDB Table Created: " + TABLE_NAME);
    }
}
