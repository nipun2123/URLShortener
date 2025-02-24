package com.app.urlshortener.setup;

import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;

public class LambdaSetup {
    private static final String FUNCTION_NAME = "UrlShortenerLambda";

    public static void main(String[] args) {
//        LambdaClient lambda = LambdaClient.create();
//
//        CreateFunctionRequest request = CreateFunctionRequest.builder()
//                .functionName(FUNCTION_NAME)
//                .runtime(Runtime.JAVA17)
//                .handler("com.app.urlshortener.UrlShortenerHandler::handleRequest")
//                .role("arn:aws:iam::YOUR_ACCOUNT_ID:role/LambdaExecutionRole") // Replace with actual IAM role
//                .code(Code.builder()
//                        .s3Bucket("your-lambda-code-bucket") // Replace with actual S3 bucket containing JAR file
//                        .s3Key("url-shortener.jar") // Replace with actual JAR file name
//                        .build())
//                .build();
//
//        lambda.createFunction(request);
//        System.out.println("Lambda Function Created: " + FUNCTION_NAME);
    }
}
