package com.group5.htms.dto.admin.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserCreateRequest {
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

    @NotBlank
    @Pattern(regexp = "admin|horse_owner|jockey|race_referee|spectator",
            message = "roleType must be one of: admin, horse_owner, jockey, race_referee, spectator")
    private String roleType;

    @Size(max = 150)
    private String stableName;

    @Size(max = 50)
    private String licenseNumber;

    @Size(max = 255)
    private String address;

    private Integer favoriteJockeyId;
    private Integer experienceYears;
}
