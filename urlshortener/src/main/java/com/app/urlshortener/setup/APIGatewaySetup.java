package com.app.urlshortener.setup;

import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.model.*;

import java.util.Map;

public class APIGatewaySetup {

    private static final String API_NAME = "ShortifyAppAPIDemo";

    public static void main(String[] args) {
        ApiGatewayClient apiClient = ApiGatewayClient.create();

        CreateRestApiResponse apiResponse = apiClient.createRestApi(CreateRestApiRequest.builder()
                .name(API_NAME)
                .description("API Gateway for Shortify App")
                .build());

        String apiId = apiResponse.id();
        System.out.println("API Gateway Created: " + apiId);

        // Create REST API
        GetResourcesResponse resourcesResponse = apiClient.getResources(GetResourcesRequest.builder()
                .restApiId(apiId)
                .build());

        String rootResourceId = resourcesResponse.items().get(0).id();
        System.out.println("Root Resource ID: " + rootResourceId);

        // Create /shortifyapp Resource
        CreateResourceResponse shortifyResourceResponse = apiClient.createResource(CreateResourceRequest.builder()
                .restApiId(apiId)
                .parentId(rootResourceId)
                .pathPart("shortifyapp")
                .build());

        String shortifyResourceId = shortifyResourceResponse.id();
        System.out.println("Created `/shortifyapp` Resource: " + shortifyResourceId);


        // Create /shortifyapp/handler Resource
        CreateResourceResponse handlerResourceResponse = apiClient.createResource(CreateResourceRequest.builder()
                .restApiId(apiId)
                .parentId(shortifyResourceId)
                .pathPart("handler")
                .build());

        String handlerResourceId = handlerResourceResponse.id();
        System.out.println("Created `/shortifyapp/handler` Resource: " + handlerResourceId);



        // Integrate /shortifyapp with S3
        PutMethodRequest putS3MethodRequest = PutMethodRequest.builder()
                .restApiId(apiId)
                .resourceId(shortifyResourceId)
                .httpMethod("GET")
                .authorizationType("NONE")
                .build();

        apiClient.putMethod(putS3MethodRequest);

        PutIntegrationRequest putS3IntegrationRequest = PutIntegrationRequest.builder()
                .restApiId(apiId)
                .resourceId(shortifyResourceId)
                .httpMethod("GET")
                .integrationHttpMethod("GET")
                .type(IntegrationType.AWS)
                .uri("arn:aws:apigateway:eu-west-2:s3:path/shortify-app/index.html") // Replace with your bucket
                .credentials("arn:aws:iam::203918856231:role/APIGatewayS3Role")
                .build();

        apiClient.putIntegration(putS3IntegrationRequest);
        System.out.println("Integrated `/shortifyapp` with S3.");



        // Define Method Response (Allow API Gateway to return 200)
        PutMethodResponseRequest putMethodResponseRequest = PutMethodResponseRequest.builder()
                .restApiId(apiId)
                .resourceId(shortifyResourceId)
                .httpMethod("GET")
                .statusCode("200")
                .responseParameters(Map.of("method.response.header.Content-Type", true)) // Allow Content-Type in response
                .build();

        apiClient.putMethodResponse(putMethodResponseRequest);

        // Define Integration Response (Map S3 Response to API Gateway Output)
        PutIntegrationResponseRequest putIntegrationResponseRequest = PutIntegrationResponseRequest.builder()
                .restApiId(apiId)
                .resourceId(shortifyResourceId)
                .httpMethod("GET")
                .statusCode("200")
                .responseParameters(Map.of(
                        "method.response.header.Content-Type", "integration.response.header.Content-Type"
                )) // Map S3 response headers
                .build();

        apiClient.putIntegrationResponse(putIntegrationResponseRequest);

//        String lambdaArn = "arn:aws:lambda:us-east-1:your-account-id:function:your-lambda-function"; // Replace
//
//        // Create GET Method for Lambda
//        PutMethodRequest getLambdaMethodRequest = PutMethodRequest.builder()
//                .restApiId(apiId)
//                .resourceId(handlerResourceId)
//                .httpMethod("GET")
//                .authorizationType("NONE")
//                .build();
//
//        apiClient.putMethod(getLambdaMethodRequest);
//
//        // Create POST Method for Lambda
//        PutMethodRequest postLambdaMethodRequest = PutMethodRequest.builder()
//                .restApiId(apiId)
//                .resourceId(handlerResourceId)
//                .httpMethod("POST")
//                .authorizationType("NONE")
//                .build();
//
//        apiClient.putMethod(postLambdaMethodRequest);
//
//
//        // Step 1: Set up Lambda Integration for GET with Path Parameter
//        PutIntegrationRequest putLambdaIntegrationRequest = PutIntegrationRequest.builder()
//                .restApiId(apiId)
//                .resourceId(shortifyResourceId)
//                .httpMethod("GET")
//                .integrationHttpMethod("POST")  // Lambda uses POST for invocation
//                .type(IntegrationType.AWS)
//                .uri("arn:aws:lambda:eu-west-2:your-account-id:function:your-lambda-function-name")
//                .credentials("arn:aws:iam::203918856231:role/APIGatewayLambdaRole") // API Gateway role
//                .build();
//
//        apiClient.putIntegration(putLambdaIntegrationRequest);
//
//    // Step 2: Define Method Response for GET
//        PutMethodResponseRequest putMethodResponseRequest = PutMethodResponseRequest.builder()
//                .restApiId(apiId)
//                .resourceId(shortifyResourceId)
//                .httpMethod("GET")
//                .statusCode("200")
//                .responseParameters(Map.of("method.response.header.Content-Type", true))
//                .build();
//
//        apiClient.putMethodResponse(putMethodResponseRequest);
//
//// Step 3: Define Integration Response for GET
//        PutIntegrationResponseRequest putIntegrationResponseRequest = PutIntegrationResponseRequest.builder()
//                .restApiId(apiId)
//                .resourceId(shortifyResourceId)
//                .httpMethod("GET")
//                .statusCode("200")
//                .responseParameters(Map.of("method.response.header.Content-Type", "integration.response.header.Content-Type"))
//                .build();
//
//        apiClient.putIntegrationResponse(putIntegrationResponseRequest);
//
//// Step 4: Define Lambda Integration for POST
//        PutIntegrationRequest putPostLambdaIntegrationRequest = PutIntegrationRequest.builder()
//                .restApiId(apiId)
//                .resourceId(shortifyResourceId)
//                .httpMethod("POST")
//                .integrationHttpMethod("POST")
//                .type(IntegrationType.AWS)
//                .uri("arn:aws:lambda:eu-west-2:your-account-id:function:your-lambda-function-name")
//                .credentials("arn:aws:iam::203918856231:role/APIGatewayLambdaRole")
//                .build();
//
//        apiClient.putIntegration(putPostLambdaIntegrationRequest);



        // Deploy app
        CreateDeploymentResponse deploymentResponse = apiClient.createDeployment(CreateDeploymentRequest.builder()
                .restApiId(apiId)
                .stageName("dev")
                .build());

        System.out.println("Deployed API Gateway at stage: dev");


    }
}
