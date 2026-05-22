package com.group5.htms.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;

    // role_type: horse_owner, jockey, race_referee, spectator (admin không cho tự đăng ký)
    @NotBlank
    @Pattern(regexp = "horse_owner|jockey|race_referee|spectator",
            message = "role must be one of: horse_owner, jockey, race_referee, spectator")
    private String roleType;
}