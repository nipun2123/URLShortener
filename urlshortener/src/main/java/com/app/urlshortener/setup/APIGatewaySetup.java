package com.app.urlshortener.setup;

import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiRequest;
import software.amazon.awssdk.services.apigateway.model.CreateRestApiResponse;

public class APIGatewaySetup {

    private static final String API_NAME = "UrlShortenerAPI";

    public static void main(String[] args) {
        ApiGatewayClient apiGateway = ApiGatewayClient.create();

        CreateRestApiRequest apiRequest = CreateRestApiRequest.builder()
                .name(API_NAME)
                .build();

        CreateRestApiResponse apiResponse = apiGateway.createRestApi(apiRequest);
        System.out.println("API Gateway Created: " + apiResponse.id());
    }
}
