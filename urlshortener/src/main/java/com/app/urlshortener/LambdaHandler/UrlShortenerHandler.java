package com.app.urlshortener.LambdaHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UrlShortenerHandler implements RequestHandler<Map<String,String>, Object> {

    private static final String TABLE_NAME = "shortify-app";
    private static final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private static final Gson gson = new Gson();

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, Context context) {

        context.getLogger().log("Key Set is : " + event.keySet());

//        String httpMethod = (String) event.get("httpMethod");
        Map<String, Object> response = new HashMap<>();

        if (event.containsKey("longUrl")) {
//            Map<String, String> body = gson.fromJson((String) event.get("body"), Map.class);
            context.getLogger().log("Original Url is : " + event.get("longUrl"));
            String originalUrl = event.get("longUrl");
            String shortCode = generateShortCode();

            context.getLogger().log("Short code is : " + shortCode);

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
            response.put("body", gson.toJson(Map.of("short_url", "https://aly8aastnd.execute-api.eu-west-2.amazonaws.com/dev/" + shortCode)));

        } else if (event.containsKey("shortCode")) {
            String shortCode = event.get("shortCode");

            context.getLogger().log("Short code is : " + shortCode);

            GetItemResponse item = dynamoDb.getItem(GetItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(Map.of("short_id", AttributeValue.builder().s(shortCode).build()))
                    .build());

            if (item.hasItem()) {
                context.getLogger().log("Long url is : " + item.item().get("long_url").s());
                response.put("statusCode", 302);
                response.put("headers", Map.of("Location", item.item().get("url").s()));
            } else {
                context.getLogger().log("Short URL not found" );
                response.put("statusCode", 404);
                response.put("body", gson.toJson(Map.of("error", "Short URL not found")));
            }
        } else {
            context.getLogger().log("Invalid request!" );
            response.put("statusCode", 400);
            response.put("body", gson.toJson(Map.of("error", "Invalid request")));
        }

        context.getLogger().log("Done!" );
        return response;


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
