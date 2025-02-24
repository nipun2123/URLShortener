package com.app.urlshortener.LambdaHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UrlShortnerHandler implements RequestHandler<Map<String,Object>, Object>{

    private static final String TABLE_NAME = "url_shortener";
    private static final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private static final Gson gson = new Gson();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
            String httpMethod = (String) event.get("httpMethod");
            Map<String, Object> response = new HashMap<>();

            if ("POST".equalsIgnoreCase(httpMethod)) {
                Map<String, String> body = gson.fromJson((String) event.get("body"), Map.class);
                String originalUrl = body.get("url");
                String shortCode = generateShortCode();

                Map<String, AttributeValue> item = Map.of(
                        "short_id", AttributeValue.builder().s(shortCode).build(),
                        "long_url", AttributeValue.builder().s(originalUrl).build(),
                        "created_at", AttributeValue.builder().s(new Date().toString()).build()
                );

                dynamoDb.putItem(PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build());

                response.put("statusCode", 200);
                response.put("body", gson.toJson(Map.of("short_url", "https://your-api-id.execute-api.region.amazonaws.com/" + shortCode)));
            }
            return null;
    }

    private String generateShortCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder shortCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            shortCode.append(chars.charAt(random.nextInt(chars.length())));
        }
        return shortCode.toString();
    }
}
