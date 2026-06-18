package com.group5.htms.service;

import com.group5.htms.dto.upload.response.UploadImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    UploadImageResponse uploadUserImage(Integer userId, MultipartFile file);

    UploadImageResponse uploadHorseImage(Integer horseId, MultipartFile file);
}
