package com.group5.htms.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.group5.htms.dto.upload.response.UploadImageResponse;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {
    private static final String USERS_FOLDER = "users";
    private static final String HORSE_FOLDER = "horse";

    private final Cloudinary cloudinary;

    @Override
    public UploadImageResponse uploadUserImage(MultipartFile file) {
        return uploadImage(file, USERS_FOLDER);
    }

    @Override
    public UploadImageResponse uploadHorseImage(MultipartFile file) {
        return uploadImage(file, HORSE_FOLDER);
    }

    private UploadImageResponse uploadImage(MultipartFile file, String folder) {
        validateImage(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );

            return UploadImageResponse.builder()
                    .url(asString(result.get("url")))
                    .secureUrl(asString(result.get("secure_url")))
                    .publicId(asString(result.get("public_id")))
                    .folder(folder)
                    .resourceType(asString(result.get("resource_type")))
                    .build();
        } catch (IOException ex) {
            throw new BadRequestException("Cannot read upload file");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
