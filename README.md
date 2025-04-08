# Shortify - AWS URL Shortener

Shortify is a high-performance URL shortener built on AWS with Java.

## Features

- 🚀 Serverless architecture
- ⚡ Sub-millisecond redirects with Redis caching
- 🧠 Smart caching: Automatic Redis population with 1-hour TTL on cache misses
- 🔒 Secure VPC-hosted services
- 📊 SHA-256 + Base62 URL hashing
- 🏗️ Infrastructure as Code with AWS CDK

## Architecture

![Shortfy diagram](https://github.com/user-attachments/assets/b1f48094-af92-4c56-b9a8-68166ec30a98)

1. **Frontend**: Static HTML hosted on S3
2. **API Layer**: API Gateway routes to Lambda
3. **Caching**: Redis (ElastiCache) for fast lookups
4. **Storage**: DynamoDB for persistent URL mapping
5. **Networking**: VPC with private subnets and endpoints

## Deployment

### Prerequisites
- AWS account
- AWS CLI configured
- Java JDK 17+
- Maven
- AWS CDK

### Steps
1. Clone the repository
2. Build the project: `mvn package`
3. Deploy with CDK: `cdk deploy`
4. Access the application at the provided API Gateway URL

For more details on CDK deployment, look at the **README file in CDK**.

## Usage
- POST `/handler` with `{"longUrl": "https://example.com"}` to create a short URL
- GET `/{shortCode}` to redirect to original URL

## Contributing
Pull requests are welcome! Please open an issue first to discuss changes.
