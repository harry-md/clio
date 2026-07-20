package com.harry.clio.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.harry.clio.exception.CloudinaryException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;
    private final Tika tika;
    private static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final List<String> defaultUrls = List.of(
            "https://res.cloudinary.com/dswxedhsf/image/upload/v1782883664/book_fgsg7m.jpg",
            "https://res.cloudinary.com/dswxedhsf/image/upload/v1782626276/avatar_qoprdc.png");

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
            log.error("Lỗi khi đọc file {}", file.getName(), ex);
            throw new CloudinaryException("Lỗi khi đọc file");
        }
    }

    private void validate(byte[] data) {
        if (data.length > MAX_IMAGE_SIZE) {
            throw new CloudinaryException("Ảnh vượt quá kích thước cho phép");
        }
        String mimeType = tika.detect(data);
        if (!mimeType.startsWith("image/")) {
            throw new CloudinaryException("Định dạng file không hợp lệ");
        }
    }

    public String upload(MultipartFile file) {
        validate(file);
        try {
            Map res = cloudinary
                    .uploader()
                    .upload(file.getBytes(), ObjectUtils.asMap("resource_type", "image"));
            return res.get("secure_url").toString();
        } catch (IOException ex) {
            log.error("Lỗi khi upload ảnh Cloudinary {}", file.getName(), ex);
            throw new CloudinaryException("Lỗi khi upload ảnh");
        }
    }

    public String upload(byte[] data) {
        validate(data);
        try {
            Map res =
                    cloudinary.uploader().upload(data, ObjectUtils.asMap("resource_type", "image"));
            return res.get("secure_url").toString();
        } catch (IOException ex) {
            log.error("Lỗi khi upload ảnh bìa Cloudinary", ex);
            throw new CloudinaryException("Lỗi khi upload ảnh bìa");
        }
    }

    public void delete(String url) {
        if (url == null || defaultUrls.contains(url)) {
            return;
        }
        String[] parts = url.split("/upload/");
        String path = parts[1];
        if (path.startsWith("v") && path.contains("/")) {
            path = path.substring(path.indexOf("/") + 1);
        }
        String publicId = path.substring(0, path.lastIndexOf("."));

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "auto"));
        } catch (IOException ex) {
            log.error("Lỗi khi xóa ảnh Cloudinary {}", url, ex);
            throw new CloudinaryException("Lỗi khi xóa ảnh");
        }
    }
}
