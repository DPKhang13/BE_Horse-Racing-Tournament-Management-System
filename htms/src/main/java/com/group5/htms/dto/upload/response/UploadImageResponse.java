package com.group5.htms.dto.upload.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UploadImageResponse {
    private String url;
    private String secureUrl;
    private String publicId;
    private String folder;
    private String resourceType;
}
