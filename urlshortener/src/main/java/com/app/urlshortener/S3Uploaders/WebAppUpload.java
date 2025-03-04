package com.app.urlshortener.S3Uploaders;

import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebAppUpload {

    private static final String BUCKET_NAME = "shortify-app";
    private static final String LOCAL_FOLDER_PATH = "E:\\Projects\\AWS tutorials\\Shorten Url Project\\urlshortener\\urlshortener\\src\\main\\resources\\static\\";

    public static void main(String[] args) {
        S3Client s3 = S3Client.create();

        File folder = new File(LOCAL_FOLDER_PATH);
        System.out.println(folder.exists());
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
