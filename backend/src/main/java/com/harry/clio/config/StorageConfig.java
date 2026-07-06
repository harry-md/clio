package com.harry.clio.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class StorageConfig {
    @Value("${cloudinary.cloud_name}")
    private String CLOUDINARY_CLOUD_NAME;

    @Value("${cloudinary.api_key}")
    private String CLOUDINARY_API_KEY;

    @Value("${cloudinary.api_secret}")
    private String CLOUDINARY_API_SECRET;

    @Value("${r2.account_id}")
    private String R2_ACCOUNT_ID;

    @Value("${r2.access_key}")
    private String R2_ACCESS_KEY;

    @Value("${r2.secret_key}")
    private String R2_SECRET_KEY;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name",
                CLOUDINARY_CLOUD_NAME,
                "api_key",
                CLOUDINARY_API_KEY,
                "api_secret",
                CLOUDINARY_API_SECRET,
                "secure",
                true));
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }

    @Bean
    public S3Client r2Client() {
        return S3Client.builder()
                .endpointOverride(
                        URI.create("https://" + R2_ACCOUNT_ID + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(R2_ACCESS_KEY, R2_SECRET_KEY)))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(false)
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
