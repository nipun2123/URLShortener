# Shortify CDK Infrastructure

This directory contains the AWS CDK (Java) code to deploy the Shortify URL shortener infrastructure.

## Stack Components

1. **S3 Bucket**: Hosts static website (index.html)
2. **API Gateway**: Routes requests to Lambda or S3
3. **Lambda Function**: Java handler for URL shortening/redirects
4. **DynamoDB**: Stores short URL mappings
5. **ElastiCache Redis**: Caches frequent URL lookups
6. **VPC**: Private networking for Lambda and Redis
7. **VPC Endpoints**: Secure access to DynamoDB

## Deployment

1. Install dependencies:
   ```bash
   npm install -g aws-cdk
   mvn install
   ```
2. Build the Lambda package:
   ```bash
   mvn package
    ```
3. Deploy the stack:
   ```bash
   cdk bootstrap
   cdk synth
   cdk deploy
   ```

## 🛠️ Configuration

- Environment variables in ShortifyAppStack.java:
- DYNAMODB_TABLE: DynamoDB table name
- REDIS_ENDPOINT: Redis cluster endpoint
- REDIS_PORT: Redis port

## 🛠️ Customizing the Stack

* Modify the `src/main/java/com/myorg/ShortifyAppStack.java` file to adjust configurations.

## 🧹Clean Up
   ```bash
     cdk deploy
   ```

## 📝 Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation
 * `cdk destroy`     destroy the resources created by CDK app

📌 Notes

* It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven-compatible Java IDE to build and run tests.

* Ensure IAM permissions are correctly set before deploying.

* Configure your AWS CLI with the necessary credentials.

Let me know if you’d like any refinements! 🚀
