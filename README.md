# Shortfy - Serverless URL Shortener

Shortfy is a serverless URL shortener built using AWS services for high availability, low latency, and cost efficiency.

## 🚀 Features

* Shorten URLs via a simple API.

* Retrieve long URLs using a short code with automatic redirection.

* Fast lookups with ElastiCache (Redis) caching.

* Persistent storage using DynamoDB.

* Serverless and cost-efficient, powered by AWS Lambda.

## 🏗️ Architecture

* **API Gateway** – Exposes REST API.

* **Lambda** – Business logic for shortening and retrieving URLs.

* **DynamoDB** – Stores short and long URL mappings.

* **Redis (ElastiCache)** – Caches mappings for faster lookups.

* **S3** – Hosts the web frontend.

* **AWS CDK** – Infrastructure as Code.

## ⚡ How It Works

* **Shorten a URL** – Send a POST request to /handler with a long URL.

* **Retrieve a URL** – Send a GET request with the shortcode.

* **Performance Boost** – DynamoDB is the source of truth, and Redis caches recent lookups for a 1-hour TTL.

## 🛠️ Deployment

Deploy the full infrastructure using AWS CDK:
```bash
cd urlshortener
cd cdk
cdk bootstrap
cdk synth
cdk deploy
```
For more details on deployment, have a look at the **README file in CDK**.
![Shortfy diagram](https://github.com/user-attachments/assets/b1f48094-af92-4c56-b9a8-68166ec30a98)
