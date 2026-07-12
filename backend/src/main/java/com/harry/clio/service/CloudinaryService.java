package com.harry.clio.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.harry.clio.exception.CloudinaryException;

import lombok.RequiredArgsConstructor;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;
    private final Tika tika;
    private static final int MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static List<String> defaultUrls = List.of(
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
            throw new CloudinaryException("Lỗi khi upload ảnh");
        }
    }

    public void delete(String url) {
        if (defaultUrls.contains(url)) {
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
            throw new CloudinaryException("Lỗi khi xóa ảnh");
        }
    }
}
