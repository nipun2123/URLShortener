package com.app.urlshortener.setup;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

public class S3Setup {

    private static final String BUCKET_NAME = "url-shortener-app";

    public static void main(String[] args) {
        S3Client s3 = S3Client.create();

        // Check if bucket exists
        if (!s3.listBuckets().buckets().stream().anyMatch(b -> b.name().equals(BUCKET_NAME))) {
            // Create S3 bucket
            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                    .bucket(BUCKET_NAME)
                    .build();
            s3.createBucket(bucketRequest);
            System.out.println("S3 Bucket Created: " + BUCKET_NAME);
        } else {
            System.out.println("Bucket already exists.");
        }
    }


}
