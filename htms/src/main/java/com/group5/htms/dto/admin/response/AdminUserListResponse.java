package com.group5.htms.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AdminUserListResponse {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String roleType;
    private String status;
    private String avatarUrl;
    private Instant createdAt;
    private String profileStatus;
}
