package com.harry.clio.service.impl;

import com.adobe.epubcheck.api.EpubCheck;
import com.harry.clio.dto.book.PresignedUpload;
import com.harry.clio.exception.InvalidEbookException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class R2Service {
    private static final int EPUBCHECK_FATAL = 4;

    @Value("${r2.bucket-name}")
    private String r2BucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public PresignedUpload createOriginUploadUrl() {
        String objectKey = "books/origin/" + UUID.randomUUID() + ".epub";
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(r2BucketName)
                .key(objectKey)
                .contentType("application/epub+zip")
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(req)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return new PresignedUpload(
                objectKey, presignedRequest.url().toExternalForm(), "application/epub+zip");
    }

    public Path downloadToTemp(String objectKey) {
        Path tmpFile = null;
        try {
            tmpFile = Files.createTempFile("origin-", ".epub");
            Files.deleteIfExists(tmpFile);

            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(r2BucketName)
                    .key(objectKey)
                    .build();

            s3Client.getObject(req, tmpFile);
            return tmpFile;
        } catch (IOException | RuntimeException ex) {
            if (tmpFile != null) {
                deleteTmpFile(tmpFile);
            }
            throw new RuntimeException(ex);
        }
    }

    public String uploadEncryptedEbook(Path file) {
        String objectKey = "books/encrypted/" + UUID.randomUUID();
        try {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(r2BucketName)
                    .key(objectKey)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .build();

            s3Client.putObject(req, RequestBody.fromFile(file));
            return objectKey;
        } catch (RuntimeException ex) {
            log.error("Lỗi upload file {}", objectKey, ex);
            throw ex;
        }
    }

    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return;
        }

        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder()
                    .bucket(r2BucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(req);
        } catch (RuntimeException ex) {
            log.error("Lỗi xóa file {}", objectKey, ex);
        }
    }

    public String getPresignedUrl(String objectKey) {
        GetObjectRequest objectRequest =
                GetObjectRequest.builder().bucket(r2BucketName).key(objectKey).build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(30))
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toExternalForm();
    }

    public void validateEbook(Path path) {
        EpubCheck epubCheck = new EpubCheck(path.toFile());
        int result = epubCheck.doValidate();

        boolean isFatal = (result & EPUBCHECK_FATAL) != 0;
        if (isFatal) {
            throw new InvalidEbookException("File ebook có lỗi");
        }
    }

    private void deleteTmpFile(Path tmpFile) {
        if (tmpFile != null) {
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException ex) {
                log.error("Lỗi xóa file tạm {}", tmpFile.getFileName().toString(), ex);
            }
        }
    }
}
