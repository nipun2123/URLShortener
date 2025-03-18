package com.app.urlshortener.LambdaHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class UrlShortenerHandler implements RequestHandler<Map<String,Object>, Object> {

    private static final String TABLE_NAME = System.getenv("DYNAMODB_TABLE");
    private static final String REDIS_ENDPOINT = "sho-sh-1d27hzrcbo9fl.mnfcle.0001.euw2.cache.amazonaws.com";
    private static final int REDIS_PORT = 6379;
    private static final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private static final Gson gson = new Gson();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {

        context.getLogger().log("Key Set is : " + event.keySet());
        context.getLogger().log("Method is : " + event.get("httpMethod"));
        context.getLogger().log("Body is : " + event.get("body"));

        // Create empty response
        Map<String, Object> response = new HashMap<>();
        Map<String, String> body = gson.fromJson((String) event.get("body"), Map.class);

        String httpMethod = (String) event.get("httpMethod");
        if (httpMethod.equalsIgnoreCase("post")) {
            if (body.containsKey("longUrl")) {
                context.getLogger().log("Original Url is : " + body.get("longUrl"));
                String originalUrl = body.get("longUrl");
                String shortCode = generateShortHash(originalUrl);

                context.getLogger().log("Short code is : " + shortCode);

                if(getItemResponse(shortCode).hasItem()){

                    shortCode = handleCollision(shortCode);
                    context.getLogger().log("Collision handled : " + shortCode);
                }

                Map<String, AttributeValue> item = Map.of(
                        "short_id", AttributeValue.builder().s(shortCode).build(),
                        "long_url", AttributeValue.builder().s(originalUrl).build(),
                        "created_at", AttributeValue.builder().s(new Date().toString()).build()
                );



                dynamoDb.putItem(PutItemRequest.builder()
                        .tableName(TABLE_NAME)
                        .item(item)
                        .build());

                context.getLogger().log("Short code saved : " + shortCode);

                response.put("statusCode", 201);
                response.put("body", gson.toJson(Map.of("short_url", shortCode)));

                context.getLogger().log("Done!");
                return response;
            }
            } else if (httpMethod.equalsIgnoreCase("get")) {

            context.getLogger().log("Path Parameters are : " + event.get("pathParameters"));
            Map<String, String> pathParameters = (Map<String, String>) event.get("pathParameters");
            context.getLogger().log("Path Parameters are : " + pathParameters);
                if (pathParameters.containsKey("shortCode")) {
                    String shortCode = pathParameters.get("shortCode");

                    context.getLogger().log("Short code is : " + shortCode);

                    // Initialize Redis client and connection
                    RedisClient redisClient = RedisClient.create("redis://" + REDIS_ENDPOINT + ":" + REDIS_PORT);
                    StatefulRedisConnection<String, String> connection = redisClient.connect();
                    RedisCommands<String, String> syncCommands = connection.sync();

                    // Try to get the original URL from Redis cache
                    String longUrl = syncCommands.get(shortCode);

                    context.getLogger().log("Long URL in cache : " +longUrl);
                    if (longUrl == null) {
                        context.getLogger().log("Long URL not found in Cache");

                        GetItemResponse item = getItemResponse(shortCode);
                        if (item.hasItem()) {
                            longUrl = item.item().get("long_url").s();
                            context.getLogger().log("LongUrl from DB : " +longUrl);
                            syncCommands.setex(shortCode, 3600, longUrl); // Store with 1-hour TTL
                            context.getLogger().log("LongUrl saved in the Redis!");

                        } else {
                            context.getLogger().log("Short URL not found  in the DB");
                            response.put("statusCode", 404);
                            response.put("body", gson.toJson(Map.of("error", "Short URL not found")));

                            context.getLogger().log("Done!");
                            return response;
                        }
                    }

                    // Close Redis connection
                    connection.close();
                    redisClient.shutdown();

                    context.getLogger().log("Long url is : " + longUrl);
                    response.put("statusCode", 302);
                    response.put("headers", Map.of("Location", longUrl));

                    context.getLogger().log("Done!");
                    return response;


                }
            }

        context.getLogger().log("Invalid request!");
        response.put("statusCode", 400);
        response.put("body", gson.toJson(Map.of("error", "Invalid request")));

        context.getLogger().log("Done!");
        return response;


        }

    private static GetItemResponse getItemResponse(String shortCode) {
        return dynamoDb.getItem(GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Map.of("short_id", AttributeValue.builder().s(shortCode).build()))
                .build());
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



    private static final String BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;

    // Generate SHA-256 Hash and Extract First 8 Bytes
    public static String generateShortHash(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(url.getBytes());

            // Extract first 8 bytes (16 hex characters)
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                hexString.append(String.format("%02x", hash[i]));
            }

            // Convert hex to Base62
            return encodeBase62(new BigInteger(hexString.toString(), 16));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 Algorithm not found", e);
        }
    }

    // Convert a BigInteger to Base62
    public static String encodeBase62(BigInteger num) {
        StringBuilder sb = new StringBuilder();
        while (num.compareTo(BigInteger.ZERO) > 0) {
            sb.append(BASE62_ALPHABET.charAt(num.mod(BigInteger.valueOf(BASE)).intValue()));
            num = num.divide(BigInteger.valueOf(BASE));
        }
        return sb.reverse().toString();
    }

    // Simulating Collision Handling by Adding a Random Character
    public static String handleCollision(String shortCode) {
        Random rand = new Random();
        return shortCode + BASE62_ALPHABET.charAt(rand.nextInt(BASE));
    }
}
