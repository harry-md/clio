package com.harry.clio.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.harry.clio.exception.CloudinaryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;
    private final Tika tika;
    private static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    private void validate(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new CloudinaryException("Ảnh vượt quá kích thước cho phép");
        }
        try {
            String mimeType = tika.detect(file.getInputStream());
            if (!mimeType.startsWith("image/")) {
                throw new CloudinaryException("Định dạng file không hợp lệ");
            }
        } catch (IOException ex) {
            log.error("Lỗi khi đọc file {}", ex);
            throw new CloudinaryException("Lỗi khi đọc file");
        }
    }

    public String upload(MultipartFile file) {
        validate(file);
        try {
            Map res = cloudinary
                    .uploader()
                    .upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            return res.get("secure_url").toString();
        } catch (IOException ex) {
            log.error("Lỗi khi upload ảnh {}", ex);
            throw new CloudinaryException("Lỗi khi upload ảnh");
        }
    }

    public void delete(String url) {
        String[] parts = url.split("/upload/");
        String path = parts[1];
        if (path.startsWith("v") && path.contains("/")) {
            path = path.substring(path.indexOf("/") + 1);
        }
        String publicId = path.substring(0, path.lastIndexOf("."));

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "auto"));
        } catch (IOException ex) {
            log.error("Lỗi khi xóa ảnh: {}", ex);
            throw new CloudinaryException("Lỗi khi xóa ảnh");
        }
    }
}
