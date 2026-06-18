package com.group5.htms.controller;

import com.group5.htms.dto.upload.response.UploadImageResponse;
import com.group5.htms.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final UploadService uploadService;

    @Operation(summary = "Upload user image", description = "Upload ảnh user/avatar lên Cloudinary folder users.")
    @PostMapping(value = "/users/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UploadImageResponse> uploadUserImage(
            @PathVariable Integer userId,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(uploadService.uploadUserImage(userId, file));
    }

    @Operation(summary = "Upload horse image", description = "Upload ảnh ngựa lên Cloudinary folder horse.")
    @PostMapping(value = "/horses/{horseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UploadImageResponse> uploadHorseImage(
            @PathVariable Integer horseId,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(uploadService.uploadHorseImage(horseId, file));
    }
}
