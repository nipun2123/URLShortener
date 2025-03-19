package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.s3.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.s3.deployment.*;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpoint;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpointAwsService;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.elasticache.*;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.Duration;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.io.File;
import software.amazon.awscdk.services.s3.assets.Asset;


public class ShortifyAppStack extends Stack {
    public ShortifyAppStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    private static final String PATH_FOR_HTML = "E:/Projects/AWS tutorials/Shorten Url Project/urlshortener/urlshortener/src/main/resources/static/";

    public ShortifyAppStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create the S3 bucket
        Bucket s3ShortifyBucket = Bucket.Builder.create(this, "ShortifyAppBucket")
                .bucketName("shortify-app")  // Set the bucket name
                .versioned(false)
                .websiteIndexDocument("index.html")  // Set the index document for static website hosting
                .removalPolicy(RemovalPolicy.DESTROY)
                .publicReadAccess(true)
                .autoDeleteObjects(true)
                .blockPublicAccess(
                        BlockPublicAccess.Builder.create()
                                .blockPublicPolicy(false)
                                .ignorePublicAcls(false)
                                .restrictPublicBuckets(false)
                                .build()) // Block all public access settings
                .build();

        // Upload a file to the bucket
        BucketDeployment.Builder.create(this, "DeployFile")
                .sources(List.of(Source.asset(PATH_FOR_HTML))) // Path to your file
                .destinationBucket(s3ShortifyBucket)
                .build();

        // Create a DynamoDB table
        Table dynamodbShortifyTable = Table.Builder.create(this, "ShortifyAppTable")
                .tableName("shortify-app")  // Set table name
                .partitionKey(Attribute.builder()
                        .name("short_id")  // Set partition key
                        .type(AttributeType.STRING)  // Data type is String
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)  // Use on-demand billing
                .removalPolicy(RemovalPolicy.DESTROY)  // Destroy on stack deletion
                .build();

        // Create a VPC for both Redis and Lambda
        Vpc vpc = Vpc.Builder.create(this, "ShortifyVpc")
                .maxAzs(3) // 3 availability zones
                .build();

        // Create a Gateway VPC Endpoint for DynamoDB
        GatewayVpcEndpoint dynamoDbEndpoint = GatewayVpcEndpoint.Builder.create(this, "DynamoDbEndpoint")
                .vpc(vpc)
                .service(GatewayVpcEndpointAwsService.DYNAMODB)
                .build();

        // Create a Security Group for Redis
        SecurityGroup redisSecurityGroup = SecurityGroup.Builder.create(this, "ShortifyRedisSecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        // Create a Security Group for the Lambda function
        SecurityGroup lambdaSecurityGroup = SecurityGroup.Builder.create(this, "ShortifyLambdaSecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        // Allow Lambda to access Redis
        redisSecurityGroup.addIngressRule(lambdaSecurityGroup, Port.tcp(6379), "Allow Lambda to access Redis");

        // Create an ElastiCache Redis Cluster
        CfnSubnetGroup redisSubnetGroup = CfnSubnetGroup.Builder.create(this, "ShortifyRedisSubnetGroup")
                .cacheSubnetGroupName("redis-subnet-group")
                .subnetIds(vpc.getPrivateSubnets().stream().map(ISubnet::getSubnetId).toList())
                .description("Subnet group for Redis")
                .build();

        CfnCacheCluster redisCluster = CfnCacheCluster.Builder.create(this, "ShortifyRedisCluster")
                .engine("redis")
                .cacheNodeType("cache.t3.micro")
                .numCacheNodes(1)
                .vpcSecurityGroupIds(List.of(redisSecurityGroup.getSecurityGroupId()))
                .cacheSubnetGroupName(redisSubnetGroup.getCacheSubnetGroupName())
                .build();

        // Output Redis Endpoint
        CfnOutput.Builder.create(this, "RedisEndpoint")
                .value(redisCluster.getAttrRedisEndpointAddress())
                .description("Redis Endpoint Address")
                .build();

        // Define the Lambda function
        Function lambdaUrlShortener = Function.Builder.create(this, "UrlShortenerLambda")
                .functionName("shortify-url-shortener") // Set Lambda function name
                .runtime(Runtime.JAVA_17)  // Java 17 runtime
                .handler("com.app.urlshortener.LambdaHandler.UrlShortenerHandler::handleRequest")
                .code(Code.fromAsset("../target/urlshortener-0.0.1-SNAPSHOT.jar"))
                .timeout(Duration.seconds(40))
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .securityGroups(List.of(lambdaSecurityGroup))
                .environment(Map.of(
                        "DYNAMODB_TABLE", dynamodbShortifyTable.getTableName(),
                        "REDIS_ENDPOINT", redisCluster.getAttrRedisEndpointAddress(),
                        "REDIS_PORT", redisCluster.getAttrRedisEndpointPort()
                ))
                .build();

        // Grant permition of dyanamodb read and write data for lambda function
        dynamodbShortifyTable.grantReadWriteData(lambdaUrlShortener);

        // Grant API Gateway permissions to invoke Lambda
        lambdaUrlShortener.grantInvoke(new ServicePrincipal("apigateway.amazonaws.com"));

        // Grant API Gateway read access to the S3 bucket
        Role apiRole = Role.Builder.create(this, "ApiGatewayS3Role")
                .assumedBy(new ServicePrincipal("apigateway.amazonaws.com"))
                .build();
        s3ShortifyBucket.grantRead(apiRole);

        // Create API Gateway
        RestApi apiShortify = RestApi.Builder.create(this, "ShortifyApi")
                .restApiName("ShortifyAppAPI")
                .description("API for Shortify Application")
                .build();

        // Create api integration for the s3 bucket
        AwsIntegration awsIntegrationS3 = new AwsIntegration(AwsIntegrationProps.builder()
                .service("s3")
                .integrationHttpMethod("GET")
                .path(s3ShortifyBucket.getBucketName() + "/index.html") // Route to S3 bucket
                .options(IntegrationOptions.builder()
                        .credentialsRole(apiRole)
                        .integrationResponses(List.of(
                                IntegrationResponse.builder()
                                        .statusCode("200")
                                        .responseParameters(Map.of(
                                                "method.response.header.Content-Type", "'text/html'"
                                        ))
                                        .build(),
                                IntegrationResponse.builder()
                                        .statusCode("403")
                                        .build(),
                                IntegrationResponse.builder()
                                        .statusCode("500")
                                        .build()
                        ))
                        .build())
                .build());

        // Route: GET / → S3 bucket
        apiShortify.getRoot().addMethod("GET", awsIntegrationS3, MethodOptions.builder()
                .methodResponses(List.of(
                        MethodResponse.builder()
                                .statusCode("200")
                                .responseParameters(Map.of(
                                        "method.response.header.Content-Type", true
                                ))
                                .build(),
                        MethodResponse.builder()
                                .statusCode("403")
                                .build(),
                        MethodResponse.builder()
                                .statusCode("500")
                                .build()
                ))
                .build());

        // Create api integration for the lambda function
        Integration lambdaIntegration = new LambdaIntegration(lambdaUrlShortener, LambdaIntegrationOptions.builder()
                .proxy(true)
                .integrationResponses(
                        List.of(
                                IntegrationResponse.builder()
                                        .statusCode("201")
                                        .build(),
                                IntegrationResponse.builder()
                                        .statusCode("302")
                                        .build(),
                                IntegrationResponse.builder()
                                        .statusCode("400")
                                        .build(),
                                IntegrationResponse.builder()
                                        .statusCode("404")
                                        .build()
                        )
                )
                .passthroughBehavior(PassthroughBehavior.NEVER)
                .build());

        // Route: GET /{shortCode} → Lambda
        Resource urlParamResource = apiShortify.getRoot().addResource("{shortCode}"); // Path parameter
        urlParamResource.addMethod("GET", lambdaIntegration, MethodOptions.builder()
                .methodResponses(List.of(
                        MethodResponse.builder().statusCode("302").build(),
                        MethodResponse.builder().statusCode("400").build(),
                        MethodResponse.builder().statusCode("404").build()

                ))
                .build());

        // Route: POST /handler → Lambda
        Resource handlerResource = apiShortify.getRoot().addResource("handler");
        handlerResource.addMethod("POST", lambdaIntegration, MethodOptions.builder()
                .methodResponses(List.of(
                        MethodResponse.builder().statusCode("201").build(),
                        MethodResponse.builder().statusCode("400").build()
                        ))
                .build());

        // Deploy API
        Deployment deployment = Deployment.Builder.create(this, "Deployment")
                .api(apiShortify)
                .build();

        Stage stage = Stage.Builder.create(this, "dev")
                .deployment(deployment)
                .stageName("dev")
                .build();

        // Output the API Gateway Invoke URL
        CfnOutput.Builder.create(this, "ApiGatewayInvokeUrl")
                .value(apiShortify.getUrl())
                .description("API Gateway Base URL")
                .build();
    }
}
