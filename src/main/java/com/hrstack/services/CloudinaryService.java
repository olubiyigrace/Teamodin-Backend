package com.hrstack.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hrstack.dto.responseDto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public ImageUploadResponse upload(MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.emptyMap()
            );
            return ImageUploadResponse.builder()
                    .imageUrl(uploadResult.get("secure_url").toString())
                    .publicId(uploadResult.get("public_id").toString())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Unable to upload image.");
        }
    }

    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete image.");
        }
    }
}
