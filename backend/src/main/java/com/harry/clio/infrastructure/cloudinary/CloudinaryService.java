package com.harry.clio.infrastructure.cloudinary;

import com.cloudinary.Cloudinary;
import com.harry.clio.shared.exception.CloudinaryException;

import lombok.RequiredArgsConstructor;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;
    private final Tika tika;
    private final int MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    public void validate(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new CloudinaryException("Ảnh vượt quá kích thước cho phép");
        }
    }
}
