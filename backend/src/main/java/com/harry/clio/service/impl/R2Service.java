package com.harry.clio.service.impl;

import com.adobe.epubcheck.api.EpubCheck;
import com.harry.clio.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class R2Service {
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    private static final int EPUBCHECK_FATAL = 4;

    @Value("${r2.bucket-name}")
    private String R2_BUCKET_NAME;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public String uploadOriginEbook(MultipartFile file) {
        File tmpFile = validateEbook(file);
        String objectKey = "books/origin/" + UUID.randomUUID() + ".epub";

        try {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(R2_BUCKET_NAME)
                    .key(objectKey)
                    .contentType("application/epub+zip")
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(req, RequestBody.fromFile(tmpFile));
            return objectKey;
        } finally {
            deleteTmpFile(tmpFile);
        }
    }

    public Path downloadToTemp(String objectKey) {
        Path tmpFile = null;
        try {
            tmpFile = Files.createTempFile("origin-", ".epub");
            Files.deleteIfExists(tmpFile);

            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(R2_BUCKET_NAME)
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
                    .bucket(R2_BUCKET_NAME)
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
                    .bucket(R2_BUCKET_NAME)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(req);
        } catch (RuntimeException ex) {
            log.error("Lỗi xóa file {}", objectKey, ex);
        }
    }

    public String getPresignedUrl(String objectKey) {
        GetObjectRequest objectRequest =
                GetObjectRequest.builder().bucket(R2_BUCKET_NAME).key(objectKey).build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(objectRequest)
                .build();
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toExternalForm();
    }

    private File validateEbook(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File vượt quá kích thước tối đa");
        }

        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".epub");
            file.transferTo(tmpFile);

            EpubCheck epubCheck = new EpubCheck(tmpFile);
            int result = epubCheck.doValidate();
            boolean hasFatal = (result & EPUBCHECK_FATAL) != 0;
            if (hasFatal) {
                throw new BadRequestException("File Ebook có lỗi");
            }
            return tmpFile;
        } catch (IOException ex) {
            deleteTmpFile(tmpFile);
            throw new BadRequestException("Lỗi đọc file ebook");
        } catch (RuntimeException ex) {
            deleteTmpFile(tmpFile);
            throw ex;
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

    private void deleteTmpFile(File tmpFile) {
        if (tmpFile != null) {
            try {
                Files.deleteIfExists(tmpFile.toPath());
            } catch (IOException ex) {
                log.error("Lỗi xóa file tạm {}", tmpFile.getName(), ex);
            }
        }
    }
}
