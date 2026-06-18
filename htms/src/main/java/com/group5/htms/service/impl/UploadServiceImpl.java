package com.group5.htms.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.group5.htms.dto.upload.response.UploadImageResponse;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.Users;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {
    private static final String USERS_FOLDER = "users";
    private static final String HORSE_FOLDER = "horse";

    private final Cloudinary cloudinary;
    private final UsersRepository usersRepository;
    private final HorsesRepository horsesRepository;

    @Override
    @Transactional
    public UploadImageResponse uploadUserImage(Integer userId, MultipartFile file) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UploadImageResponse response = uploadImage(file, USERS_FOLDER, userId);
        user.setAvatarUrl(response.getSecureUrl());
        usersRepository.save(user);

        return response;
    }

    @Override
    @Transactional
    public UploadImageResponse uploadHorseImage(Integer horseId, MultipartFile file) {
        Horses horse = horsesRepository.findById(horseId)
                .orElseThrow(() -> new ResourceNotFoundException("Horse not found"));

        UploadImageResponse response = uploadImage(file, HORSE_FOLDER, horseId);
        horse.setAvatarUrl(response.getSecureUrl());
        horsesRepository.save(horse);

        return response;
    }

    private UploadImageResponse uploadImage(MultipartFile file, String folder, Integer entityId) {
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
                    .entityId(entityId)
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
