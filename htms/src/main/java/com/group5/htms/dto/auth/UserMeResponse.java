package com.group5.htms.dto.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserMeResponse {
    private Integer id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String status;
    private List<String> roles;
}