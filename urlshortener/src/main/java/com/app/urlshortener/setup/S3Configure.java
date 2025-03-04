package com.app.urlshortener.setup;

import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class S3Configure {

    private static final String BUCKET_NAME = "shortify-appdemo";

    public static void main(String[] args) {
        try(S3Client s3 = S3Client.create()) {

            // Check if bucket exists
            if (s3.listBuckets().buckets().stream().anyMatch(b -> b.name().equals(BUCKET_NAME))) {

                // Enable static website hosting
                PutBucketWebsiteRequest websiteRequest = PutBucketWebsiteRequest.builder()
                        .bucket(BUCKET_NAME)
                        .websiteConfiguration(builder -> builder
                                .indexDocument(IndexDocument.builder().suffix("index.html").build())
                                .errorDocument(ErrorDocument.builder().key("error.html").build()))
                        .build();

                s3.putBucketWebsite(websiteRequest);
                System.out.println("S3 Website Hosting Enabled!");

//              Disable Block Public Access
                s3.putPublicAccessBlock(PutPublicAccessBlockRequest.builder()
                        .bucket("shortify-app")
                        .publicAccessBlockConfiguration(PublicAccessBlockConfiguration.builder()
                                .blockPublicAcls(false)
                                .ignorePublicAcls(false)
                                .blockPublicPolicy(false)
                                .restrictPublicBuckets(false)
                                .build())
                        .build());
                System.out.println("Block Public Access Disabled!");


                // Public Read-Only Policy
                String bucketPolicy = "{ \"Version\": \"2012-10-17\", \"Statement\": [{ \"Effect\": \"Allow\", \"Principal\": \"*\", \"Action\": \"s3:GetObject\", \"Resource\": \"arn:aws:s3:::" + BUCKET_NAME + "/*\" }] }";

                // Apply Policy
                s3.putBucketPolicy(PutBucketPolicyRequest.builder()
                        .bucket(BUCKET_NAME)
                        .policy(bucketPolicy)
                        .build());
                System.out.println("Bucket Policy Updated: Public Access Enabled!");
            } else {
                System.out.println("Bucket is not exists.");
            }

        }
    }

}
