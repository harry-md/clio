package com.harry.clio.service;

import com.adobe.epubcheck.api.EpubCheck;
import com.harry.clio.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class R2Service {
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;
    private static final int EPUBCHECK_ERROR = 2;
    private static final int EPUBCHECK_FATAL = 4;

    @Value("${r2.bucket_name}")
    private String R2_BUCKET_NAME;

    private final S3Client s3Client;

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
            tmpFile.delete();
        }
    }

    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return;
        }

        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(R2_BUCKET_NAME)
                .key(objectKey)
                .build();
        s3Client.deleteObject(req);
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
                throw new BadRequestException("File EPUB không hợp lệ");
            }
            return tmpFile;
        } catch (IOException ex) {
            throw new BadRequestException("Lỗi đọc file epub");
        } catch (RuntimeException ex) {
            if (tmpFile != null) {
                tmpFile.delete();
            }
            throw ex;
        }
    }
}
