package com.app.urlshortener.LambdaHandler;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class UrlShortenerService {

    private static final String TABLE_NAME = "url_shortener";
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    public String shortenUrl(String longUrl) {
        String shortId = UUID.randomUUID().toString().substring(0, 6);

        // Save to DynamoDB
        Map<String, AttributeValue> item = Map.of(
                "short_id", AttributeValue.builder().s(shortId).build(),
                "long_url", AttributeValue.builder().s(longUrl).build(),
                "created_at", AttributeValue.builder().s(new Date().toString()).build()
        );
        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
        dynamoDbClient.putItem(request);

        return "https://short.ly/" + shortId;
    }

}
