package com.group5.htms.dto.admin.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserUpdateRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Pattern(regexp = "admin|horse_owner|jockey|race_referee|spectator",
            message = "Role type cannot be changed after user creation")
    private String roleType;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;

    @Size(max = 150, message = "Stable name must not exceed 150 characters")
    private String stableName;

    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    private Integer favoriteJockeyId;

    @Min(value = 0, message = "Experience years must be greater than or equal to 0")
    private Integer experienceYears;

    @Size(max = 20, message = "Profile status must not exceed 20 characters")
    private String profileStatus;
}
