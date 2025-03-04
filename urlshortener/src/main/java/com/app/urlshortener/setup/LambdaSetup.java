package com.app.urlshortener.setup;

import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.Runtime;
import software.amazon.awssdk.services.lambda.model.FunctionCode;
public class LambdaSetup {
    private static final String FUNCTION_NAME = "ShortifyAppLambda";

    public static void main(String[] args) {
        LambdaClient lambda = LambdaClient.create();

        CreateFunctionRequest request = CreateFunctionRequest.builder()
                .functionName(FUNCTION_NAME)
                .runtime(Runtime.JAVA11)
                .handler("com.app.urlshortener.LambdaHandler.UrlShortenerHandler::handleRequest")
                .role("arn:aws:iam::203918856231:role/url_shortener_lambda") // Replace with actual IAM role
                .code(FunctionCode.builder()
                        .s3Bucket("shortify-app") // Replace with actual S3 bucket containing JAR file
                        .s3Key("urlshortener-0.0.1-SNAPSHOT.jar") // Replace with actual JAR file name
                        .build())
                .build();

        lambda.createFunction(request);
        System.out.println("Lambda Function Created: " + FUNCTION_NAME);
    }
}
