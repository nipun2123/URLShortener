package com.app.urlshortener.S3Uploaders;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebAppUpload {

    private static final String BUCKET_NAME = "url-shortener-app";
    private static final String LOCAL_FOLDER_PATH = "C:/path-to-your-build-folder/";

    public static void main(String[] args) {
        S3Client s3 = S3Client.create();

        File folder = new File(LOCAL_FOLDER_PATH);
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                uploadFile(s3, file);
            }
        }
    }

    private static void uploadFile(S3Client s3, File file) {
        try {
            String key = file.getName(); // S3 key (file name)
            s3.putObject(PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .contentType(Files.probeContentType(Paths.get(file.getPath())))
                    .build(), file.toPath());

            System.out.println("Uploaded: " + key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
