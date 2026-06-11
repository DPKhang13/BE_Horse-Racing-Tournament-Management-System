package com.group5.htms.mapper;

import com.group5.htms.dto.auth.UserMeResponse;
import com.group5.htms.entity.Users;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthMapper {

    public UserMeResponse toUserMeResponse(Users user) {
        List<String> roles = user.getRoleType() == null || user.getRoleType().isBlank()
                ? List.of()
                : List.of(user.getRoleType().toLowerCase());

        return UserMeResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .roles(roles)
                .build();
    }
}
