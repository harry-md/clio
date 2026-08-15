package com.harry.clio.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.harry.clio.config.properties.CloudinaryProperties;
import com.harry.clio.config.properties.R2Properties;

import lombok.RequiredArgsConstructor;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@RequiredArgsConstructor
@Configuration
public class StorageConfig {
    private final CloudinaryProperties cloudinaryProperties;
    private final R2Properties r2Properties;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name",
                cloudinaryProperties.cloudName(),
                "api_key",
                cloudinaryProperties.apiKey(),
                "api_secret",
                cloudinaryProperties.apiSecret(),
                "secure",
                true));
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(
                        "https://" + r2Properties.accountId() + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        r2Properties.accessKey(), r2Properties.secretKey())))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(false)
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(
                        "https://" + r2Properties.accountId() + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        r2Properties.accessKey(), r2Properties.secretKey())))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(false)
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
